package de.lenox.neoforge

import de.lenox.Halfbright
import net.neoforged.fml.common.Mod

@Mod(Halfbright.MOD_ID)
object HalfbrightNeoForge {
	init {
		Halfbright.init()
		Halfbright.LOGGER.info("Hello from Architectury NeoForge!")
	}
}
