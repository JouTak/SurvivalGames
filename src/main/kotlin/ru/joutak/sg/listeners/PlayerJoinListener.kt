package ru.joutak.sg.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.players.PlayerData

object PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        PlayerData.resetPlayer(player.uniqueId)
        LobbyManager.teleportToLobby(player)
    }
}
