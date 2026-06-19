package de.lenox.fabric

import de.lenox.Halfbright
import de.lenox.command.HalfbrightCommand
import net.fabricmc.api.ModInitializer

object HalfbrightFabric : ModInitializer {
	override fun onInitialize() {
		Halfbright.init()

		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(de.lenox.client.HalfbrightKeybinds.toggleKey)
		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(de.lenox.client.HalfbrightKeybinds.increaseKey)
		net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(de.lenox.client.HalfbrightKeybinds.decreaseKey)

		net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			HalfbrightCommand.register(dispatcher)
		}
	}
}
