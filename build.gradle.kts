import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val baseGroup: String by project
val baseVersion: String by project

val kotlinxCoroutineVersion: String by project

val gsonVersion: String by project
val fastutilVersion: String by project
val ow2asmVersion: String by project

group = baseGroup
version = baseVersion

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("it.unimi.dsi:fastutil:$fastutilVersion")

    implementation("org.ow2.asm:asm-commons:$ow2asmVersion")
    implementation("org.ow2.asm:asm-tree:$ow2asmVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "fast-remapper"
            groupId = baseGroup
            version = baseVersion

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}