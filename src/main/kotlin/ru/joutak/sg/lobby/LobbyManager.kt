package ru.joutak.sg.lobby

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.entity.Player
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.games.GameManager
import ru.joutak.sg.players.PlayerData
import ru.joutak.sg.utils.PluginManager

object LobbyManager {
    val world: World
    private var gameStartTask: Int? = null
    private var timeLeft: Int = 0

    init {
        if (Bukkit.getWorld(Config.get(ConfigKeys.LOBBY_WORLD_NAME)) == null) {
            world = Bukkit.getWorlds()[0]
            PluginManager.getLogger().warning(
                "Отсутствует мир ${Config.get(ConfigKeys.LOBBY_WORLD_NAME)}! В качестве лобби используется мир ${world.name}.",
            )
        } else {
            world = Bukkit.getWorld(Config.get(ConfigKeys.LOBBY_WORLD_NAME))!!
        }
    }

    fun teleportToLobby(player: Player) {
        PluginManager.multiverseCore.teleportPlayer(
            Bukkit.getConsoleSender(),
            player,
            PluginManager.multiverseCore.mvWorldManager
                .getMVWorld(world.name)
                .spawnLocation,
        )

        val audience = Audience.audience(player)
        audience.sendMessage(
            LinearComponents.linear(
                Component.text("Для игры в "),
                SurvivalGamesPlugin.TITLE,
                Component.text(" введите команду "),
                Component.text("/ready", NamedTextColor.RED, TextDecoration.BOLD),
            ),
        )
    }

    fun checkPlayers() {
        val readyCount = world.players.count { PlayerData.get(it).isReady() }

        if (readyCount >= Config.get(ConfigKeys.PLAYERS_TO_START) && gameStartTask == null) {
            startCountdown()
            return
        }

        if (getReadyPlayers().count() < Config.get(ConfigKeys.PLAYERS_TO_START)) {
            if (gameStartTask != null) {
                Audience.audience(getReadyPlayers()).sendMessage(
                    LinearComponents.linear(
                        Component.text("Недостаточно игроков для начала игры!"),
                    ),
                )
                resetTask()
            }

            Audience.audience(world.players).sendMessage(
                LinearComponents.linear(
                    Component.text("Ожидание "),
                    Component.text(
                        "${Config.get(ConfigKeys.PLAYERS_TO_START) - getReadyPlayers().count()}",
                        NamedTextColor.GOLD,
                    ),
                    Component.text(" игроков для начала игры."),
                ),
            )
        }
    }

    fun startCountdown() {
        timeLeft = Config.get(ConfigKeys.TIME_TO_START_GAME_LOBBY)

        gameStartTask =
            Bukkit
                .getScheduler()
                .runTaskTimer(
                    PluginManager.survivalGames,
                    Runnable {
                        if (timeLeft > 0) {
                            if (timeLeft % 5 == 0 || timeLeft <= 3) {
                                Audience.audience(getReadyPlayers()).sendMessage(
                                    LinearComponents.linear(
                                        Component.text("Ваша игра начнется через "),
                                        Component.text("$timeLeft", NamedTextColor.RED),
                                        Component.text(" секунд!"),
                                    ),
                                )

                                Audience.audience(getNonReadyPlayers()).sendMessage(
                                    LinearComponents.linear(
                                        Component.text("Следующая игра начнется через "),
                                        Component.text("$timeLeft", NamedTextColor.RED),
                                        Component.text(" секунд, успейте присоединиться!"),
                                    ),
                                )
                            }
                            timeLeft--
                        } else {
                            GameManager.createNewGame().start()
                            resetTask()
                        }
                    },
                    0L,
                    20L,
                ).taskId
    }

    fun getReadyPlayers(): Iterable<Player> =
        world.players
            .filter {
                PlayerData.get(it).isReady()
            }.sortedBy { PlayerData.get(it).getTimeSetReady() }

    fun getNonReadyPlayers(): Iterable<Player> = world.players.filter { !PlayerData.get(it).isReady() }

    fun configure() {
        val mvWorld = PluginManager.multiverseCore.mvWorldManager.getMVWorld(world)
        PluginManager.multiverseCore.mvWorldManager.setFirstSpawnWorld(world.name)
        mvWorld.setTime("day")
        mvWorld.setEnableWeather(false)
        mvWorld.setDifficulty(Difficulty.PEACEFUL)
        mvWorld.setGameMode(GameMode.ADVENTURE)
        mvWorld.setPVPMode(false)
        mvWorld.setHunger(false)

        world.setGameRule(GameRule.FALL_DAMAGE, false)
        world.setGameRule(GameRule.DROWNING_DAMAGE, false)
        world.setGameRule(GameRule.FIRE_DAMAGE, false)
        world.setGameRule(GameRule.FREEZE_DAMAGE, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
    }

    fun resetTask() {
        if (gameStartTask != null) {
            Bukkit.getScheduler().cancelTask(gameStartTask!!)
        }
        gameStartTask = null
    }
}
