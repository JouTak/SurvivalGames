package ru.joutak.sg.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.games.SpartakiadaManager
import ru.joutak.sg.utils.PluginManager

object PlayerLoginListener : Listener {
    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        if (!Config.get(ConfigKeys.SPARTAKIADA_MODE)) {
            return
        }

        PluginManager.getLogger().info(Config.get(ConfigKeys.SPARTAKIADA_MODE).toString())

        val player = event.player

        if (SpartakiadaManager.canBypass(player)) return

        if (!SpartakiadaManager.hasAttempts(player)) {
            event.disallow(
                PlayerLoginEvent.Result.KICK_WHITELIST,
                SpartakiadaManager.KICK_NO_ATTEMPTS_MESSAGE,
            )
            return
        }

        if (!SpartakiadaManager.isParticipant(player)) {
            event.disallow(
                PlayerLoginEvent.Result.KICK_WHITELIST,
                SpartakiadaManager.KICK_NON_PARTICIPANT_MESSAGE,
            )
            return
        }
    }
}
