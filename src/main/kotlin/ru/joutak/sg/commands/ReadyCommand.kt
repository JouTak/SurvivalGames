package ru.joutak.sg.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.lobby.LobbyManager
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

        val playerData = PlayerData.get(sender)

        if (!playerData.isInLobby()) {
            sender.sendMessage("Данную команду можно использовать только в лобби.")
            return true
        }

        if (playerData.isReady()) {
            playerData.setReady(false)
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
            playerData.setReady(true)
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
        LobbyManager.checkPlayers()
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> = emptyList<String>()
}
