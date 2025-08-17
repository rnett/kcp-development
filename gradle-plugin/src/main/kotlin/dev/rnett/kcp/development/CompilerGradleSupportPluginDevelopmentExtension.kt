package dev.rnett.kcp.development

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public abstract class CompilerGradleSupportPluginDevelopmentExtension {
    public companion object {
        public const val NAME: String = "compilerSupportPluginDevelopment"
    }

    public abstract val compilerPluginProjectPath: Property<String>
    public abstract val additionalLibraryProjectPaths: MapProperty<String, String>
}