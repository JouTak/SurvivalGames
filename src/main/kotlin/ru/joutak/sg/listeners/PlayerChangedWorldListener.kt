package ru.joutak.blockparty.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import ru.joutak.blockparty.lobby.LobbyReadyBossBar
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.games.GameManager
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.players.PlayerData

object PlayerChangedWorldListener : Listener {
    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player = event.player

        if (event.from.name.equals(LobbyManager.world.name)) {
            LobbyReadyBossBar.removeFor(player)
        }

        if (player.location.world.name
                .equals(LobbyManager.world.name)
        ) {
            LobbyReadyBossBar.setFor(player)
        }

        if (ArenaManager.isArena(event.from)) {
            GameManager.getByPlayer(event.player)?.checkPlayers()
            PlayerData.resetPlayer(event.player.uniqueId)
        }
    }
}
