package ru.joutak.sg.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import ru.joutak.sg.games.GameManager
import ru.joutak.sg.games.GameManager.isPlaying

object PlayerDamageListener : Listener {
    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return

        val player = event.entity as Player

        if (!isPlaying(player)) {
            return
        }

        if (player.health > event.damage) {
            return
        }

        val game = GameManager.getByPlayer(player)!!
        event.isCancelled = true
        game.kill(player)
    }
}
