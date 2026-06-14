plugins {
	id("dev.architectury.loom")
	id("architectury-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("com.gradleup.shadow")
}

architectury {
	platformSetupLoomIde()
	fabric()
}

val shadowBundle by configurations.creating

dependencies {
	val minecraftVersion = providers.gradleProperty("minecraft_version").get()
	val loaderVersion = providers.gradleProperty("loader_version").get()
	val fabricKotlinVersion = providers.gradleProperty("fabric_kotlin_version").get()
	val fabricApiVersion = providers.gradleProperty("fabric_api_version").get()

	minecraft("com.mojang:minecraft:$minecraftVersion")
	implementation("net.fabricmc:fabric-loader:$loaderVersion")
	implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
	implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

	implementation(project(":common"))
	add("shadowBundle", project(":common"))
}

tasks.processResources {
	val version = project.version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.jar {
	archiveClassifier.set("thin")
}

tasks.shadowJar {
	exclude("architectury.common.json")
	configurations = listOf(shadowBundle)
	archiveClassifier.set("")
}
