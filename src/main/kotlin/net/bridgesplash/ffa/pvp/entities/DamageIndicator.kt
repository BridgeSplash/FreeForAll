package net.bridgesplash.ffa.pvp.entities

import net.bridgesplash.sploosh.Manager
import net.bridgesplash.sploosh.npc.PacketHologram
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.ThreadLocalRandom

object DamageIndicator {

    fun send(viewers: Collection<Player>, loc: Vec, damage: Float, offset: Vec = Vec.ZERO){
        val rnd = ThreadLocalRandom.current()
        val packetHolo = PacketHologram(
            listOf(Component.text("☆ ${damage.toInt()}", TextColor.color(0x6F00F6))),
            loc.withX(
                (rnd.nextDouble(offset.x*2)-offset.x) + loc.x
            )
            .withY(
                (rnd.nextDouble(offset.y*2)-offset.y) + loc.y
            )
            .withZ(
                (rnd.nextDouble(offset.z*2)-offset.z) + loc.z
            ))

        viewers.forEach{ packetHolo.addViewer(it) }
        Manager.scheduler.scheduleTask({
            packetHolo.destroy()
        }, TaskSchedule.seconds(1), TaskSchedule.stop(), ExecutionType.SYNC)
    }
    fun send(viewers: Collection<Player>, loc: Vec, damage: Float, offset: Double = 0.0){
        send(viewers, loc, damage, Vec(offset, offset, offset))
    }

    fun send(viewers: Collection<Player>, loc: Vec, damage: Float){
        send(viewers, loc, damage, Vec.ZERO)
    }

}