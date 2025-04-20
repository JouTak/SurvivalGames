package ru.joutak.sg.arenas

import org.bukkit.Location
import org.bukkit.World
import java.util.Objects

data class SpawnLocation(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double,
    val pitch: Double,
) {
    companion object {
        fun deserialize(values: Map<String, Any>): SpawnLocation =
            SpawnLocation(
                values["name"] as String,
                values["x"] as Double,
                values["y"] as Double,
                values["z"] as Double,
                values["yaw"] as Double,
                values["pitch"] as Double,
            )
    }

    fun toLocation(world: World): Location = Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())

    fun serialize(): Map<String, Any> =
        mapOf(
            "name" to name,
            "x" to x,
            "y" to y,
            "z" to z,
            "pitch" to pitch,
            "yaw" to yaw,
        )

    override fun toString(): String = "$name (x: $x, y: $y, z: $z, yaw: $yaw, pitch: $pitch)"

    override fun equals(other: Any?): Boolean {
        if (other !is SpawnLocation) return false

        return this.x == other.x &&
            this.y == other.y &&
            this.z == other.z &&
            this.yaw == other.yaw &&
            this.pitch == other.pitch
    }

    override fun hashCode(): Int = Objects.hash(x, y, z, yaw, pitch)
}
