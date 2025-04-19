package ru.joutak.sg.config

object ConfigKeys {
    val ARENA_WORLD_NAME = string("ARENA_WORLD_NAME", "arena")
    val LOBBY_WORLD_NAME = string("LOBBY_WORLD_NAME", "lobby")
    val LOG_INFO_TO_CONSOLE = boolean("LOG_INFO_TO_CONSOLE", false)
    val MAX_PLAYERS_IN_GAME = int("MAX_PLAYERS_IN_GAME", 20)
    val PLAYERS_TO_START = int("PLAYERS_TO_START", 20)
    val PLAYERS_TO_END = int("PLAYERS_TO_END", 1)
    val SPARTAKIADA_MODE = boolean("SPARTAKIADA_MODE", false)
    val TIME_TO_START_GAME_LOBBY = int("TIME_TO_START_GAME_LOBBY", 15)

    val all =
        setOf(
            ARENA_WORLD_NAME,
            LOBBY_WORLD_NAME,
            LOG_INFO_TO_CONSOLE,
            MAX_PLAYERS_IN_GAME,
            PLAYERS_TO_START,
            PLAYERS_TO_END,
            SPARTAKIADA_MODE,
            TIME_TO_START_GAME_LOBBY,
        )

    private fun int(
        path: String,
        default: Int,
    ) = object : ConfigKey<Int> {
        override val path = path
        override val value = default

        override fun parse(input: String) = input.toIntOrNull()
    }

    private fun boolean(
        path: String,
        default: Boolean,
    ) = object : ConfigKey<Boolean> {
        override val path = path
        override val value = default

        override fun parse(input: String): Boolean? =
            when (input.lowercase()) {
                "true", "yes", "1" -> true
                "false", "no", "0" -> false
                else -> null
            }
    }

    private fun string(
        path: String,
        default: String,
    ) = object : ConfigKey<String> {
        override val path = path
        override val value = default

        override fun parse(input: String) = input
    }
}
