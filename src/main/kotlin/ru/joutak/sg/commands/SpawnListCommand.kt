package ru.joutak.sg.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import ru.joutak.sg.arenas.SpawnManager

object SpawnListCommand : CommandExecutor, TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isNotEmpty()) return false

        if (SpawnManager.getSpawns().size == 0) {
            sender.sendMessage("Список спавнов пуст!")
            return true
        }

        sender.sendMessage("Список всех спавнов:\n")
        SpawnManager.getSpawns().forEach { sender.sendMessage("$it\n") }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> = emptyList()
}
