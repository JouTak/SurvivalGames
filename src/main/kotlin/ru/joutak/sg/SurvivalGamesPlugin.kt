package ru.joutak.sg

import org.bukkit.plugin.java.JavaPlugin

class SurvivalGamesPlugin : JavaPlugin() {
    companion object {
        @JvmStatic
        lateinit var instance: SurvivalGamesPlugin
    }

    /**
     * Plugin startup logic
     */
    override fun onEnable() {
        instance = this

        logger.info("Плагин ${pluginMeta.name} версии ${pluginMeta.version} включен!")
    }

    /**
     * Plugin shutdown logic
     */
    override fun onDisable() {
    }
}
