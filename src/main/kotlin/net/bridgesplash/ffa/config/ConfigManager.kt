package net.bridgesplash.ffa.config

import net.bridgesplash.sploosh.config.ConfigHelper
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.notExists
import kotlin.io.path.walk

object ConfigManager {

    val maps = mutableListOf<MapConfig>()
    val mapsConfigFolder: Path = Path.of("maps")

    @OptIn(ExperimentalPathApi::class)
    fun loadConfigs(){
        if(mapsConfigFolder.notExists()){
            mapsConfigFolder.toFile().mkdir()
            ConfigHelper.writeObjectToPath(mapsConfigFolder.resolve("example.json"), MapConfig())
        }
        mapsConfigFolder.walk().forEach {
            if(it.fileName.toString().endsWith(".json")){
                val mapConfig = ConfigHelper.readConfigFile<MapConfig>(it)
                maps.add(mapConfig)
            }
        }
    }

}