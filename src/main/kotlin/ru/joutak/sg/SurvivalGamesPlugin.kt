package ru.joutak.sg

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.commands.ConfigCommand
import ru.joutak.sg.commands.ReadyCommand
import ru.joutak.sg.config.Config
import ru.joutak.sg.listeners.PlayerJoinListener
import ru.joutak.sg.lobby.LobbyManager

class SurvivalGamesPlugin : JavaPlugin() {
    companion object {
        @JvmStatic
        lateinit var instance: SurvivalGamesPlugin
        val TITLE =
            LinearComponents.linear(
                Component.text("S", NamedTextColor.DARK_RED),
                Component.text("u", NamedTextColor.RED),
                Component.text("r", NamedTextColor.GOLD),
                Component.text("v", NamedTextColor.YELLOW),
                Component.text("i", NamedTextColor.GOLD),
                Component.text("v", NamedTextColor.RED),
                Component.text("a", NamedTextColor.GOLD),
                Component.text("l", NamedTextColor.DARK_RED),
                Component.space(),
                Component.text("G", NamedTextColor.RED),
                Component.text("a", NamedTextColor.GOLD),
                Component.text("m", NamedTextColor.YELLOW),
                Component.text("e", NamedTextColor.GOLD),
                Component.text("s", NamedTextColor.DARK_RED),
            )
    }

    /**
     * Plugin startup logic
     */
    override fun onEnable() {
        instance = this

        loadData()
        registerEvents()
        registerCommands()
        LobbyManager.configure()

        logger.info("Плагин ${pluginMeta.name} версии ${pluginMeta.version} включен!")
    }

    private fun loadData() {
        ArenaManager.setTemplate()
        Config.loadConfig()
    }

    private fun registerEvents() {
        val manager = Bukkit.getPluginManager()
        manager.registerEvents(PlayerJoinListener, instance)
    }

    private fun registerCommands() {
        getCommand("ready")?.setExecutor(ReadyCommand)
        getCommand("sgconfig")?.setExecutor(ConfigCommand)
    }

    /**
     * Plugin shutdown logic
     */
    override fun onDisable() {
    }
}
