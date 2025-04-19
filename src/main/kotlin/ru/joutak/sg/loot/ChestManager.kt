package ru.joutak.sg.loot

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import ru.joutak.sg.utils.PluginManager

object ChestManager {
    fun fillChest(inventory: Inventory) {
        PluginManager.getLogger().info("Filling chest: $inventory")
        inventory.clear()
        val item = ItemStack(Material.GOLD_BLOCK)
        inventory.contents = arrayOf(item)
    }
}
