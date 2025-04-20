package ru.joutak.sg.utils

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.Bukkit
import ru.joutak.sg.SurvivalGamesPlugin
import java.io.File
import java.util.logging.Logger

object PluginManager {
    val survivalGames: SurvivalGamesPlugin = SurvivalGamesPlugin.instance
    val multiverseCore: MultiverseCore = Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

    fun getLogger(): Logger = survivalGames.logger

    fun getDataFolder(): File = survivalGames.dataFolder
}
