package net.bridgesplash.ffa.pvp.entities.fakeplayer

import net.bridgesplash.ffa.pvp.entities.CombatDummy
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.inventory.AbstractInventory
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.network.packet.client.ClientPacket
import net.minestom.server.network.packet.client.play.*
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.KeepAlivePacket
import net.minestom.server.network.packet.server.play.PlayerPositionAndLookPacket
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.utils.MathUtils
import net.minestom.server.utils.inventory.PlayerInventoryUtils
import net.minestom.server.utils.validate.Check

class CombatDummyController(private val dummy: CombatDummy){
    fun clickWindow(
        playerInventory: Boolean,
        slot: Short,
        button: Byte,
        clickType: ClientClickWindowPacket.ClickType?,
    ) {
        var playerInventory = playerInventory
        var slot = slot
        val inventory: Inventory? = if (playerInventory) null else dummy.openInventory
        val abstractInventory: AbstractInventory = inventory ?: dummy.inventory
        playerInventory = abstractInventory is PlayerInventory
        slot = if (playerInventory) PlayerInventoryUtils.convertToPacketSlot(slot.toInt()).toShort() else slot
        val itemStack = abstractInventory.getItemStack(slot.toInt())
        addToQueue(
            ClientClickWindowPacket(
                if (playerInventory) 0 else inventory!!.windowId, 0,
                slot, button,
                clickType!!, listOf(), itemStack
            )
        )
    }

    /**
     * Closes the current opened inventory if there is any.
     */
    fun closeWindow() {
        val openInventory: Inventory? = dummy.openInventory
        addToQueue(ClientCloseWindowPacket(openInventory?.windowId ?: 0))
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel The channel of the message.
     * @param message The message data.
     */
    fun sendPluginMessage(channel: String?, message: ByteArray?) {
        addToQueue(ClientPluginMessagePacket(channel!!, message))
    }

    /**
     * Sends a plugin message to the player.
     *
     * @param channel The channel of the message.
     * @param message The message data.
     */
    fun sendPluginMessage(channel: String?, message: String) {
        sendPluginMessage(channel, message.toByteArray())
    }

    /**
     * Attacks the given `entity`.
     *
     * @param entity The entity that is to be attacked.
     */
    fun attackEntity(entity: Entity) {
        addToQueue(
            ClientInteractEntityPacket(
                entity.entityId,
                ClientInteractEntityPacket.Attack(),
                dummy.isSneaking
            )
        )
    }

    /**
     * Respawns the player.
     *
     * @see Player.respawn
     */
    fun respawn() {
        // Sending the respawn packet for some reason
        // Is related to FakePlayer#showPlayer and the tablist option (probably because of the scheduler)
        /*ClientStatusPacket statusPacket = new ClientStatusPacket();
        statusPacket.action = ClientStatusPacket.Action.PERFORM_RESPAWN;
        addToQueue(statusPacket);*/
        dummy.respawn()
    }

    /**
     * Changes the current held slot for the player.
     *
     * @param slot The slot that the player has to held.
     * @throws IllegalArgumentException If `slot` is not between `0` and `8`.
     */
    fun setHeldItem(slot: Short) {
        Check.argCondition(!MathUtils.isBetween(slot.toInt(), 0, 8), "Slot has to be between 0 and 8!")
        addToQueue(ClientHeldItemChangePacket(slot))
    }

    /**
     * Sends an animation packet that animates the specified arm.
     *
     * @param hand The hand of the arm to be animated.
     */
    fun sendArmAnimation(hand: Player.Hand?) {
        addToQueue(ClientAnimationPacket(hand!!))
    }

    /**
     * Uses the item in the given `hand`.
     *
     * @param hand The hand in which an ite mshould be.
     */
    fun useItem(hand: Player.Hand?) {
        addToQueue(ClientUseItemPacket(hand!!, 0))
    }

    /**
     * Rotates the fake player.
     *
     * @param yaw   The new yaw for the fake player.
     * @param pitch The new pitch for the fake player.
     */
    fun rotate(yaw: Float, pitch: Float) {
        addToQueue(ClientPlayerRotationPacket(yaw, pitch, dummy.isOnGround))
    }

    /**
     * Starts the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    fun startDigging(blockPosition: Point?, blockFace: BlockFace?) {
        addToQueue(
            ClientPlayerDiggingPacket(
                ClientPlayerDiggingPacket.Status.STARTED_DIGGING,
                blockPosition!!, blockFace!!, 0
            )
        )
    }

    /**
     * Stops the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    fun stopDigging(blockPosition: Point?, blockFace: BlockFace?) {
        addToQueue(
            ClientPlayerDiggingPacket(
                ClientPlayerDiggingPacket.Status.CANCELLED_DIGGING,
                blockPosition!!, blockFace!!, 0
            )
        )
    }

    /**
     * Finishes the digging process of the fake player.
     *
     * @param blockPosition The position of the block to be excavated.
     * @param blockFace     From where the block is struck.
     */
    fun finishDigging(blockPosition: Point?, blockFace: BlockFace?) {
        addToQueue(
            ClientPlayerDiggingPacket(
                ClientPlayerDiggingPacket.Status.FINISHED_DIGGING,
                blockPosition!!, blockFace!!, 0
            )
        )
    }

    /**
     * Makes the player receives a packet
     * WARNING: pretty much unsafe, used internally to redirect packets here,
     * you should instead use [PlayerConnection.sendPacket]
     *
     * @param serverPacket the packet to consume
     */
    fun consumePacket(serverPacket: ServerPacket) {
        if (serverPacket is PlayerPositionAndLookPacket) {
            addToQueue(ClientTeleportConfirmPacket(serverPacket.teleportId()))
        } else if (serverPacket is KeepAlivePacket) {
            addToQueue(ClientKeepAlivePacket(serverPacket.id()))
        }
    }

    /**
     * All packets in the queue are executed in the [Player.update] method. It is used internally to add all
     * received packet from the client. Could be used to "simulate" a received packet, but to use at your own risk!
     *
     * @param clientPlayPacket The packet to add in the queue.
     */
    private fun addToQueue(clientPlayPacket: ClientPacket) {
        this.dummy.addPacketToQueue(clientPlayPacket)
    }
}