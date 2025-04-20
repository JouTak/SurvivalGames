package ru.joutak.sg.games

import org.bukkit.World
import org.bukkit.entity.Player
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
        val game = Game(arena, players.map { it.uniqueId })

        games[game.uuid] = game
        LobbyManager.resetTask()

        return game
    }

    fun getByArena(arena: World): Game? {
        for (game in games.values) {
            if (game.arena == arena) {
                return game
            }
        }
        return null
    }

    fun getByPlayer(player: Player): Game? {
        for (game in games.values) {
            if (game.hasPlayer(player)) {
                return game
            }
        }
        return null
    }

    fun getBySpectator(spectator: Player): Iterable<Game> {
        val result = mutableListOf<Game>()
        for (game in games.values) {
            if (game.hasSpectator(spectator)) {
                result.add(game)
            }
        }
        return result
    }

    fun isPlaying(player: Player): Boolean = getByPlayer(player) != null

    fun remove(gameUuid: UUID) {
        games.remove(gameUuid)
    }
}
