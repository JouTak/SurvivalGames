package ru.joutak.sg.loot

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

data class LootItem(
    val material: Material,
    val chance: Double,
    val min: Int,
    val max: Int,
    val name: String? = null,
    val enchants: Map<Enchantment, Int> = emptyMap(),
)
