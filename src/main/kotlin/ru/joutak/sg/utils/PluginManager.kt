package ru.joutak.sg.utils

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.Bukkit
import ru.joutak.sg.SurvivalGamesPlugin
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.games.SpartakiadaManager
import java.io.File
import java.util.logging.Logger

object PluginManager {
    val survivalGames: SurvivalGamesPlugin = SurvivalGamesPlugin.instance
    val multiverseCore: MultiverseCore = Bukkit.getServer().pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

    fun getLogger(): Logger = survivalGames.logger

    fun getDataFolder(): File {
        if (Config.get(ConfigKeys.SPARTAKIADA_MODE)) {
            return SpartakiadaManager.spartakiadaFolder
        }
        return survivalGames.dataFolder
    }
}
