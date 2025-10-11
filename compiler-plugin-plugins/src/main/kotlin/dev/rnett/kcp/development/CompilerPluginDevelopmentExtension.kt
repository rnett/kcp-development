package dev.rnett.kcp.development

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

public abstract class CompilerPluginDevelopmentExtension {
    public companion object {
        public const val NAME: String = "compilerPluginDevelopment"
        public const val TEST_GENERATOR_MAIN: String = "dev.rnett.kcp.development.testing.generation.TestGenerationBootstrapper"
    }

    /**
     * Whether to use a `BaseTestGenerator`-extending class to generate tests. Defaults to true.
     *
     * If true, set [testGenerator] to your `BaseTestGenerator` class.
     *
     * If false, set [testGenerator] to the class of your `main` function that does test generation.
     *
     */
    public abstract val useTestGenerator: Property<Boolean>

    /**
     * The class to use for test generation. Typically your object that extends `BaseTestGenerator`.
     *
     * If not set, no tests will be generated.
     *
     * @see useTestGenerator
     */
    public abstract val testGenerator: Property<String>

    /**
     * Whether to add the kcp-development test support library to the test fixtures classpath.  Defaults to true.
     */
    public abstract val addTestSupportDependency: Property<Boolean>

    /**
     * Whether to add the kcp-development compiler plugin core library to the main classpath.  Defaults to true.
     */
    public abstract val addCoreDependency: Property<Boolean>

    /**
     * The root of the generated tests. Defaults to `src/test-gen`.
     */
    public abstract val testGenerationRoot: DirectoryProperty

    /**
     * The root of the test data files.  Defaults to `src/testData`.
     */
    public abstract val testDataRoot: DirectoryProperty

    /**
     * The full class name of your `CommandLineProcessor`.
     * If present and the `services` plugin is applied, the plugin will create a service declaration file for it.
     */
    public abstract val commandLineProcessor: Property<String>

    /**
     * The full class name of your `ComponentRegistrar`.
     * If present and the `services` plugin is applied, the plugin will create a service declaration file for it.
     * It will also be used in any `BaseTestGenerator`-generated tests.
     */
    public abstract val compilerPluginRegistrar: Property<String>

    /**
     * Whether to run the tests in parallel.  Defaults to true.
     */
    public abstract val parallelTests: Property<Boolean>

    init {
        this::class.objectInstance
    }
}