package net.bridgesplash.ffa.config

import kotlinx.serialization.Serializable

@Serializable
data class MapConfig(
    val mapName: String = "null",
    val displayName: String = "<red>Null Map</red>",
    val time: Long = 6000,
    val chunkRadius: Int = 6,
    val safeArea: List<Region> = listOf(Region()),
)