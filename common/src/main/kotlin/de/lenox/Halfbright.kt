package de.lenox

import org.slf4j.LoggerFactory

object Halfbright {
	const val MOD_ID = "halfbright"
	val LOGGER = LoggerFactory.getLogger(MOD_ID)

	fun init() {
		LOGGER.info("Hello from Architectury Common!")
		HalfbrightConfig.load()
	}
}
