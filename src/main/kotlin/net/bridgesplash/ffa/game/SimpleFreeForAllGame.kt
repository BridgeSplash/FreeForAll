package net.bridgesplash.ffa.game

import io.github.bloepiloepi.pvp.PvpExtension
import net.bridgesplash.ffa.FreeForAll
import net.bridgesplash.ffa.config.ConfigManager
import net.bridgesplash.ffa.config.MapConfig
import net.bridgesplash.ffa.pvp.PvPManager
import net.bridgesplash.ffa.pvp.entities.CombatDummy
import net.bridgesplash.sploosh.Manager
import net.bridgesplash.sploosh.dimension.DimensionManager
import net.bridgesplash.sploosh.game.GameManager.joinGameOrNew
import net.bridgesplash.sploosh.game.PvpGame
import net.bridgesplash.sploosh.schematic.Schematic
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.Instance
import net.minestom.server.tag.Tag
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.notExists

class SimpleFreeForAllGame: PvpGame() {


    private val gameMap: MapConfig = ConfigManager.maps.firstOrNull() ?: error("No maps found!")

    override val allowsSpectators: Boolean = true
    override val canJoinDuringGame: Boolean = true
    override val countdownSeconds: Int = 0
    override val maxPlayers: Int = 80
    override val minPlayers: Int = 0
    override val showScoreboard: Boolean = false // TODO - later
    override val showsJoinLeaveMessages: Boolean = true

    private val spawnPoint = Pos(0.5, 64.0, 0.5, 90f, 0f)


    private val pvpManager = PvPManager(this)


    override fun victory(winningPlayers: Collection<Player>) { } // Prevent game-end and sending players away

    override fun getSpawnPosition(player: Player, spectator: Boolean): Pos {
        return spawnPoint
    }

    override fun gameEnded() {
    }

    override fun gameStarted() {
        for (activePlayer in activePlayers) {
            activePlayer.sendMessage("Game Started!")
        }
        for(i in 0..10){
            CombatDummy.dummyFromId(i.toLong()).thenAccept { it.joinGameOrNew(this.gameName) }
        }
    }

    override fun instanceCreate(): CompletableFuture<Instance> {
        val instance = Manager.instance.createInstanceContainer(
            DimensionManager.FULLBRIGHT,
            AnvilLoader("worlds/${gameMap.mapName}")
        )
        val completableFuture = CompletableFuture<Instance>()
        instance.timeRate = 0
        instance.time = 6000
        instance.timeUpdate = null
        instance.enableAutoChunkLoad(false)
        instance.setTag(Tag.Boolean("doNotAutoUnloadChunk"), true)

        instance.loadChunk(spawnPoint.chunkX(), spawnPoint.chunkZ()).thenAccept { it.sendChunk() }

        val radius = 8

        val futures = mutableListOf<CompletableFuture<*>>()
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val future = instance.loadChunk(x + spawnPoint.chunkX(), z + spawnPoint.chunkZ()).thenAccept {
                    it.sendChunk()
                }
                futures.add(future)
            }
        }
        CompletableFuture.allOf(*futures.toTypedArray()).thenAccept {

            if(gameMap.mapName == "null"){
                val path  = Path.of("schematics/BlankFFA.schem")
                if(path.notExists())  {
                    FreeForAll::class.java.getResource("/BlankFFA.schem")!!.openStream().use { input ->
                        path.toFile().outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                val schematic = Schematic(path, Schematic.Companion.SchematicType.SCHEM, false)
                schematic.load(instance, Pos(0.5, 53.5, 0.5))
            }
            completableFuture.complete(instance);
        }

        return completableFuture
    }

    override fun playerDied(player: Player, killer: Entity?) {
        player.sendMessage("You died!")
    }

    override fun playerJoin(player: Player) {
        PvpExtension.setLegacyAttack(player, true)
        player.sendMessage("Welcome to the ffa")
    }

    override fun playerLeave(player: Player) {
        pvpManager.removePlayer(player)
    }

    override fun registerEvents(eventNode: EventNode<InstanceEvent>) {
        eventNode.addChild(pvpManager.events())
    }

    override fun respawn(player: Player) { // handle stuff
    }
}