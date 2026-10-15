package ru.joutak.sg.loot

import org.bukkit.inventory.Inventory
import ru.joutak.sg.utils.PluginManager
import kotlin.random.Random

object ChestManager {
    fun fillContainer(inventory: Inventory, type: LootContainerType) {
        inventory.clear()
        LootManager.getRandomLoot(type).forEach { item ->
            val emptySlots = (0 until inventory.size).filter { inventory.getItem(it) == null }
            if (emptySlots.isNotEmpty()) inventory.setItem(emptySlots.random(Random.Default), item)
        }
    }
}
