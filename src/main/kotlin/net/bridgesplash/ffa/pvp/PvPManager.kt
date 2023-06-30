package net.bridgesplash.ffa.pvp

import io.github.bloepiloepi.pvp.config.DamageConfig
import io.github.bloepiloepi.pvp.config.PvPConfig
import io.github.bloepiloepi.pvp.events.EntityPreDeathEvent
import net.bridgesplash.ffa.pvp.entities.DamageIndicator
import net.bridgesplash.sploosh.game.Game
import net.bridgesplash.sploosh.game.GameManager.game
import net.bridgesplash.sploosh.game.PvpGame
import net.bridgesplash.sploosh.util.ListenerUtils.listenOnly
import net.bridgesplash.sploosh.util.toMini
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.trait.InstanceEvent

class PvPManager(private val game: PvpGame) {
    private val pvpEvents: EventNode<InstanceEvent> = EventNode.type("ffa-pvp", EventFilter.INSTANCE)
    private val pvpConfig = PvPConfig.legacyBuilder()
        .damage(DamageConfig.legacyBuilder().fallDamage(false).build())
        .build()


    init{
        pvpEvents.addChild(pvpConfig.createNode())
        pvpEvents.listenOnly<PlayerDeathEvent>{
            val killerUUID = (this.player as CombatPlayer).lastPlayerDamagedBy()?.first
            game.kill(this.player, killerUUID?.let{Entity.getEntity(it)})
        }
        pvpEvents.listenOnly<EntityPreDeathEvent>{
            if(this.entity !is CombatPlayer)return@listenOnly
            val player = this.entity as CombatPlayer
            player.lastEntityAttacked()?.let{ killer -> if(killer is Player) killer.sendMessage(player.getDamageReport().toMini()) }
        }

        pvpEvents.listenOnly<EntityDamageEvent>{
            if(this.entity !is Player)return@listenOnly
            val player = this.entity as Player
            if(player.game !is Game)return@listenOnly
            DamageIndicator.send(player.game!!.players, player.position.asVec(), this.damage, 0.25)
        }
    }

    fun events(): EventNode<InstanceEvent> {
        return pvpEvents
    }


    fun removePlayer(player: Player){
        // unlock combat-locked players
    }
}