package dev.rnett.kcp.development

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

public abstract class CompilerPluginDevelopmentExtension {
    public companion object {
        public const val NAME: String = "compilerPluginDevelopment"
        public const val TEST_GENERATOR_MAIN: String = "dev.rnett.kcp.development.testing.generation.TestGenerationBootstrapper"
    }

    /**
     * Whether to use [TestGenerator] in test-support. Defaults to true.
     *
     * If true, set [testGenerator] to your [TestGenerator] class.
     *
     * If false, set [testGenerator] to the class of your `main` function that does test generation.
     *
     */
    public abstract val useTestGenerator: Property<Boolean>
    public abstract val testGenerator: Property<String>

    public abstract val addTestSupportDependency: Property<Boolean>
    public abstract val addCoreDependency: Property<Boolean>

    public abstract val testGenerationRoot: DirectoryProperty
    public abstract val testDataRoot: DirectoryProperty

    public abstract val commandLineProcessor: Property<String>
    public abstract val compilerPluginRegistrar: Property<String>

    public abstract val parallelTests: Property<Boolean>

    init {
        this::class.objectInstance
    }
}