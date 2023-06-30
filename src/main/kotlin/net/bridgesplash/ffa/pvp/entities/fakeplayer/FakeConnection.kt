package net.bridgesplash.ffa.pvp.entities.fakeplayer

import net.bridgesplash.ffa.pvp.entities.CombatDummy
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.SendablePacket
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.utils.validate.Check
import java.net.InetSocketAddress
import java.net.SocketAddress


class FakeConnection : PlayerConnection() {
    override fun sendPacket(packet: SendablePacket) {
        val controller = fakePlayer.controller
        val serverPacket = SendablePacket.extractServerPacket(packet)
        controller.consumePacket(serverPacket)
    }

    override fun getRemoteAddress(): SocketAddress {
        return InetSocketAddress(0)
    }

    private val fakePlayer: CombatDummy
        get() = player as CombatDummy

    override fun setPlayer(player: Player) {
        Check.argCondition(player !is CombatDummy, "FakePlayerController needs a FakePlayer object")
        super.setPlayer(player)
    }
}

