package ru.joutak.sg.games

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.players.PlayerData
import ru.joutak.sg.utils.PluginManager
import java.io.File

object SpartakiadaManager {
    val spartakiadaFolder = File(PluginManager.survivalGames.dataFolder, "spartakiada")
    private val participantsFile = File(PluginManager.getDataFolder(), "participants.yml")

    private var watchThread: Thread? = null
    private val participants = mutableSetOf<String>()

    val KICK_NON_PARTICIPANT_MESSAGE =
        LinearComponents.linear(
            Component.text("☒", NamedTextColor.RED),
            Component.text(" Вы "),
            Component.text("не являетесь", NamedTextColor.RED),
            Component.text(" участником спартакиады!"),
        )

    val KICK_NO_ATTEMPTS_MESSAGE =
        LinearComponents.linear(
            Component.text("К сожалению, для вас cпартакиада "),
            Component.text("закончилась ", NamedTextColor.RED),
            Component.text("☹"),
        )

    val KICK_WINNER_MESSAGE =
        LinearComponents.linear(
            Component.text("☑", NamedTextColor.GREEN),
            Component.text(" Вы уже "),
            Component.text("прошли", NamedTextColor.GREEN),
            Component.text(" в следующий этап!"),
        )

    init {
        if (!participantsFile.exists()) {
            PluginManager.survivalGames.saveResource("participants.yml", true)
        }
        loadParticipants()
    }

    private fun loadParticipants() {
        val config = YamlConfiguration.loadConfiguration(participantsFile)
        participants.clear()
        config.getStringList("participants").forEach { name ->
            participants.add(name)
        }
        PluginManager.getLogger().info("${participants.joinToString("\n")}")
    }

    fun isParticipant(player: Player): Boolean {
        loadParticipants()

        return participants.contains(player.name)
    }

    fun hasAttempts(player: Player): Boolean {
        val attempts = getRemainingAttempts(player)
        return attempts > 0
    }

    fun getRemainingAttempts(player: Player): Int =
        Config.get(ConfigKeys.SPARTAKIADA_ATTEMPTS) - PlayerData.get(player.uniqueId).getGames().size

    fun reload() {
        loadParticipants()
        checkPlayers()
    }

    fun checkPlayers() {
        if (!Config.get(ConfigKeys.SPARTAKIADA_MODE)) return
        PluginManager.getLogger().info("Проверка текущих игроков на возможность участия в спартакиаде...")

        for (player in Bukkit.getOnlinePlayers()) {
            checkPlayer(player)
        }
    }

    fun canBypass(player: Player): Boolean =
        player.isOp || player.hasPermission("blockparty.admin") || player.hasPermission("blockparty.spectator")

    fun checkPlayer(player: Player) {
        if (canBypass(player)) return
        if (!Config.get(ConfigKeys.SPARTAKIADA_MODE)) return

        if (!hasAttempts(player)) {
            player.kick(
                KICK_NO_ATTEMPTS_MESSAGE,
                PlayerKickEvent.Cause.WHITELIST,
            )
            return
        }

        if (!isParticipant(player)) {
            player.kick(
                KICK_NON_PARTICIPANT_MESSAGE,
                PlayerKickEvent.Cause.WHITELIST,
            )
        }
    }

    fun watchParticipantsChanges() {
        val participantsPath =
            PluginManager.survivalGames.dataFolder
                .toPath()
                .resolve("participants.yml")
        val watchService = participantsPath.parent.fileSystem.newWatchService()

        participantsPath.parent.register(
            watchService,
            java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
        )

        watchThread =
            Thread {
                while (!Thread.interrupted()) {
                    val key = watchService.take()
                    for (event in key.pollEvents()) {
                        val changed = event.context() as? java.nio.file.Path ?: continue
                        if (changed.fileName.toString().equals("participants.yml", ignoreCase = true)) {
                            PluginManager
                                .getLogger()
                                .info("Обнаружено изменение participants.yml, перезагрузка списка участников...")
                            reload()
                            // PluginManager.getLogger().info(participants.joinToString("\n"))
                        }
                    }
                    key.reset()
                }
            }

        watchThread!!.isDaemon = true
        watchThread!!.start()
    }

    fun stopWatching() {
        watchThread?.interrupt()
        watchThread = null
    }
}
