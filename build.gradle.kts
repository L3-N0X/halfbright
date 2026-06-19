import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.gradle.api.plugins.JavaPluginExtension

plugins {
	id("architectury-plugin") version "3.5.169"
	id("dev.architectury.loom") version "1.17.480" apply false
	id("org.jetbrains.kotlin.jvm") version "2.4.0" apply false
	id("com.gradleup.shadow") version "8.3.6" apply false
	id("net.neoforged.moddev") version "2.0.141" apply false
	id("me.modmuss50.mod-publish-plugin") version "2.0.0" apply false
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

architectury {
	minecraft = providers.gradleProperty("minecraft_version").get()
}

subprojects {
	version = rootProject.version
	group = rootProject.group

	repositories {
		mavenCentral()
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Architectury"
			url = uri("https://maven.architectury.dev/")
		}
		maven {
			name = "NeoForge"
			url = uri("https://maven.neoforged.net/releases/")
		}
		maven {
			name = "CaffeineMC"
			url = uri("https://maven.caffeinemc.net/releases")
		}
	}

	tasks.withType<JavaCompile>().configureEach {
		options.release = 21
	}

	afterEvaluate {
		extensions.configure<KotlinJvmProjectExtension> {
			compilerOptions {
				jvmTarget.set(JvmTarget.JVM_21)
			}
		}

		extensions.configure<JavaPluginExtension> {
			sourceCompatibility = JavaVersion.VERSION_21
			targetCompatibility = JavaVersion.VERSION_21
		}
	}
}
