package ru.joutak.sg.arenas

import org.mvplugins.multiverse.core.MultiverseCore
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.World
import org.mvplugins.multiverse.core.MultiverseCoreApi
import org.mvplugins.multiverse.core.world.AllowedPortalType
import org.mvplugins.multiverse.core.world.options.CloneWorldOptions
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions
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
        val coreApi = MultiverseCoreApi.get();
        val worldManager = coreApi.worldManager


        val mvWorldOption = worldManager.getWorld(world.name)

        if (mvWorldOption.isDefined) {
            val mvWorld = mvWorldOption.get()

            mvWorld.setAllowWeather(false)
            mvWorld.setDifficulty(Difficulty.NORMAL)
            mvWorld.setGameMode(GameMode.SURVIVAL)
            mvWorld.setPvp(false)
            mvWorld.setHunger(true)
            mvWorld.setPortalForm(AllowedPortalType.NONE)


            try {
                val setTimeMethod = mvWorld.javaClass.getMethod("setTime", Long::class.java)
                setTimeMethod.invoke(mvWorld, 1000L) // Day time
            } catch (_: Exception) {
            }
        }


        world.setGameRule(GameRule.FALL_DAMAGE, true)
        world.setGameRule(GameRule.FIRE_DAMAGE, true)
        world.setGameRule(GameRule.DROWNING_DAMAGE, true)
        world.setGameRule(GameRule.FREEZE_DAMAGE, true)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.worldBorder.size = 640.0
        world.worldBorder.center = world.spawnLocation
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
    }

    fun cloneArena(): World {
        if (template == null) {
            throw NullPointerException("Отсутствует мир ${Config.get(ConfigKeys.ARENA_WORLD_NAME)}! Невозможно склонировать арену.")
        }
        val worldName = "sg_arena_${arenas.size + 1}"
        val coreApi = MultiverseCoreApi.get();
        val worldManager = coreApi.worldManager

        val mvTemplateOption = worldManager.getWorld(template!!.name)

        val mvTemplate = mvTemplateOption.getOrNull()
            ?: throw Exception("Шаблонный мир не зарегистрирован в Multiverse!")

        val cloneOptions = CloneWorldOptions.fromTo(mvTemplate, worldName)
            .keepWorldConfig(true)
            .keepGameRule(false)
            .keepWorldBorder(true)
            .saveBukkitWorld(true)

        var cloneSuccess = false
        var cloneError: String? = null

        worldManager.cloneWorld(cloneOptions)
            .onSuccess { newWorld ->
                cloneSuccess = true
                PluginManager.getLogger().info("Мир $worldName успешно склонирован")
            }
            .onFailure { failure ->
                PluginManager.getLogger().severe("Не удалось клонировать мир: ${failure.failureMessage}")
            }

        var world = Bukkit.getWorld(worldName)

        configureArena(world!!)
        arenas[worldName] = world
        return world
    }

    fun deleteArena(worldName: String) {
        val coreApi = MultiverseCoreApi.get();
        val worldManager = coreApi.worldManager

        val mvWorldOption = worldManager.getWorld(worldName)
        if (mvWorldOption.isDefined) {
            try {
                val deleteOptions = DeleteWorldOptions.world(mvWorldOption.get())
                worldManager.deleteWorld(deleteOptions)
                PluginManager.getLogger().info("Арена $worldName удалена через Multiverse")
            } catch (e: Exception) {
                PluginManager.getLogger().warning("Не удалось удалить мир через Multiverse: ${e.message}")
                deleteWorldFolder(worldName)
            }
        }else {
            deleteWorldFolder(worldName)
        }

        arenas.remove(worldName)
    }

    private fun deleteWorldFolder(worldName: String): Boolean {
        val worldFolder = File(Bukkit.getWorldContainer(), worldName)
        if (!worldFolder.exists()) return true

        return worldFolder.deleteRecursively()
    }
}
