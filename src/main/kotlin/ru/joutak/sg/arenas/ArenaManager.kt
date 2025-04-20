package ru.joutak.sg.arenas

import com.onarandombox.MultiverseCore.enums.AllowedPortalType
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.World
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.utils.PluginManager
import java.io.File

object ArenaManager {
    private val arenas = mutableMapOf<String, World>()
    private var template: World? = null
    val GOLD_PEDESTAL = SpawnLocation("GOLD_PEDESTAL", -493.0, 76.0, -665.0, 90.0, 0.0)
    val SILVER_PEDESTAL = SpawnLocation("SILVER_PEDESTAL", -495.0, 76.0, -674.0, 45.0, 0.0)
    val BRONZE_PEDESTAL = SpawnLocation("BRONZE_PEDESTAL", -496.0, 76.0, -656.0, 127.0, 0.0)
    val PEDESTALS = SpawnLocation("PEDESTALS", -500.0, 74.0, -665.0, -90.0, 0.0)

    fun get(name: String): World? = arenas[name]

    fun getArenas(): Map<String, World> = arenas

    fun deleteExistingArenas() {
        val worldContainer = Bukkit.getWorldContainer()
        val arenaDirs =
            worldContainer.listFiles { file -> file.isDirectory && file.name.startsWith("sg_arena_") }?.filterNotNull()
                ?: return

        for (dir in arenaDirs) {
            val name = dir.name
            deleteArena(name)
        }
    }

    fun isArena(world: World) = arenas.containsKey(world.name)

    fun setTemplate() {
        template = Bukkit.getWorld(Config.get(ConfigKeys.ARENA_WORLD_NAME))
        if (template == null) {
            PluginManager.getLogger().severe(
                "Отсутствует мир ${Config.get(ConfigKeys.ARENA_WORLD_NAME)}! Проверьте наличие мира с ареной и укажите верное название.",
            )
        } else {
            configureArena(template!!)
        }
    }

    fun configureArena(world: World) {
        val mvWorld = PluginManager.multiverseCore.mvWorldManager.getMVWorld(world)
        mvWorld.setTime("day")
        mvWorld.setEnableWeather(false)
        mvWorld.setDifficulty(Difficulty.NORMAL)
        mvWorld.setGameMode(GameMode.SURVIVAL)
        mvWorld.setPVPMode(false) // будет включено во время игры через определенный cooldown
        mvWorld.hunger = true
        mvWorld.setAllowAnimalSpawn(true)
        mvWorld.setAllowMonsterSpawn(false)
        mvWorld.allowPortalMaking(AllowedPortalType.NONE)
        world.setGameRule(GameRule.FALL_DAMAGE, true)
        world.setGameRule(GameRule.FIRE_DAMAGE, true)
        world.setGameRule(GameRule.DROWNING_DAMAGE, true)
        world.setGameRule(GameRule.FREEZE_DAMAGE, true)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.worldBorder.size = 640.0
        world.worldBorder.center = world.spawnLocation
    }

    fun cloneArena(): World {
        if (template == null) {
            throw NullPointerException("Отсутствует мир ${Config.get(ConfigKeys.ARENA_WORLD_NAME)}! Невозможно склонировать арену.")
        }
        val worldName = "sg_arena_${arenas.size + 1}"
        if (!PluginManager.multiverseCore.mvWorldManager.cloneWorld(template!!.name, worldName)) {
            throw Exception("Не удалось склонировать мир! Проверьте логи плагина MultiVerse.")
        }
        arenas[worldName] = Bukkit.getWorld(worldName)!!
        return arenas[worldName]!!
    }

    fun deleteArena(worldName: String) {
        val mvWorldManager = PluginManager.multiverseCore.mvWorldManager
        if (mvWorldManager.isMVWorld(worldName)) {
            mvWorldManager.deleteWorld(worldName)
        } else {
            Bukkit.getScheduler().runTaskLater(
                PluginManager.survivalGames,
                Runnable {
                    if (deleteWorldFolder(worldName)) {
                        PluginManager.getLogger().info("Удаление арены $worldName прошло успешно!")
                    } else {
                        PluginManager.getLogger().severe("Не удалось удалить папку с ареной $worldName!")
                    }
                },
                20L,
            )
        }
        arenas.remove(worldName)
    }

    private fun deleteWorldFolder(worldName: String): Boolean {
        val worldFolder = File(Bukkit.getWorldContainer(), worldName)
        if (!worldFolder.exists()) return true

        return worldFolder.deleteRecursively()
    }
}
