package ru.joutak.sg.listeners

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.joutak.sg.arenas.ArenaManager
import ru.joutak.sg.loot.ChestManager
import ru.joutak.sg.loot.LootContainerType

object ChestListener : Listener {
    private data class LootContainer(val type: LootContainerType, var generated: Boolean = false)
    private data class BlockPosition(val x: Int, val y: Int, val z: Int)
    private val arenaContainers = mutableMapOf<String, MutableMap<BlockPosition, LootContainer>>()

    fun prepareArena(world: World) {
        val containers = mutableMapOf<BlockPosition, LootContainer>()
        val border = world.worldBorder
        val minChunkX = kotlin.math.floor((border.center.x - border.size / 2) / 16.0).toInt()
        val maxChunkX = kotlin.math.floor((border.center.x + border.size / 2) / 16.0).toInt()
        val minChunkZ = kotlin.math.floor((border.center.z - border.size / 2) / 16.0).toInt()
        val maxChunkZ = kotlin.math.floor((border.center.z + border.size / 2) / 16.0).toInt()

        for (chunkX in minChunkX..maxChunkX) for (chunkZ in minChunkZ..maxChunkZ) {
            val chunk = world.getChunkAt(chunkX, chunkZ)
            chunk.tileEntities.forEach { state ->
                val type = LootContainerType.from(state.type) ?: return@forEach
                if (state is Container) containers[position(state.block)] = LootContainer(type)
            }
        }
        arenaContainers[world.name] = containers
    }

    fun clearLootedChests(world: World) {
        arenaContainers.remove(world.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onContainerOpen(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (!ArenaManager.isArena(block.world) || !event.action.isRightClick) return
        generate(block)
    }

    @EventHandler(ignoreCancelled = true)
    fun onContainerBreak(event: BlockBreakEvent) = generate(event.block)

    @EventHandler(ignoreCancelled = true)
    fun onExplosion(event: EntityExplodeEvent) = event.blockList().forEach(::generate)

    @EventHandler(ignoreCancelled = true)
    fun onExplosion(event: BlockExplodeEvent) = event.blockList().forEach(::generate)

    @EventHandler(ignoreCancelled = true)
    fun onContainerPlace(event: BlockPlaceEvent) {
        if (ArenaManager.isArena(event.blockPlaced.world)) {
            arenaContainers[event.blockPlaced.world.name]?.remove(position(event.blockPlaced))
        }
    }

    private fun generate(block: Block) {
        val entry = arenaContainers[block.world.name]?.get(position(block)) ?: return
        if (entry.generated) return
        val state = block.state as? Container ?: return
        ChestManager.fillContainer(state.inventory, entry.type)
        entry.generated = true
    }

    private fun position(block: Block) = BlockPosition(block.x, block.y, block.z)
}
