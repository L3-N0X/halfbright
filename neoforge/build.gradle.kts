plugins {
	id("net.neoforged.moddev")
	id("org.jetbrains.kotlin.jvm")
	id("com.gradleup.shadow")
	id("me.modmuss50.mod-publish-plugin")
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

publishMods {
	file.set(tasks.shadowJar.flatMap { it.archiveFile })
	changelog.set(providers.environmentVariable("CHANGELOG").orElse("No changelog provided."))
	type.set(me.modmuss50.mpp.ReleaseType.STABLE)
	modLoaders.add("neoforge")

	modrinth {
		projectId.set(providers.gradleProperty("modrinth_project_id").orElse("placeholder"))
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		minecraftVersions.add(providers.gradleProperty("minecraft_version").get())
	}
}
