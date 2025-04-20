package ru.joutak.sg.loot

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.joutak.sg.utils.PluginManager
import java.io.File

object LootManager {
    val lootItems = mutableListOf<LootItem>()
    val file: File = File(PluginManager.getDataFolder(), "loot.yml")
    val config: YamlConfiguration

    init {
        PluginManager.getLogger().info("Загрузка конфига с возможным лутом...")
        if (!file.exists()) {
            PluginManager.survivalGames.saveResource("loot.yml", true)
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun loadLoot() {
        lootItems.clear()
        for (section in config.getMapList("loot")) {
            val material = Material.valueOf(section["material"].toString())
            val chance = section["chance"].toString().toDouble()
            val min = section["min"].toString().toInt()
            val max = section["max"].toString().toInt()
            val name = section["name"]?.toString()
            val enchantsMap = mutableMapOf<Enchantment, Int>()
            val enchants = section["enchants"] as? Map<*, *>
            enchants?.forEach { (key, value) ->
                val enchant = Enchantment.getByName(key.toString())
                if (enchant != null) {
                    enchantsMap[enchant] = value.toString().toInt()
                }
            }

            lootItems += LootItem(material, chance, min, max, name, enchantsMap)
        }
    }

    fun getRandomLoot(): List<ItemStack> {
        val result = mutableListOf<ItemStack>()

        for (item in lootItems) {
            if (Math.random() * 100 <= item.chance) {
                val amount = (item.min..item.max).random()
                val stack = ItemStack(item.material, amount)
                val meta = stack.itemMeta

                if (item.name != null) meta.displayName(Component.text(item.name))
                item.enchants.forEach { (ench, level) -> meta.addEnchant(ench, level, true) }

                stack.itemMeta = meta
                result += stack
            }
        }

        return result
    }
}
