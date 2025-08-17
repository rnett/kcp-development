package dev.rnett.kcp.development

import dev.rnett.kcp.development.tasks.CompilerPluginGenerateServicesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the

public class CompilerPluginAutoServicesPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(CompilerPluginBasePlugin::class.java)

        val extension = the<CompilerPluginDevelopmentExtension>()

        val resourcesDir = layout.buildDirectory.dir("generated/sources/kcpDevServices/resources")

        val generatePluginServices by tasks.registering(CompilerPluginGenerateServicesTask::class) {
            this.compilerPluginRegistrar.set(extension.compilerPluginRegistrar)
            this.commandLineProcessor.set(extension.commandLineProcessor)
            this.resourcesDirectory.set(resourcesDir)
        }

        val javaExtension = the<JavaPluginExtension>()
        javaExtension.sourceSets.apply {
            named("main") {
                resources.srcDir(generatePluginServices.flatMap { it.resourcesDirectory })
            }
        }
    }
}