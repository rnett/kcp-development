package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.annotations.DelicateKcpDevApi
import dev.rnett.kcp.development.testing.SysProps
import dev.rnett.kcp.development.testing.configuration.withCompilerPluginRegistrar
import dev.rnett.kcp.development.testing.directives.UtilityDirectives.BOX_OPT_IN
import dev.rnett.kcp.development.testing.directives.UtilityDirectives.IMPORTS
import dev.rnett.kcp.development.testing.directives.preprocessors.useBoxOptInPreprocessor
import dev.rnett.kcp.development.testing.directives.preprocessors.useImportsPreprocessor
import dev.rnett.kcp.development.testing.generation.configuration.ConfigurationHost
import dev.rnett.kcp.development.testing.generation.configuration.RuntimeConfigurationMethodModel
import dev.rnett.kcp.development.testing.preprocessors.PackagePreprocessor
import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.runtime.useTestRuntime
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.OPT_IN
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

/**
 * A test generator. Configures test generation, and can override [generateTests] to generate tests using the DSL.
 */
@OptIn(DelicateKcpDevApi::class)
public abstract class BaseTestGenerator : ConfigurationHost() {
    /**
     * The root of the test data. Autoconfigured by the Gradle plugin.
     */
    public open val testDataRoot: Path = Path(SysProps.testDataRoot)

    /**
     * Whether to clean the root of the generated tests before generating.  Defaults to true.
     */
    public open val cleanGeneratedRoot: Boolean = true

    /**
     * The root of the generated tests.  Autoconfigured by the Gradle plugin.
     */
    public open val testGenerationRoot: Path = Path(SysProps.testGenRoot)

    /**
     * The package name of the generated tests.  Defaults to the package of the test generator.
     */
    public open val testsRootPackage: String = this::class.java.packageName

    /**
     * Whether to disable autogeneration.  Defaults to false. If true, override [generateTests] or else no tests will be generated.
     */
    public open val disableAutogeneration: Boolean get() = false

    /**
     * A [CompilerPluginRegistrar] to register the compiler plugin.  Defaults to the plugin registrar specified in the
     * Gradle plugin.
     */
    @OptIn(ExperimentalCompilerApi::class)
    public open val compilerPluginRegistrar: CompilerPluginRegistrar? by lazy {
        SysProps.pluginRegistrar?.let {
            val cls = Class.forName(it).kotlin
            check(cls.isSubclassOf(CompilerPluginRegistrar::class)) { "Cannot use $it as a compiler plugin registrar, it is not a subclass of CompilerPluginRegistrar" }
            @Suppress("UNCHECKED_CAST")
            (cls as KClass<out CompilerPluginRegistrar>).createInstance()
        }
    }

    /**
     * Override this to generate your tests.
     */
    public open fun TestGenerationBuilder.generateTests() {}

    /**
     * Imports to add to all test data files, via a preprocessor.
     */
    public open val imports: Set<String> = emptySet()

    /**
     * Opt-ins to add to all test data files, via a preprocessor.
     */
    public open val optIns: Set<String> = emptySet()

    /**
     * Opt-ins to add to all test data files as box tests, via a preprocessor.
     */
    public open val boxOptIns: Set<String> = emptySet()

    /**
     * Called to adjust the compiler configuration before tests are run.
     */
    public open fun adjustCompilerConfiguration(module: TestModule, configuration: CompilerConfiguration) {

    }

    private inner class Configurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
        override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
            adjustCompilerConfiguration(module, configuration)
        }
    }

    /**
     * Additional methods to add to generated test classes.
     */
    public open val additionalMethods: List<MethodModel<*>> = emptyList()

    /**
     * The core configuration used to set up the other options in this class. Override at your own risk. If you do not call super things will not work as expected.
     */
    @DelicateKcpDevApi
    @OptIn(ExperimentalCompilerApi::class)
    public open fun coreConfiguration(builder: TestConfigurationBuilder) {
        with(builder) {
            defaultDirectives {
                JVM_TARGET.with(JvmTarget.JVM_17)
                +FULL_JDK
                +WITH_STDLIB
            }
            useTestRuntime()
            useAdditionalService<KotlinStandardLibrariesPathProvider> { ClasspathBasedStandardLibrariesPathProvider }

            useImportsPreprocessor()
            useBoxOptInPreprocessor()
            defaultDirectives {
                OPT_IN.with(optIns.toList())
                BOX_OPT_IN.with(boxOptIns.toList())

                val actualImports = imports + optIns + boxOptIns
                IMPORTS.with(actualImports.toList())
            }

            useConfigurators(::Configurator)
            compilerPluginRegistrar?.let { withCompilerPluginRegistrar(it) }
        }
    }

    /**
     * The default configuration applied to all tests.
     * Override at your own risk, but not suite as dangerous as [coreConfiguration].
     */
    @DelicateKcpDevApi
    public open fun defaultConfiguration(builder: TestConfigurationBuilder) {
        with(builder) {
            useSourcePreprocessor({ PackagePreprocessor(it, testDataRoot.pathString) })
        }
    }

    private fun createTestGenerationBuilder(): TestGenerationBuilderImplementation = TestGenerationBuilderImplementation(TestGenerationPathSpec.Root(testDataRoot)).apply {
        method(RuntimeConfigurationMethodModel(setOf(this@BaseTestGenerator::class)))
        if (!disableAutogeneration) {
            group("auto") {
                autoGenerateTests()
            }
        }
        generateTests()
    }

    init {
        check(this::class.java.canonicalName != null) { "TestGenerator class must have a canonical name" }
        check(this::class.java.simpleName != null) { "TestGenerator class must have a simple name" }
    }

    internal val testSpecs: Map<String, GeneratedTestSpec> by lazy {
        createTestGenerationBuilder().createGenerationSpecs(testsRootPackage)
    }

    @OptIn(ExperimentalPathApi::class)
    internal fun generateSuite(dryRun: Boolean = false, additionalMethodGenerators: List<MethodModel<*>> = emptyList()) {
        if (cleanGeneratedRoot) {
            testGenerationRoot.listDirectoryEntries().forEach { it.deleteRecursively() }
        }

        val additionalMethods: List<MethodModel<*>> = additionalMethodGenerators + additionalMethods
        
        generateTestGroupSuiteWithJUnit5(
            dryRun = dryRun
        ) {
            this.testGroup(
                testGenerationRoot.toString(),
                testDataRoot.toString()
            ) {
                testSpecs.forEach { (_, spec) ->
                    spec.applyTo(null, this, additionalMethods)
                }
            }
        }
    }

    final override fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        coreConfiguration(builder)
        defaultConfiguration(builder)
        val testClass = testSpecs[testInstance::class.java.name] ?: error("No test spec for ${testInstance::class.java.name}")
        testClass.generatedFrom.applyConfiguration(builder)
    }
}
