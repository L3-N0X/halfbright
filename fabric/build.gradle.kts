plugins {
	id("dev.architectury.loom")
	id("architectury-plugin")
	id("org.jetbrains.kotlin.jvm")
	id("com.gradleup.shadow")
	id("me.modmuss50.mod-publish-plugin")
}

architectury {
	platformSetupLoomIde()
	fabric()
}

val shadowBundle by configurations.creating

sourceSets {
	main {
		resources {
			srcDir("../common/src/main/resources")
		}
	}
}

dependencies {
	val minecraftVersion = providers.gradleProperty("minecraft_version").get()
	val loaderVersion = providers.gradleProperty("loader_version").get()
	val fabricKotlinVersion = providers.gradleProperty("fabric_kotlin_version").get()
	val fabricApiVersion = providers.gradleProperty("fabric_api_version").get()

	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
	modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
	modApi("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

	implementation(project(":common", configuration = "namedElements"))
	add("shadowBundle", project(":common"))

	modCompileOnly("net.caffeinemc:sodium-fabric-api:0.8.12+mc1.21.11")
}

tasks.processResources {
	val version = project.version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<org.gradle.api.tasks.bundling.AbstractArchiveTask>().configureEach {
	archiveBaseName.set("${rootProject.name}-${project.name}")
	archiveVersion.set("v${project.version}+${providers.gradleProperty("minecraft_version").get()}")
}

tasks.jar {
	archiveClassifier.set("thin")
}

tasks.shadowJar {
	exclude("architectury.common.json")
	configurations = listOf(shadowBundle)
	archiveClassifier.set("")
}

publishMods {
	file.set(tasks.shadowJar.flatMap { it.archiveFile })
	changelog.set(providers.environmentVariable("CHANGELOG").orElse("No changelog provided."))
	type.set(me.modmuss50.mpp.ReleaseType.STABLE)
	modLoaders.add("fabric")

	modrinth {
		projectId.set(providers.gradleProperty("modrinth_project_id").orElse("placeholder"))
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		minecraftVersions.add(providers.gradleProperty("minecraft_version").get())
	}
}
