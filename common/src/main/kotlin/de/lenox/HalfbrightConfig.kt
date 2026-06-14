package de.lenox

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path

object HalfbrightConfig {
    private val path: Path = Path.of("config", "halfbright.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    var enabled: Boolean = false
    var minLightLevel: Float = 6.0f

    fun load() {
        try {
            if (Files.exists(path)) {
                Files.newBufferedReader(path).use { reader ->
                    val data = gson.fromJson(reader, ConfigData::class.java)
                    if (data != null) {
                        enabled = data.enabled
                        minLightLevel = data.minLightLevel
                    }
                }
            } else {
                save()
            }
        } catch (e: Exception) {
            Halfbright.LOGGER.error("Failed to load halfbright config", e)
        }
    }

    fun save() {
        try {
            val parent = path.parent
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent)
            }
            Files.newBufferedWriter(path).use { writer ->
                gson.toJson(ConfigData(enabled, minLightLevel), writer)
            }
        } catch (e: Exception) {
            Halfbright.LOGGER.error("Failed to save halfbright config", e)
        }
    }

    private data class ConfigData(val enabled: Boolean, val minLightLevel: Float = 6.0f)
}
