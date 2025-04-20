package ru.joutak.sg.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.games.GameManager
import ru.joutak.sg.lobby.LobbyManager

object SpectateCommand : CommandExecutor, TabExecutor {
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

        var startSpectating = false
        var endSpectating = false

        if (args.size == 1) {
            startSpectating = true
        } else if (args.isEmpty()) {
            endSpectating = true
        }

        if (!startSpectating && !endSpectating) {
            return false
        }

        if (endSpectating) {
            GameManager.getBySpectator(sender).forEach { it.removeSpectator(sender) }
            LobbyManager.teleportToLobby(sender)
            return true
        }

        // else if (startSpectating)
        val arena = ArenaManager.get(args[0])
        if (arena == null) {
            sender.sendMessage("Арены с таким именем не существует.")
            return true
        }
        val game = GameManager.getByArena(arena)
        if (game == null) {
            sender.sendMessage("В данный момент на арене не идет игра.")
            return true
        }

        GameManager.getBySpectator(sender).forEach { it.removeSpectator(sender) }
        game.addSpectator(sender)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> =
        when (args.size) {
            1 -> ArenaManager.getArenas().keys.filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
}
