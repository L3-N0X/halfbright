package de.lenox.neoforge

import de.lenox.Halfbright
import de.lenox.command.HalfbrightCommand
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent

@Mod(Halfbright.MOD_ID)
class HalfbrightNeoForge {
	init {
		Halfbright.init()
		Halfbright.LOGGER.info("Hello from Architectury NeoForge!")

		NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent::class.java) { event ->
			HalfbrightCommand.register(event.dispatcher)
		}
	}
}
