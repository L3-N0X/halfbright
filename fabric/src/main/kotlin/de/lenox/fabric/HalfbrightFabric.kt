package de.lenox.fabric

import de.lenox.Halfbright
import de.lenox.command.HalfbrightCommand
import net.fabricmc.api.ModInitializer

object HalfbrightFabric : ModInitializer {
	override fun onInitialize() {
		Halfbright.init()
		Halfbright.LOGGER.info("Hello from Architectury Fabric!")

		net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			HalfbrightCommand.register(dispatcher)
		}
	}
}
