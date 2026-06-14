package de.lenox.fabric

import de.lenox.Halfbright
import net.fabricmc.api.ModInitializer

object HalfbrightFabric : ModInitializer {
	override fun onInitialize() {
		Halfbright.init()
		Halfbright.LOGGER.info("Hello from Architectury Fabric!")
	}
}
