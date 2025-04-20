package ru.joutak.sg.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import ru.joutak.sg.arenas.SpawnManager

object RemoveSpawnCommand : CommandExecutor, TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.size != 1) return false

        if (SpawnManager.removeSpawn(args[0])) {
            sender.sendMessage("Спавн ${args[0]} был успешно удален!")
        } else {
            sender.sendMessage("Спавна с таким именем не существует!")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> =
        when (args.size) {
            1 ->
                SpawnManager
                    .getSpawns()
                    .values
                    .map { it.name }
                    .toList()
            else -> emptyList()
        }
}
