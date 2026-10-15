package ru.joutak.sg.loot

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.joutak.sg.utils.PluginManager
import java.io.File
import kotlin.random.Random

data class LootPool(val minRolls: Int, val maxRolls: Int, val items: List<LootItem>)

object LootManager {
    private val pools = mutableMapOf<LootContainerType, LootPool>()
    private val file = File(PluginManager.getDataFolder(), "loot.yml")

    fun loadLoot() {
        if (!file.exists()) PluginManager.survivalGames.saveResource("loot.yml", false)
        val config = YamlConfiguration.loadConfiguration(file)
        pools.clear()
        loadPools(config)
        if (!config.isConfigurationSection("pools")) {
            config.getMapList("loot").mapNotNull(::readItem).takeIf { it.isNotEmpty() }?.let {
                pools[LootContainerType.CHEST] = LootPool(2, 5, it)
            }
            PluginManager.survivalGames.getResource("loot.yml")?.reader()?.use { defaults ->
                loadPools(YamlConfiguration.loadConfiguration(defaults), onlyMissing = true)
            }
            PluginManager.getLogger().warning("Используется устаревший loot.yml. Добавлены стандартные пулы новых контейнеров.")
        }
    }

    private fun loadPools(config: YamlConfiguration, onlyMissing: Boolean = false) {
        LootContainerType.entries.forEach { type ->
            if (onlyMissing && type in pools) return@forEach
            val section = config.getConfigurationSection("pools.${type.configKey}") ?: return@forEach
            val items = readItems(section)
            if (items.isEmpty()) return@forEach
            val minRolls = section.getInt("rolls.min", 1).coerceAtLeast(0)
            pools[type] = LootPool(minRolls, section.getInt("rolls.max", minRolls).coerceAtLeast(minRolls), items)
        }
    }

    fun getRandomLoot(type: LootContainerType): List<ItemStack> {
        val pool = pools[type] ?: return emptyList()
        if (pool.items.sumOf { it.weight } <= 0) return emptyList()
        return List(Random.nextInt(pool.minRolls, pool.maxRolls + 1)) { createStack(weightedItem(pool.items)) }
    }

    private fun readItems(section: ConfigurationSection) = section.getMapList("items").mapNotNull(::readItem)

    private fun readItem(source: Map<*, *>): LootItem? = try {
        val material = Material.matchMaterial(source["material"]?.toString() ?: return null) ?: return null
        val weight = (source["weight"] ?: source["chance"] ?: 1).toString().toDouble().toInt().coerceAtLeast(0)
        val min = source["min"].toString().toInt().coerceAtLeast(1)
        val max = source["max"].toString().toInt().coerceAtLeast(min)
        val enchants = mutableMapOf<Enchantment, Int>()
        (source["enchants"] as? Map<*, *>)?.forEach { (key, value) ->
            Enchantment.getByName(key.toString())?.let { enchants[it] = value.toString().toInt() }
        }
        LootItem(material, weight, min, max, source["name"]?.toString(), enchants)
    } catch (exception: Exception) {
        PluginManager.getLogger().warning("Пропущена некорректная запись лута: ${exception.message}")
        null
    }

    private fun weightedItem(items: List<LootItem>): LootItem {
        var point = Random.nextInt(items.sumOf { it.weight })
        for (item in items) {
            point -= item.weight
            if (point < 0) return item
        }
        return items.last()
    }

    private fun createStack(item: LootItem) = ItemStack(item.material, Random.nextInt(item.min, item.max + 1)).also { stack ->
        stack.itemMeta = stack.itemMeta.apply {
            if (item.name != null) displayName(Component.text(item.name))
            item.enchants.forEach { (enchantment, level) -> addEnchant(enchantment, level, true) }
        }
    }
}
