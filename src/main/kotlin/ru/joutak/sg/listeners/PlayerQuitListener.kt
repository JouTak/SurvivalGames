package ru.joutak.sg.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import ru.joutak.sg.games.GameManager
import ru.joutak.sg.games.GamePhase
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.players.PlayerData
import ru.joutak.sg.utils.PluginManager

object PlayerQuitListener : Listener {
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val lastGame = GameManager.getByPlayer(player)

        Bukkit.getScheduler().runTaskLater(
            PluginManager.survivalGames,
            Runnable {
                if (lastGame != null && lastGame.getPhase() != GamePhase.ENDING) {
                    PlayerData.resetPlayer(player.uniqueId)
                    lastGame.knockout(player.uniqueId)
                    lastGame.checkPlayers()
                }
                PlayerData.get(player.uniqueId).save()
                LobbyManager.checkPlayers()
            },
            5L,
        )
    }
}
