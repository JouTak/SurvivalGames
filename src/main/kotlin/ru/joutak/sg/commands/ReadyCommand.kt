package ru.joutak.sg.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import ru.joutak.blockparty.lobby.LobbyReadyBossBar
import ru.joutak.minigames.managers.MatchmakingManager
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.players.PlayerData

object ReadyCommand : CommandExecutor, TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Данную команду можно использовать только в игре.")
            return true
        }

        if (args.isNotEmpty()) {
            return false
        }

        if (!PlayerData.get(sender.uniqueId).isInLobby()) {
            sender.sendMessage("Данную команду можно использовать только в лобби.")
            return true
        }

        if (MatchmakingManager.isPlayerInAnyInstance(sender.uniqueId)) {
            MatchmakingManager.removePlayer(sender)
            sender.sendMessage(
                LinearComponents.linear(
                    Component.text("Вы "),
                    Component.text("вышли", NamedTextColor.RED),
                    Component.text(" из очереди на "),
                    SurvivalGamesPlugin.TITLE,
                    Component.text("!"),
                ),
            )
        } else {
            MatchmakingManager.addPlayer(sender)
            sender.sendMessage(
                LinearComponents.linear(
                    Component.text("Вы "),
                    Component.text("встали", NamedTextColor.GREEN),
                    Component.text(" в очередь на "),
                    SurvivalGamesPlugin.TITLE,
                    Component.text("!"),
                ),
            )
        }
        LobbyReadyBossBar.checkLobby()
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> = emptyList<String>()
}
