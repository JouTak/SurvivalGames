package ru.joutak.sg.arenas

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

    fun getArenas(): Map<String, World> = arenas

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
        mvWorld.setPVPMode(false)
        mvWorld.hunger = false

        world.setGameRule(GameRule.FALL_DAMAGE, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
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
        if (arenas[worldName] == null) {
            PluginManager.getLogger().warning("Не удалось найти и удалить мир $worldName")
        }
        PluginManager.multiverseCore.mvWorldManager.deleteWorld(worldName)
        if (deleteWorldFolder(worldName)) {
            PluginManager.getLogger().info("Удаление арены $worldName прошло успешно!")
        } else {
            PluginManager.getLogger().severe("Не удалось удалить папку с ареной $worldName!")
        }
    }

    fun deleteWorldFolder(worldName: String): Boolean {
        val worldFolder = File(Bukkit.getWorldContainer(), worldName)
        if (!worldFolder.exists()) return true

        return worldFolder.deleteRecursively()
    }
}
