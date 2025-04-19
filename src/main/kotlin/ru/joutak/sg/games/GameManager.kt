package ru.joutak.sg.games

import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.lobby.LobbyManager
import java.util.UUID

object GameManager {
    private val games = mutableMapOf<UUID, Game>()

    fun createNewGame(): Game {
        val arena = ArenaManager.cloneArena()
        val players =
            LobbyManager.getReadyPlayers().take(
                minOf(LobbyManager.getReadyPlayers().count(), Config.get(ConfigKeys.MAX_PLAYERS_IN_GAME)),
            )
        val game = Game(arena, players)

        games[game.uuid] = game

        return game
    }
}
