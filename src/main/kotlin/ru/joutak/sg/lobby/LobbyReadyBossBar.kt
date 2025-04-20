package ru.joutak.blockparty.lobby

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.LinearComponents
import org.bukkit.entity.Player
import ru.joutak.sg.config.Config
import ru.joutak.sg.config.ConfigKeys
import ru.joutak.sg.lobby.LobbyManager
import ru.joutak.sg.players.PlayerData
import java.util.UUID

object LobbyReadyBossBar {
    private val bars = mutableMapOf<UUID, BossBar>()

    fun removeAllBossBars() {
        LobbyManager.getPlayers().forEach { it.activeBossBars().toList().forEach { bar -> it.hideBossBar(bar) } }
    }

    fun checkLobby() {
        for (player in LobbyManager.getPlayers()) {
            setFor(player)
        }
    }

    fun setFor(player: Player) {
        val uuid = player.uniqueId
        val state = PlayerData.get(uuid).isReady()

        val text: String
        val color: BossBar.Color
        val progress: Float

        when (state) {
            true -> {
                text = "Готов к игре! :) "
                color = BossBar.Color.GREEN
                progress = 1.0f
            }

            false -> {
                text = "Не готов :( "
                color = BossBar.Color.RED
                progress = 1.0f
            }
        }

        val bar =
            BossBar.bossBar(
                LinearComponents.linear(
                    Component.text(text),
                    Component.text(
                        "[${LobbyManager.getReadyPlayers().count()}/${
                            Config.get(
                                ConfigKeys.PLAYERS_TO_START,
                            )
                        }]",
                    ),
                ),
                progress,
                color,
                BossBar.Overlay.PROGRESS,
            )

        removeFor(player)
        player.showBossBar(bar)
        bars[uuid] = bar
    }

    fun removeFor(player: Player) {
        if (bars.containsKey(player.uniqueId)) {
            player.hideBossBar(bars[player.uniqueId]!!)
            bars.remove(player.uniqueId)
        }
    }
}
