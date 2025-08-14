package dev.rnett.kcp.development

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class CompilerGradleSupportPluginDevelopmentExtension {
    companion object {
        const val NAME = "compilerSupportPluginDevelopment"
    }

    abstract val compilerPluginProjectPath: Property<String>
    abstract val additionalLibraryProjectPaths: MapProperty<String, String>
}