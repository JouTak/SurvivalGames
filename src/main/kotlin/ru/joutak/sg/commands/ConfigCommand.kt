package ru.joutak.sg.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKey
import ru.joutak.sg.config.ConfigKeys

object ConfigCommand : CommandExecutor, TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!sender.hasPermission("sg.admin")) {
            sender.sendMessage("Недостаточно прав для использования данной команды.")
            return true
        }

        var set = false
        var get = false

        if (args.size == 2) {
            set = true
        } else if (args.size == 1) {
            get = true
        }

        if (!get && !set) {
            return false
        }

        val key = ConfigKeys.all.find { it.path.equals(args[0], ignoreCase = true) }

        if (key == null) {
            sender.sendMessage("${args[0]} не найден.")
            return true
        }

        if (get) {
            sender.sendMessage("Текущее значение ${key.path}: ${Config.get(key)}")
            return true
        }
        // else if (set)
        val value = key.parse(args[1])

        if (value == null) {
            sender.sendMessage("Не удалось преобразовать ${args[1]}.")
            return true
        }

        @Suppress("UNCHECKED_CAST")
        Config.set(key as ConfigKey<Any>, value)
        sender.sendMessage("Значение ${key.path} обновлено на $value.")

        // Костыль чтобы не делать /reload confirm ;)
        if (key.path.equals(ConfigKeys.SPARTAKIADA_MODE.path)) {
            TODO()
            // PlayerData.reloadDatas()
            // SpartakiadaManager.reload()
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
            1 -> ConfigKeys.all.map { it.path }.filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
}
