package ru.joutak.sg

import org.bukkit.plugin.java.JavaPlugin
import ru.joutak.sg.commands.ConfigCommand
import ru.joutak.sg.config.Config

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

        loadData()
        registerCommands()
        logger.info("Плагин ${pluginMeta.name} версии ${pluginMeta.version} включен!")
    }

    private fun loadData() {
        Config.loadConfig()
    }
    private fun registerCommands() {
        getCommand("sgconfig")?.setExecutor(ConfigCommand)
    }
    /**
     * Plugin shutdown logic
     */
    override fun onDisable() {
    }
}
