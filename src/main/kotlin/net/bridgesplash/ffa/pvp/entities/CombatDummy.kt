package net.bridgesplash.ffa.pvp.entities

import com.extollit.gaming.ai.path.HydrazinePathFinder
import net.bridgesplash.ffa.pvp.CombatPlayer
import net.bridgesplash.ffa.pvp.entities.fakeplayer.CombatDummyController
import net.bridgesplash.ffa.pvp.entities.fakeplayer.FakeConnection
import net.bridgesplash.sploosh.Manager
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.entity.fakeplayer.FakePlayerOption
import net.minestom.server.entity.pathfinding.Navigator
import net.minestom.server.event.EventListener
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.Instance
import net.minestom.server.network.player.PlayerConnection
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.time.TimeUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class CombatDummy(uuid: UUID, username: String, option: FakePlayerOption, spawnCallback: Consumer<CombatDummy>?):
    CombatPlayer(uuid, username, FakeConnection()) {

    private val CONNECTION_MANAGER = MinecraftServer.getConnectionManager()

    private var option: FakePlayerOption? = null

    private val navigator = Navigator(this)
    val controller = CombatDummyController(this)

    private var spawnListener: EventListener<PlayerSpawnEvent>? = null

    init{
        this.option = option


        if (spawnCallback != null) {
            spawnListener = EventListener.builder(
                PlayerSpawnEvent::class.java
            )
                .handler { event: PlayerSpawnEvent ->
                    if(event.player !is CombatDummy) return@handler
                    if ((event.player as CombatDummy) == this) if (event.isFirstSpawn) {
                        spawnCallback.accept(this)
                        MinecraftServer.getGlobalEventHandler().removeListener(spawnListener!!)
                    }
                }.build()
            MinecraftServer.getGlobalEventHandler().addListener(spawnListener!!)
        }
        CONNECTION_MANAGER.startPlayState(this, option.isRegistered)
    }
    companion object{

            fun dummyFromId(id: Long): CompletableFuture<CombatDummy> {
                val future = CompletableFuture<CombatDummy>()
                val uuid = UUID(0, id)
                val username = "Dummy$id"
                val option = FakePlayerOption()
                val spawnCallback = Consumer<CombatDummy> { player ->
                    future.complete(player)
                }
                CombatDummy(uuid, username, option, spawnCallback)
                return future
            }
        }


    override fun kill() {
        super.kill()
       Manager.scheduler.scheduleTask({
           respawn()
       }, TaskSchedule.tick(10), TaskSchedule.stop())
    }


    override fun update(time: Long) {
        super.update(time)
        // Path finding
        navigator.tick()
    }

    override fun setInstance(instance: Instance, spawnPosition: Pos): CompletableFuture<Void?>? {
        navigator.setPathFinder(HydrazinePathFinder(navigator.pathingEntity, instance.instanceSpace))
        return super.setInstance(instance, spawnPosition)
    }

    override fun updateNewViewer(player: Player) {
        player.sendPacket(addPlayerToList)
        handleTabList(player.playerConnection)
        super.updateNewViewer(player)
    }

    override fun showPlayer(connection: PlayerConnection) {
        super.showPlayer(connection)
        handleTabList(connection)
    }

    fun getNavigator(): Navigator {
        return navigator
    }

    private fun handleTabList(connection: PlayerConnection) {
        if (!option!!.isInTabList) {
            // Remove from tab-list
            MinecraftServer.getSchedulerManager().buildTask {
                connection.sendPacket(
                    removePlayerToList
                )
            }.delay(20, TimeUnit.SERVER_TICK).schedule()
        }
    }
}