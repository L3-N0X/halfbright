plugins {
	id("dev.architectury.loom")
	id("architectury-plugin")
	id("org.jetbrains.kotlin.jvm")
}

architectury {
	common("fabric", "neoforge")
}

dependencies {
	val minecraftVersion = providers.gradleProperty("minecraft_version").get()
	minecraft("com.mojang:minecraft:$minecraftVersion")

	compileOnly("org.spongepowered:mixin:0.8.7")
}
