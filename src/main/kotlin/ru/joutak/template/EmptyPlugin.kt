package com.joutak.template

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class EmptyPlugin : JavaPlugin() {
    companion object {
        @JvmStatic
        lateinit var instance: EmptyPlugin
    }

    private var customConfig = YamlConfiguration()

    private fun loadConfig() {
        val fx = File(dataFolder, "config.yml")
        if (!fx.exists()) {
            saveResource("config.yml", true)
        }
    }

    override fun onEnable() {
        // Plugin startup logic
        instance = this

        loadConfig()

        // Register commands and events

        logger.info("Template plugin version ${pluginMeta.version} enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
