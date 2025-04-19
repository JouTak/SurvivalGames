package ru.joutak.sg.config

import org.bukkit.configuration.file.YamlConfiguration
import ru.joutak.sg.utils.PluginManager
import java.io.File

object Config {
    private val configFile = File(PluginManager.survivalGames.dataFolder, "config.yml")
    private val config: YamlConfiguration

    init {
        PluginManager.getLogger().info("Загрузка значений из конфига...")
        if (!configFile.exists()) {
            PluginManager.survivalGames.saveResource("config.yml", true)
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    fun loadConfig() {
        for (key in ConfigKeys.all) {
            if (!config.contains(key.path)) {
                PluginManager
                    .getLogger()
                    .warning("Не найден ключ ${key.path} в конфиге! Взято стандартное значение: ${key.value}")
                config.set(key.path, key.value)
            }
        }
        config.save(configFile)
    }

    fun <T : Any> get(key: ConfigKey<T>): T {
        val value = config.get(key.path)
        return if (key.value::class.java.isInstance(value)) {
            value as T
        } else {
            key.value
        }
    }

    fun <T : Any> set(
        key: ConfigKey<T>,
        value: T,
    ) {
        config.set(key.path, value)
        config.save(configFile)
    }
}
