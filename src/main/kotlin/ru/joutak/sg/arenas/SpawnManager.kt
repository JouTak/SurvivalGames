package ru.joutak.sg.arenas

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import ru.joutak.sg.utils.PluginManager
import java.io.File
import java.io.IOException

class SpawnManager {
    companion object {
        private val spawnsFile: File = File(PluginManager.getDataFolder(), "spawns.yml")
        private val spawns = mutableMapOf<String, SpawnLocation>()

        fun addSpawn(
            name: String,
            x: Double,
            y: Double,
            z: Double,
            yaw: Double,
            pitch: Double,
        ): Boolean {
            if (!spawns.containsKey(name)) {
                spawns[name] = SpawnLocation(name, x, y, z, yaw, pitch)
                return true
            }
            return false
        }

        fun removeSpawn(name: String): Boolean = spawns.remove(name) != null

        fun getSpawns(): Map<String, SpawnLocation> = spawns

        fun loadSpawns() {
            if (!spawnsFile.exists()) {
                PluginManager.survivalGames.saveResource("spawns.yml", true)
            }

            val yaml = YamlConfiguration.loadConfiguration(spawnsFile)

            spawns.clear()

            for (loc in yaml.getList("spawns") as List<Map<String, Any>>) {
                try {
                    PluginManager.getLogger().info("Десериализация информации о спавнах...")
                    val spawnLoc = SpawnLocation.deserialize(loc)
                    spawns[spawnLoc.name] = spawnLoc
                } catch (e: Exception) {
                    PluginManager.getLogger().severe("Ошибка при загрузке спавнов: ${e.message}")
                    break
                }
            }
        }

        fun saveSpawns() {
            val yaml = YamlConfiguration()

            yaml.set(
                "spawns",
                spawns.values.map { it.serialize() },
            )

            try {
                yaml.save(spawnsFile)
            } catch (e: IOException) {
                PluginManager.getLogger().severe("Ошибка при сохранении спавнов: ${e.message}")
            }
        }
    }

    private val used = mutableSetOf<SpawnLocation>()
    private val barriers = mutableSetOf<Location>()

    fun getNextSpawn(world: World): Location {
        var next: SpawnLocation
        do {
            next = spawns.values.random()

            if (used.count() == spawns.count()) {
                PluginManager.getLogger().warning(
                    "Недостаточно спавнов для текущего кол-ва игроков! Некоторые игроки могут быть на одном и том же спавне.",
                )
                break
            }
        } while (used.contains(next))

        used.add(next)
        val loc = next.toLocation(world)
        setBarriers(loc)
        return loc
    }

    fun setBarriers(loc: Location) {
        val minX = loc.blockX - 1
        val maxX = loc.blockX + 1
        val minZ = loc.blockZ - 1
        val maxZ = loc.blockZ + 1
        val baseY = loc.blockY

        // Для y = baseY .. baseY + 2 (высота 3 блока)
        for (y in baseY..(baseY + 2)) {
            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    // ставим только стенки (края квадрат)
                    if (x == minX || x == maxX || z == minZ || z == maxZ) {
                        val bLoc = Location(loc.world, x.toDouble(), y.toDouble(), z.toDouble())
                        loc.world.getBlockAt(bLoc).type = Material.BARRIER
                        barriers += bLoc
                    }
                }
            }
        }
    }

    fun removeBarriers() {
        barriers.forEach {
            if (it.block.type == Material.BARRIER) {
                it.block.type = Material.AIR
            }
        }
        barriers.clear()
    }
}
