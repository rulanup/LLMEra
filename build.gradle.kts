plugins {
    id("java")
    // NeoForge Userdev plugin provides the development environment
    // Replace the version with the latest available if needed
    id("net.neoforged.gradle.userdev") version "7.0.151"
}

// Configure the Java toolchain – NeoForge currently targets Java 21 for Minecraft 1.21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    // NeoForge Maven repository
    maven {
        name = "NeoForge"
        url = uri("https://maven.neoforged.net/releases")
    }
    // Create (and its dependencies) are hosted on Tterrag's Maven
    maven {
        name = "Create"
        url = uri("https://maven.tterrag.com/")
    }
}

// Define the version of Create you want to depend on. Update this property as needed.
val createVersion = "0.5.1.f+1.21"

dependencies {
    // NeoForge – replace the version with the latest stable build for your target MC version
    implementation("net.neoforged:neoforge:20.4.122-beta")
    // Create – compileOnly is sufficient because Create will be present at runtime on a modded client/server
    compileOnly("com.simibubi.create:create:${createVersion}")
    // Use runtimeOnly if you want to bundle Create with your mod (generally not recommended)
}

// Ensure the JAR contains the necessary metadata for Forge/NeoForge to load the mod.
tasks.withType<Jar> {
    manifest {
        attributes(
            "Specification-Title" to project.name,
            "Specification-Version" to project.version,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "YourName"
        )
    }
}
