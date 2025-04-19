package ru.joutak.sg.listeners

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.loot.ChestManager
import ru.joutak.sg.utils.PluginManager

object ChestListener : Listener {
    private val lootedChests = mutableSetOf<Location>()

    @EventHandler
    fun onChestOpen(event: PlayerInteractEvent) {
        if (!ArenaManager.isArena(event.player.location.world)) return

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type != Material.CHEST) return

        val state = block.state
        if (state !is Chest) return

        val location = block.location

        if (lootedChests.contains(location)) return

        val data = state.persistentDataContainer
        val key = NamespacedKey(PluginManager.survivalGames, "no_loot")
        if (data.has(key, PersistentDataType.BYTE)) return

        ChestManager.fillChest(state.blockInventory)

        // event.player.closeInventory()
        // event.player.openInventory(state.blockInventory)

        lootedChests.add(location)
        // PluginManager.getLogger().info("Filled loot in chest at $location")
    }

    @EventHandler
    fun onPlayerPlaceChest(event: BlockPlaceEvent) {
        if (event.blockPlaced.type != Material.CHEST) return

        val state = event.blockPlaced.state
        if (state is Chest) {
            val data = state.persistentDataContainer
            val key = NamespacedKey(PluginManager.survivalGames, "no_loot")
            data.set(key, PersistentDataType.BYTE, 1.toByte())
            state.update()
        }
    }
}
