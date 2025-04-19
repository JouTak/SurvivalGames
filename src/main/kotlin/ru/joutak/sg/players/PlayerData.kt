package ru.joutak.sg.players

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.utils.PluginManager
import java.io.File
import java.util.UUID

data class PlayerData(
    val nickname: String,
) {
    private var isReady: Boolean = false
    private var timeSetReady: Long = Long.MAX_VALUE

    private val file = File(dataFolder, "${this.nickname}.yml")
    private val playerData: YamlConfiguration

    companion object {
        private var dataFolder = File(PluginManager.getDataFolder(), "players")
        private val playerDatas = mutableMapOf<UUID, PlayerData>()

        init {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs()
            }
        }

        fun get(player: Player): PlayerData {
            if (!playerDatas.containsKey(player.uniqueId)) {
                playerDatas[player.uniqueId] = PlayerData(player.name)
            }

            return playerDatas[player.uniqueId]!!
        }

        fun reload() {
            playerDatas.clear()
            dataFolder = File(PluginManager.getDataFolder(), "players")
        }
    }

    init {
        if (file.createNewFile()) {
            playerData = YamlConfiguration()
            playerData.set("nickname", nickname)
            playerData.set("playerUuid", Bukkit.getOfflinePlayer(nickname).uniqueId.toString())
            // playerData.set("games", emptyList<String>())
            // playerData.set("maxRounds", 0)
            // playerData.set("hasWon", false)
            // playerData.set("hasBalls", false)
            playerData.save(file)
        } else {
            playerData = YamlConfiguration.loadConfiguration(file)
            for (game in playerData.get("games") as List<String>) {
                // games.add(UUID.fromString(game))
            }
            // hasWon = playerData.get("hasWon") as Boolean
        }
    }

    fun isInLobby(): Boolean =
        Bukkit
            .getPlayer(nickname)
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
}
