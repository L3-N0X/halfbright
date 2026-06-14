package de.lenox.neoforge

import de.lenox.Halfbright
import de.lenox.command.HalfbrightCommand
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent

@Mod(Halfbright.MOD_ID)
class HalfbrightNeoForge(eventBus: IEventBus) {
	init {
		Halfbright.init()
		Halfbright.LOGGER.info("Hello from Architectury NeoForge!")

		eventBus.addListener(RegisterKeyMappingsEvent::class.java) { event ->
			event.registerCategory(de.lenox.client.HalfbrightKeybinds.HALFBRIGHT_CATEGORY)
			event.register(de.lenox.client.HalfbrightKeybinds.toggleKey)
			event.register(de.lenox.client.HalfbrightKeybinds.increaseKey)
			event.register(de.lenox.client.HalfbrightKeybinds.decreaseKey)
		}

		NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent::class.java) { event ->
			HalfbrightCommand.register(event.dispatcher)
		}
	}
}
