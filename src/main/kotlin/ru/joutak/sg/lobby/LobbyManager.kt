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
import ru.joutak.blockparty.lobby.LobbyReadyBossBar
import ru.joutak.minigames.managers.MatchmakingManager
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.utils.PluginManager

object LobbyManager {
    val world: World

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
        player.gameMode = GameMode.ADVENTURE
        LobbyReadyBossBar.setFor(player)
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
        LobbyReadyBossBar.checkLobby()
    }

    fun resetTask() {
        // Countdown is managed by MiniGamesAPI's MatchmakingManager.
    }

    fun getPlayers(): Iterable<Player> = world.players

    fun getReadyPlayers(): Iterable<Player> =
        world.players.filter { MatchmakingManager.isPlayerInAnyInstance(it.uniqueId) }

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

}
