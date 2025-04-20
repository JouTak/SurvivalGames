package ru.joutak.sg.players

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.games.SpartakiadaManager
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.utils.PluginManager
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class PlayerData(
    val playerUuid: UUID,
    private val games: MutableList<UUID> = mutableListOf(),
) {
    private val dataFolder: File by lazy {
        val root =
            if (Config.get(ConfigKeys.SPARTAKIADA_MODE)) {
                SpartakiadaManager.spartakiadaFolder
            } else {
                PluginManager.survivalGames.dataFolder
            }

        File(root, "players").apply { mkdirs() }
    }

    companion object {
        private val cache = ConcurrentHashMap<UUID, PlayerData>()

        fun get(uuid: UUID) = cache.getOrPut(uuid) { PlayerData(uuid) }

        fun reloadDatas() = cache.clear()

        fun resetPlayer(playerUuid: UUID) {
            val player = Bukkit.getPlayer(playerUuid) ?: return
            player.health = 20.0
            player.foodLevel = 20
            player.inventory.clear()
            player.level = 0
            player.exp = 0.0f
            player.fireTicks = 0
        }
    }

    private val file = File(dataFolder, "${this.playerUuid}.yml")
    private val yaml: YamlConfiguration
    private var isReady: Boolean = false
    private var timeSetReady: Long = Long.MAX_VALUE

    init {
        if (file.createNewFile()) {
            yaml = YamlConfiguration()
            yaml.set("nickname", Bukkit.getOfflinePlayer(playerUuid).name)
            yaml.set("playerUuid", playerUuid.toString())
            yaml.set("games", emptyList<String>())
            // playerData.set("maxRounds", 0)
            // playerData.set("hasWon", false)
            // playerData.set("hasBalls", false)
            yaml.save(file)
        } else {
            yaml = YamlConfiguration.loadConfiguration(file)
            for (game in yaml.getStringList("games")) {
                games.add(UUID.fromString(game))
            }
            yaml.getLong("test")
            // hasWon = playerData.get("hasWon") as Boolean
        }
    }

    fun isInLobby(): Boolean =
        Bukkit
            .getPlayer(playerUuid)
            ?.world
            ?.name
            .equals(LobbyManager.world.name)

    fun isReady() = isReady

    fun setReady(isReady: Boolean) {
        this.isReady = isReady

        if (isReady) {
            timeSetReady = System.currentTimeMillis()
        } else {
            timeSetReady = Long.MAX_VALUE
        }
    }

    fun getTimeSetReady(): Long = timeSetReady

    fun addGame(gameUuid: UUID) {
        games.add(gameUuid)
        yaml.set("games", games.map { it.toString() })
        yaml.save(file)
    }

    fun getGames(): List<UUID> = games

    fun save() {
        try {
            yaml.set("nickname", Bukkit.getOfflinePlayer(playerUuid).name)
            yaml.save(file)
        } catch (e: IOException) {
            PluginManager.getLogger().severe("Ошибка при сохранении информации о игроке: ${e.message}")
        } finally {
            cache.remove(playerUuid)
        }
    }
}
