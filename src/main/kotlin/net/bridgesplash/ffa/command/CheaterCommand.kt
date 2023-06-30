package net.bridgesplash.ffa.command

import net.bridgesplash.ffa.pvp.CombatPlayer
import net.bridgesplash.sploosh.luckperms.PermissionUtils.hasLuckPermission
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.command.builder.SimpleCommand
import net.minestom.server.entity.GameMode
import net.minestom.server.item.Enchantment
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

object CheaterCommand: SimpleCommand("cheater") {

    val opItems = {
        listOf(
            ItemStack.of(Material.DIAMOND_SWORD).withMeta{
                it.enchantment(Enchantment.SHARPNESS, Short.MAX_VALUE)
                it.enchantment(Enchantment.UNBREAKING, Short.MAX_VALUE)
                it.enchantment(Enchantment.FIRE_ASPECT, Short.MAX_VALUE)
                it.enchantment(Enchantment.KNOCKBACK, Short.MAX_VALUE)
            },
            ItemStack.of(Material.DIAMOND_HELMET).withMeta{
                it.enchantment(Enchantment.PROTECTION, Short.MAX_VALUE)
                it.enchantment(Enchantment.UNBREAKING, Short.MAX_VALUE)
                it.enchantment(Enchantment.THORNS, Short.MAX_VALUE)
            },
            ItemStack.of(Material.DIAMOND_CHESTPLATE).withMeta{
                it.enchantment(Enchantment.PROTECTION, Short.MAX_VALUE)
                it.enchantment(Enchantment.UNBREAKING, Short.MAX_VALUE)
                it.enchantment(Enchantment.THORNS, Short.MAX_VALUE)
            },
            ItemStack.of(Material.DIAMOND_LEGGINGS).withMeta{
                it.enchantment(Enchantment.PROTECTION, Short.MAX_VALUE)
                it.enchantment(Enchantment.UNBREAKING, Short.MAX_VALUE)
                it.enchantment(Enchantment.THORNS, Short.MAX_VALUE)
            },
            ItemStack.of(Material.DIAMOND_BOOTS).withMeta{
                it.enchantment(Enchantment.PROTECTION, Short.MAX_VALUE)
                it.enchantment(Enchantment.UNBREAKING, Short.MAX_VALUE)
                it.enchantment(Enchantment.THORNS, Short.MAX_VALUE)
            },
        )
    }


    /**
     * Called when the command is executed by a [CommandSender].
     *
     * @param sender  the sender which executed the command
     * @param command the command name used
     * @param args    an array containing all the args (split by space char)
     * @return true when the command is successful, false otherwise
     */
    override fun process(sender: CommandSender, command: String, args: Array<out String>?): Boolean {
        if(sender is ConsoleSender) return false
        val player = sender as CombatPlayer
        player.gameMode = GameMode.CREATIVE
        for (opItem in opItems()) {
            player.inventory.addItemStack(opItem)
        }
        player.sendMessage("You are now a cheater!")
        return true
    }

    /**
     * Called to know if a player has access to the command.
     *
     * @param sender        the command sender to check the access
     * @param commandString the raw command string,
     * null if this is an access request
     * @return true if the player has access to the command, false otherwise
     */
    override fun hasAccess(sender: CommandSender, commandString: String?): Boolean {
        return (sender.hasLuckPermission("ffa.cheater") || sender.hasLuckPermission("ffa.*")) && sender !is ConsoleSender
    }
}