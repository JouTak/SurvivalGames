package ru.joutak.sg.loot

import org.bukkit.inventory.Inventory

object ChestManager {
    fun fillChest(inventory: Inventory) {
        inventory.clear()
        inventory.contents = LootManager.getRandomLoot().toTypedArray()
        // PluginManager.getLogger().info("Filled loot in chest with: ${inventory.contents.joinToString { it.toString() }}")
    }
}
