package ru.joutak.sg.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import ru.joutak.sg.arenas.SpawnManager
import ru.joutak.sg.utils.PluginManager

object AddSpawnCommand : CommandExecutor, TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.size != 6) {
            return false
        }

        try {
            val x = args[1].toDouble()
            val y = args[2].toDouble()
            val z = args[3].toDouble()
            val yaw = args[4].toDouble()
            val pitch = args[5].toDouble()
            if (SpawnManager.addSpawn(args[0], x, y, z, yaw, pitch)) {
                sender.sendMessage("Спавн ${args[0]} был успешно добавлен!")
            } else {
                sender.sendMessage("Спавн с таким именем уже существует!")
            }
        } catch (e: NumberFormatException) {
            sender.sendMessage("Координаты должны быть числами!")
            return false
        } catch (e: Exception) {
            PluginManager.getLogger().warning("Ошибка при добавлении спавна: ${e.message}")
            sender.sendMessage("Не удалось добавить спавн, проверьте вводимые данные!")
            return false
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        if (sender is Player) {
            return when (args.size) {
                2 -> listOf(sender.location.blockX.toString())
                3 -> listOf(sender.location.blockY.toString())
                4 -> listOf(sender.location.blockZ.toString())
                5 ->
                    listOf(
                        sender.location.yaw
                            .toInt()
                            .toString(),
                    )
                6 ->
                    listOf(
                        sender.location.pitch
                            .toInt()
                            .toString(),
                    )
                else -> emptyList()
            }
        }
        return emptyList()
    }
}
