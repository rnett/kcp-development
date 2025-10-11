package dev.rnett.kcp.development

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public abstract class CompilerGradleSupportPluginDevelopmentExtension {
    public companion object {
        public const val NAME: String = "compilerSupportPluginDevelopment"
    }

    /**
     * The absolute project path of the compiler plugin project.  Defaults to `:compiler-plugin` if not set.
     */
    public abstract val compilerPluginProjectPath: Property<String>

    /**
     * Any additional projects to add to the buildconfig. Must be `{name -> {absolute project path}`. Optional, defaults to empty.
     */
    public abstract val additionalLibraryProjectPaths: MapProperty<String, String>
}