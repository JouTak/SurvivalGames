package ru.joutak.sg

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ru.joutak.blockparty.listeners.PlayerChangedWorldListener
import ru.joutak.blockparty.lobby.LobbyReadyBossBar
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.arenas.SpawnManager
import ru.joutak.sg.commands.AddSpawnCommand
import ru.joutak.sg.commands.ConfigCommand
import ru.joutak.sg.commands.ReadyCommand
import ru.joutak.sg.commands.RemoveSpawnCommand
import ru.joutak.sg.commands.SpawnListCommand
import ru.joutak.sg.commands.SpectateCommand
import ru.joutak.sg.games.SpartakiadaManager
import ru.joutak.sg.listeners.ChestListener
import ru.joutak.sg.listeners.PlayerDamageListener
import ru.joutak.sg.listeners.PlayerJoinListener
import ru.joutak.sg.listeners.PlayerLoginListener
import ru.joutak.sg.listeners.PlayerQuitListener
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.loot.LootManager
import ru.joutak.sg.players.PlayerData

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
        LobbyReadyBossBar.removeAllBossBars()
        LobbyReadyBossBar.checkLobby()
        SpartakiadaManager.watchParticipantsChanges()

        logger.info("Плагин ${pluginMeta.name} версии ${pluginMeta.version} включен!")
    }

    private fun loadData() {
        ArenaManager.setTemplate()
        ArenaManager.deleteExistingArenas()
        SpawnManager.loadSpawns()
        LootManager.loadLoot()
        SpartakiadaManager.reload()
    }

    private fun registerEvents() {
        val manager = Bukkit.getPluginManager()
        manager.registerEvents(PlayerJoinListener, instance)
        manager.registerEvents(ChestListener, instance)
        manager.registerEvents(PlayerQuitListener, instance)
        manager.registerEvents(PlayerDamageListener, instance)
        manager.registerEvents(PlayerChangedWorldListener, instance)
        manager.registerEvents(PlayerLoginListener, instance)
    }

    private fun registerCommands() {
        getCommand("ready")?.setExecutor(ReadyCommand)
        getCommand("sgaddspawn")?.setExecutor(AddSpawnCommand)
        getCommand("sgconfig")?.setExecutor(ConfigCommand)
        getCommand("sgremovespawn")?.setExecutor(RemoveSpawnCommand)
        getCommand("sgspawnlist")?.setExecutor(SpawnListCommand)
        getCommand("sgspectate")?.setExecutor(SpectateCommand)
    }

    /**
     * Plugin shutdown logic
     */
    override fun onDisable() {
        SpartakiadaManager.stopWatching()
        SpawnManager.saveSpawns()
        for (player in Bukkit.getOnlinePlayers()) {
            PlayerData.get(player.uniqueId).save()
        }
    }
}
