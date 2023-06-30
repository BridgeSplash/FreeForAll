package net.bridgesplash.ffa

import io.github.bloepiloepi.pvp.PvpExtension
import net.bridgesplash.ffa.command.CheaterCommand
import net.bridgesplash.ffa.config.ConfigManager
import net.bridgesplash.ffa.game.SimpleFreeForAllGame
import net.bridgesplash.ffa.pvp.CombatPlayer
import net.bridgesplash.sploosh.Manager
import net.bridgesplash.sploosh.Sploosh
import net.bridgesplash.sploosh.game.GameManager
import net.kyori.adventure.text.Component

object FreeForAll {

    @JvmStatic
    fun main(args: Array<String>){

        ConfigManager.loadConfigs()
        System.setProperty("debug-game", "true")// TODO: remove

        // Start server
        Sploosh.runServer()

        // register extensions
        PvpExtension.init()
        Manager.connection.setPlayerProvider { uuid, username, connection -> CombatPlayer(uuid, username, connection) }

        // register commands
        Manager.command.register(CheaterCommand)


        // register games

        GameManager.registerGame<SimpleFreeForAllGame>(
            "ffa",
            Component.text("Free For All"),
            showsInSlashPlay = true,
            canSpectate = true
        )


    }

}