package net.bridgesplash.ffa.config

import kotlinx.serialization.Serializable
import net.bridgesplash.sploosh.serializer.PointSerializer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block

@Serializable
data class Region(
    val pos1: @Serializable(with = PointSerializer::class) Point = Vec.ZERO,
    val pos2: @Serializable(with = PointSerializer::class) Point = Vec.ZERO,
){
    private val minX = minOf(pos1.x(), pos2.x())
    private val maxX = maxOf(pos1.x(), pos2.x())
    private val minY = minOf(pos1.y(), pos2.y())
    private val maxY = maxOf(pos1.y(), pos2.y())
    private val minZ = minOf(pos1.z(), pos2.z())
    private val maxZ = maxOf(pos1.z(), pos2.z())


    fun contains(point: Point): Boolean {
        return point.x() in minX..maxX && point.y() in minY..maxY && point.z() in minZ..maxZ
    }

    // loop
    fun forEach(instance: Instance, block: (Block) -> Unit){
        for(x in minX.toInt()..maxX.toInt()){
            for(y in minY.toInt()..maxY.toInt()){
                for(z in minZ.toInt()..maxZ.toInt()){
                    block(instance.getBlock(Vec(x.toDouble(), y.toDouble(), z.toDouble())))
                }
            }
        }
    }

}
