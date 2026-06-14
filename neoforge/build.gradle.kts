plugins {
	id("net.neoforged.moddev")
	id("org.jetbrains.kotlin.jvm")
	id("com.gradleup.shadow")
}

evaluationDependsOn(":common")

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
			sourceSet(project(":common").extensions.getByType<org.gradle.api.tasks.SourceSetContainer>().getByName("main"))
		}
	}
}

sourceSets {
	main {
		resources {
			srcDir("../common/src/main/resources")
		}
	}
}

val shadowBundle by configurations.creating

dependencies {
	implementation(project(":common"))
	add("shadowBundle", project(":common"))

	compileOnly("net.caffeinemc:sodium-neoforge-api:0.8.12+mc26.1.2")
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
