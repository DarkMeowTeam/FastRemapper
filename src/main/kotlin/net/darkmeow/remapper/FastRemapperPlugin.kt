package net.darkmeow.remapper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar
import java.io.File

class FastRemapperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        logger = project.logger
        Shared.init(
            File(project.gradle.gradleUserHomeDir, "caches/fast-remapper/maven"),
            File(project.gradle.gradleUserHomeDir, "caches/fast-remapper")
        )
        project.extensions.create("fastRemapper", FastRemapperExtension::class.java).apply {
            projectCacheDir = File(project.projectDir, ".gradle/fast-remapper").also { it.mkdirs() }
        }
    }
    companion object {
        lateinit var logger: Logger
    }
}