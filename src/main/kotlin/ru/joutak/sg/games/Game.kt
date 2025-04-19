package ru.joutak.sg.games

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.players.PlayerData
import ru.joutak.sg.utils.PluginManager
import java.util.UUID

class Game(
    val arena: World,
    private val players: Iterable<Player>,
) : Runnable {
    val uuid = UUID.randomUUID()
    private val onlinePlayers: MutableSet<Player> = mutableSetOf()

    fun start() {
        ArenaManager.configureArena(arena)

        for (player in players) {
            val playerData = PlayerData.get(player)
            // playerData.addGame(this.uuid)
            onlinePlayers.add(player)
            PluginManager.multiverseCore.teleportPlayer(Bukkit.getConsoleSender(), player, arena.spawnLocation)
            player.gameMode = GameMode.SURVIVAL
        }
    }

    override fun run() {
    }
}
