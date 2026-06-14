plugins {
	id("net.neoforged.moddev")
	id("org.jetbrains.kotlin.jvm")
	id("com.gradleup.shadow")
}

neoForge {
	version = providers.gradleProperty("neoforge_version").get()

	runs {
		create("client") {
			client()
		}
		create("server") {
			server()
			programArgument("--nogui")
		}
	}

	mods {
		create("halfbright") {
			sourceSet(sourceSets.main.get())
		}
	}
}

val shadowBundle by configurations.creating

dependencies {
	implementation(project(":common"))
	add("shadowBundle", project(":common"))
}

tasks.processResources {
	val version = project.version
	inputs.property("version", version)

	filesMatching("META-INF/neoforge.mods.toml") {
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
