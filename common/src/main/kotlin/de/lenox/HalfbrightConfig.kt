package de.lenox

import com.google.gson.GsonBuilder
import java.nio.file.Files
import java.nio.file.Path

object HalfbrightConfig {
    private val path: Path = Path.of("config", "halfbright.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    var enabled: Boolean = false

    fun load() {
        try {
            if (Files.exists(path)) {
                Files.newBufferedReader(path).use { reader ->
                    val data = gson.fromJson(reader, ConfigData::class.java)
                    if (data != null) {
                        enabled = data.enabled
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
                gson.toJson(ConfigData(enabled), writer)
            }
        } catch (e: Exception) {
            Halfbright.LOGGER.error("Failed to save halfbright config", e)
        }
    }

    private data class ConfigData(val enabled: Boolean)
}
