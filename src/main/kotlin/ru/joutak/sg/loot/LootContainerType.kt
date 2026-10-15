package ru.joutak.sg.loot

import org.bukkit.Material


enum class LootContainerType(val configKey: String, private vararg val materialNames: String) {
    CHEST("chest", "CHEST"),
    WAXED_COPPER_CHEST("waxed_copper_chest", "WAXED_COPPER_CHEST"),
    WAXED_OXIDIZED_COPPER_CHEST("waxed_oxidized_copper_chest", "WAXED_OXIDIZED_COPPER_CHEST"),
    BARREL("barrel", "BARREL"),
    TRAPPED_CHEST("trapped_chest", "TRAPPED_CHEST");

    companion object {
        fun from(material: Material): LootContainerType? = entries.firstOrNull { material.name in it.materialNames }
    }
}
