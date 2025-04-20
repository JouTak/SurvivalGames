package ru.joutak.sg.games

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import ru.joutak.blockparty.lobby.LobbyReadyBossBar
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.arenas.SpawnManager
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.listeners.ChestListener
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.players.PlayerData
import ru.joutak.sg.utils.PluginManager
import java.util.Stack
import java.util.UUID

class Game(
    val arena: World,
    private val players: Iterable<UUID>,
) : Runnable {
    val uuid = UUID.randomUUID()
    private val logger = GameLogger(this)
    private val scoreboard = GameScoreboard()
    private val onlinePlayers: MutableSet<UUID> = mutableSetOf()
    private val spectators: MutableSet<UUID> = mutableSetOf()
    private val places: Stack<UUID> = Stack<UUID>()
    private val spawnManager: SpawnManager = SpawnManager()
    private var phase = GamePhase.STARTING
    private var timeLeft = 0
    private var gameTaskId: Int? = null
    private var borderTasks: MutableSet<Int> = mutableSetOf()

    fun prepare() {
        ArenaManager.configureArena(arena)
        phase = GamePhase.STARTING

        for (playerUuid in players) {
            val playerData = PlayerData.get(playerUuid)
            playerData.addGame(this.uuid)
            playerData.setReady(false)
            PlayerData.resetPlayer(playerUuid)
            onlinePlayers.add(playerUuid)
            Bukkit.getPlayer(playerUuid)?.let {
                PluginManager.multiverseCore.teleportPlayer(Bukkit.getConsoleSender(), it, spawnManager.getNextSpawn(arena))
                scoreboard.setFor(it)
                Bukkit.getScheduler().runTaskLater(
                    PluginManager.survivalGames,
                    Runnable {
                        it.gameMode = GameMode.ADVENTURE
                    },
                    5L,
                )
            }
        }

        LobbyManager.resetTask()
        LobbyReadyBossBar.checkLobby()

        logger.info(
            "Игра началась в составе из ${players.count()} игроков:\n${players.map {
                "$it ${Bukkit.getOfflinePlayer(
                    it,
                ).name}"
            }.joinToString("\n")}",
        )
        timeLeft = Config.get(ConfigKeys.TIME_BETWEEN_PHASES)

        gameTaskId =
            Bukkit.getScheduler().scheduleSyncRepeatingTask(
                PluginManager.survivalGames,
                this,
                0L,
                20L,
            )

        borderTasks.add(
            Bukkit
                .getScheduler()
                .runTaskLater(
                    PluginManager.survivalGames,
                    Runnable {
                        Audience
                            .audience(
                                getAllPlayers().mapNotNull { Bukkit.getPlayer(it) },
                            ).sendMessage(Component.text("Внимание, границы начали сужаться!"))
                        arena.worldBorder.setSize(320.0, 3 * 60)
                    },
                    Config.get(ConfigKeys.FIRST_BORDER_TIME) * 20L,
                ).taskId,
        )

        borderTasks.add(
            Bukkit
                .getScheduler()
                .runTaskLater(
                    PluginManager.survivalGames,
                    Runnable {
                        Audience
                            .audience(
                                getAllPlayers().mapNotNull { Bukkit.getPlayer(it) },
                            ).sendMessage(Component.text("Внимание, границы начали сужаться!"))
                        arena.worldBorder.setSize(80.0, 3 * 60)
                    },
                    Config.get(ConfigKeys.SECOND_BORDER_TIME) * 20L,
                ).taskId,
        )

        borderTasks.add(
            Bukkit
                .getScheduler()
                .runTaskLater(
                    PluginManager.survivalGames,
                    Runnable {
                        Audience
                            .audience(
                                getAllPlayers().mapNotNull { Bukkit.getPlayer(it) },
                            ).sendMessage(Component.text("Внимание, границы начали сужаться!"))
                        arena.worldBorder.setSize(30.0, 3 * 60)
                    },
                    Config.get(ConfigKeys.THIRD_BORDER_TIME) * 20L,
                ).taskId,
        )

        Bukkit.getScheduler().runTaskLater(
            PluginManager.survivalGames,
            Runnable {
                // if (bossbars["pvp"]!! > 0) {
                // bossbars["pvp"] = bossbars["pvp"]!! - 1
                // scoreboard.setBossBarTimer(getAllPlayers(), "Режим PVP", bossbars["pvp"]!!, Config.get(ConfigKeys.PVP_COOLDOWN))
                // }

                arena.pvp = true
                Audience.audience(getAllPlayers().map { Bukkit.getPlayer(it) }).showTitle(
                    Title.title(
                        LinearComponents.linear(
                            Component.text("PvP", NamedTextColor.RED, TextDecoration.BOLD),
                            Component.text(" включен!"),
                        ),
                        LinearComponents.linear(Component.text("☠ Расчехляйте оружие ☠")),
                    ),
                )
                // scoreboard.removeBossBar(getAllPlayers())
            },
            Config.get(ConfigKeys.PVP_COOLDOWN) * 20L,
        )
    }

    override fun run() {
        scoreboard.update(numberOfRemainingPlayers())

        when (phase) {
            GamePhase.STARTING -> start()
            GamePhase.PLAYING -> checkPlayers()
            GamePhase.ENDING -> end()
        }
    }

    fun start() {
        if (timeLeft > 0) {
            if (timeLeft % 5 == 0) {
                Audience.audience(getAllPlayers().mapNotNull { Bukkit.getPlayer(it) }).sendMessage(
                    LinearComponents.linear(
                        Component.text("Игра начнется через "),
                        Component.text("$timeLeft", NamedTextColor.RED),
                        Component.text(" секунд!"),
                    ),
                )
            }
            if (timeLeft <= 3) {
                Audience.audience(getAllPlayers().mapNotNull { Bukkit.getPlayer(it) }).showTitle(
                    Title.title(
                        LinearComponents.linear(
                            Component.text("$timeLeft", NamedTextColor.RED, TextDecoration.BOLD),
                        ),
                        LinearComponents.linear(),
                    ),
                )
            }

            timeLeft--
            return
        }
        logger.info("Игра началась!")

        Audience.audience(getAllPlayers().mapNotNull { Bukkit.getPlayer(it) }).also {
            it.clearTitle()
            it.sendMessage(Component.text("Да начнутся же игры на выживание!"))
        }

        for (playerUuid in onlinePlayers) {
            Bukkit.getPlayer(playerUuid)?.let {
                it.gameMode = GameMode.SURVIVAL
            }
        }
        spawnManager.removeBarriers()
        phase = GamePhase.PLAYING
    }

    fun play() {
        if (numberOfRemainingPlayers() <= 1) {
            timeLeft = Config.get(ConfigKeys.TIME_BETWEEN_PHASES)
            phase = GamePhase.ENDING
            return
        }
    }

    fun end() {
        borderTasks.forEach { Bukkit.getScheduler().cancelTask(it) }

        if (timeLeft > 0) {
            // arena.launchFireworksAtCorners()
            timeLeft--
            return
        }
        if (gameTaskId != null) {
            Bukkit.getScheduler().cancelTask(gameTaskId!!)
        }
        logger.saveGameResults()

        for (playerUuid in getAllPlayers()) {
            PlayerData.resetPlayer(playerUuid)

            Bukkit.getPlayer(playerUuid)?.let {
                scoreboard.removeFor(it)
                LobbyManager.teleportToLobby(it)
            }
        }

        logger.info("Игра завершилась")
        ChestListener.clearLootedChests(arena)
        GameManager.remove(this.uuid)
        logger.close()
        LobbyManager.checkPlayers()
        ArenaManager.deleteArena(arena.name)
    }

    fun kill(player: Player) {
        val inventory = player.inventory
        val loc = player.location

        player.gameMode = GameMode.SPECTATOR
        player.health = 20.0
        player.foodLevel = 20
        player.fireTicks = 0

        for (item in inventory.contents) {
            if (item == null) continue
            loc.world.dropItemNaturally(loc, item)
        }

        for (item in inventory.armorContents) {
            if (item == null) continue
            loc.world.dropItemNaturally(loc, item)
        }

        player.inventory.clear()

        arena.strikeLightningEffect(loc)
        knockout(player.uniqueId)
    }

    fun knockout(playerUuid: UUID) {
        logger.info("Игрок $playerUuid выбыл из игры! Место: ${players.count() - places.count()}/${players.count()}")
        if (!places.contains(playerUuid)) {
            places.push(playerUuid)
        }

        Audience
            .audience(
                getAllPlayers().mapNotNull {
                    Bukkit.getPlayer(it)
                },
            ).sendMessage(Component.text("Игрок ${Bukkit.getOfflinePlayer(playerUuid).name} выбыл из игры!"))

        val player = Bukkit.getPlayer(playerUuid)
        if (player != null) {
            player.gameMode = GameMode.SPECTATOR

            Audience.audience(player).showTitle(
                Title.title(
                    LinearComponents.linear(
                        Component.text("Вы проиграли! :(", NamedTextColor.RED),
                    ),
                    LinearComponents.linear(Component.text("Место: ${players.count() - places.count() + 1}/${players.count()}")),
                ),
            )
        }

        if (getPhase() != GamePhase.ENDING) {
            checkPlayers()
        }
    }

    fun checkPlayers() {
        if (getPhase() == GamePhase.ENDING) return

        for (playerUuid in onlinePlayers.filter {
            !Bukkit
                .getPlayer(it)
                ?.world
                ?.name
                .equals(arena.name)
        }) {
            logger.info("Игрок $playerUuid вышел из игры!")
            Bukkit.getPlayer(playerUuid)?.let {
                scoreboard.removeFor(it)
            }
            onlinePlayers.remove(playerUuid)
        }

        if (numberOfRemainingPlayers() <= 1) {
            arena.worldBorder.reset()
            for (playerUuid in onlinePlayers) {
                if (!places.contains(playerUuid)) places.push(playerUuid)
            }
            arena.pvp = false
            teleportWinners()
            val winner = Bukkit.getPlayer(places.peek())
            if (winner != null) {
                Audience.audience(winner).showTitle(
                    Title.title(
                        LinearComponents.linear(
                            Component.text("Вы победили! :)", NamedTextColor.GREEN),
                        ),
                        LinearComponents.linear(),
                    ),
                )

                Audience.audience(Bukkit.getServer().onlinePlayers).sendMessage(
                    LinearComponents.linear(
                        Component.text("Победителем очередной игры в "),
                        SurvivalGamesPlugin.TITLE,
                        Component.text(" стал:\n"),
                        Component.text(
                            winner.name,
                            NamedTextColor.WHITE,
                            TextDecoration.BOLD,
                        ),
                    ),
                )
            }

            logger.info("Победителем стал: ${places.peek()}")

            phase = GamePhase.ENDING
            timeLeft = Config.get(ConfigKeys.TIME_BETWEEN_PHASES)
        }
    }

    private fun teleportWinners() {
        val gold = ArenaManager.GOLD_PEDESTAL.toLocation(arena)
        val silver = ArenaManager.SILVER_PEDESTAL.toLocation(arena)
        val bronze = ArenaManager.BRONZE_PEDESTAL.toLocation(arena)
        val restSpawn = ArenaManager.PEDESTALS.toLocation(arena)

        val list = places.toList().asReversed()

        for (playerUuid in onlinePlayers) {
            Bukkit.getPlayer(playerUuid)?.let {
                scoreboard.removeFor(it)
                PlayerData.resetPlayer(it.uniqueId)
            }
        }

        list.getOrNull(0)?.let {
            if (getAllPlayers().contains(it)) {
                Bukkit.getPlayer(it)?.apply {
                    teleport(gold)
                    gameMode = GameMode.ADVENTURE
                }
            }
        }

        list.getOrNull(1)?.let {
            if (getAllPlayers().contains(it)) {
                Bukkit.getPlayer(it)?.apply {
                    teleport(silver)
                    gameMode = GameMode.ADVENTURE
                }
            }
        }

        list.getOrNull(2)?.let {
            if (getAllPlayers().contains(it)) {
                Bukkit.getPlayer(it)?.apply {
                    teleport(bronze)
                    gameMode = GameMode.ADVENTURE
                }
            }
        }

        for (i in 3 until list.size) {
            if (getAllPlayers().contains(list[i])) {
                Bukkit.getPlayer(list[i])?.apply {
                    teleport(restSpawn)
                    gameMode = GameMode.SPECTATOR
                }
            }
        }
    }

    fun addSpectator(player: Player) {
        spectators.add(player.uniqueId)
        player.teleport(arena.spawnLocation)
        player.gameMode = GameMode.SPECTATOR
        scoreboard.setFor(player)

        player.sendMessage("Вы наблюдаете за игрой на арене ${arena.name}.")
    }

    fun removeSpectator(player: Player) {
        spectators.remove(player.uniqueId)
        scoreboard.removeFor(player)
    }

    fun hasSpectator(player: Player): Boolean = spectators.contains(player.uniqueId)

    fun hasPlayer(player: Player): Boolean = onlinePlayers.contains(player.uniqueId)

    fun numberOfRemainingPlayers(): Int = (players.count() - places.count())

    fun getAllPlayers(): Iterable<UUID> = (onlinePlayers + spectators).toSet()

    fun getPhase(): GamePhase = this.phase

    fun serialize(): Map<String, Any> {
        // Расположение игроков по местам (1, 2, 3, ...)
        val placementMap = mutableMapOf<String, List<String>>()
        places.toList().asReversed().forEachIndexed { index, uuid ->
            val player = Bukkit.getOfflinePlayer(uuid)
            placementMap[(index + 1).toString()] = listOf(player.name ?: "Unknown", uuid.toString())
        }

        return mapOf(
            "gameUuid" to this.uuid.toString(),
            "arena" to this.arena.name,
            "players" to this.players.map { it.toString() },
            "placement" to placementMap,
        )
    }
}
