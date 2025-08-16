package dev.rnett.kcp.development

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class CompilerPluginDevelopmentExtension {
    companion object {
        const val NAME = "compilerPluginDevelopment"
        const val TEST_GENERATOR_MAIN = "dev.rnett.kcp.development.testing.generation.TestGenerationBootstrapper"
    }

    /**
     * Whether to use [TestGenerator] in test-support. Defaults to true.
     *
     * If true, set [testGenerator] to your [TestGenerator] class.
     *
     * If false, set [testGenerator] to the class of your `main` function that does test generation.
     *
     */
    abstract val useTestGenerator: Property<Boolean>
    abstract val testGenerator: Property<String>

    abstract val addTestSupportDependency: Property<Boolean>
    abstract val addCoreDependency: Property<Boolean>

    abstract val testGenerationRoot: DirectoryProperty
    abstract val testDataRoot: DirectoryProperty

    init {
        this::class.objectInstance
    }
}