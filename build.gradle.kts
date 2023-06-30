import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.bridgesplash"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.Minestom.Minestom:Minestom:954e8b3915")
    implementation("net.bridgesplash.sploosh:Sploosh:1.1.3:all")
    implementation("io.github.bloepiloepi:BridgeSplashPvP:1.1.0")

    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    compileOnly("org.litote.kmongo:kmongo-coroutine-serialization:4.9.0")
    compileOnly("org.litote.kmongo:kmongo-id:4.9.0")

    testImplementation(kotlin("test"))
    testImplementation("net.bridgesplash.sploosh:Sploosh:1.1.3:all")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    testImplementation("io.github.bloepiloepi:BridgeSplashPvP:1.1.0")
}

tasks{
    jar {
        manifest {
            attributes["Main-Class"] = "net.bridgesplash.ffa.FreeForAll"
            attributes["Multi-Release"] = true
        }
    }
    named<ShadowJar>("shadowJar") {
        dependsOn(jar)
        archiveBaseName.set("duels")

        mergeServiceFiles()
        dependencies {}
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        workingDir = file("run")
    }
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xinline-classes")
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
}


kotlin {
    jvmToolchain(17)
}