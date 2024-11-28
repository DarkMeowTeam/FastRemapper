import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "net.darkmeow"
version = "1.0"

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

gradlePlugin {
    plugins {
        create("fast-remapper") {
            id = "net.darkmeow.fast-remapper"
            displayName = "fast-remapper"
            description = "Remap tools for Minecraft mods"
            implementationClass = "net.darkmeow.remapper.FastRemapperPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("it.unimi.dsi:fastutil:8.5.12")

    implementation("org.ow2.asm:asm-commons:9.6")
    implementation("org.ow2.asm:asm-tree:9.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

kotlin {
    val jvmArgs = mutableSetOf<String>()
    (rootProject.findProperty("kotlin.daemon.jvm.options") as? String)
        ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
    System.getProperty("gradle.kotlin.daemon.jvm.options")
        ?.split("\\s+".toRegex())?.toCollection(jvmArgs)
    kotlinDaemonJvmArgs = jvmArgs.toList()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-Xlambdas=indy",
                "-Xjvm-default=all",
                "-Xbackend-threads=0"
            )
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "net.darkmeow"
            artifactId = "fast-remapper"
            version = "1.0"
        }
    }
    repositories {
        mavenLocal()
    }
}