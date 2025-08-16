package dev.rnett.kcp.development.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class CompilerPluginGenerateServicesTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val compilerPluginRegistrar: Property<String>

    @get:Input
    @get:Optional
    abstract val commandLineProcessor: Property<String>

    @get:OutputDirectory
    abstract val resourcesDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        resourcesDirectory.get().asFile.mkdirs()
        commandLineProcessor.orNull?.let {
            resourcesDirectory.file("META-INF/services/org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor").get().asFile.apply {
                parentFile.mkdirs()
                writeText(it)
            }
        }
        compilerPluginRegistrar.orNull?.let {
            resourcesDirectory.file("META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar").get().asFile.apply {
                parentFile.mkdirs()
                writeText(it)
            }
        }
    }

}