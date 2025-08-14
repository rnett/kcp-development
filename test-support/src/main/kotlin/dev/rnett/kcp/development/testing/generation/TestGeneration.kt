package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.generation.configuration.ConfigurationHost
import dev.rnett.kcp.development.testing.generation.configuration.RuntimeConfigurationMethodModel
import dev.rnett.kcp.development.testing.preprocessors.PackagePreprocessor
import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.runtime.useTestRuntime
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.generators.InconsistencyChecker
import org.jetbrains.kotlin.generators.MethodGenerator
import org.jetbrains.kotlin.generators.NewTestGeneratorImpl
import org.jetbrains.kotlin.generators.ReflectionBasedTargetBackendComputer
import org.jetbrains.kotlin.generators.TestGroupSuite
import org.jetbrains.kotlin.generators.forEachTestClassParallel
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.reflect.full.createInstance

//TODO imports and package as directives, with default methods on TestGenerator

abstract class TestGenerator : ConfigurationHost() {
    val disableAutogeneration: Boolean get() = false

    abstract fun TestGroupGeneration.generateTests()

    open fun coreDefaultConfiguration(builder: TestConfigurationBuilder) {
        with(builder) {
            defaultDirectives {
                JVM_TARGET.with(JvmTarget.JVM_17)
                +FULL_JDK
                +WITH_STDLIB
            }
            useTestRuntime()
            useAdditionalService<KotlinStandardLibrariesPathProvider> { ClasspathBasedStandardLibrariesPathProvider }
        }
    }

    open fun defaultConfiguration(builder: TestConfigurationBuilder) {
        builder.useSourcePreprocessor(::PackagePreprocessor)
    }

    private fun doGeneration(group: TestGroupGeneration): Unit = with(group) {
        if (!disableAutogeneration)
            autoGenerateTests()
        generateTests()
    }

    init {
        check(this::class.java.canonicalName != null) { "TestGenerator class must have a canonical name" }
        check(this::class.java.simpleName != null) { "TestGenerator class must have a simple name" }
    }

    val testSuite by lazy {
        val suite = TestSuiteGeneration(TestGroupSuite(ReflectionBasedTargetBackendComputer), setOf(this::class))
        suite.testGroup(
            testDataRoot = "src/testData",
            testsRoot = "src/test-gen",
        ) {
            doGeneration(this)
        }
        suite
    }

    @OptIn(ExperimentalPathApi::class)
    internal fun generateSuite(dryRun: Boolean = false, additionalMethodGenerators: List<MethodGenerator<Nothing>> = emptyList()) {
        Path("src/test-gen").deleteRecursively()

        testSuite.internal.forEachTestClassParallel { testClass ->
            val (changed, testSourceFilePath) = NewTestGeneratorImpl(additionalMethodGenerators + RuntimeConfigurationMethodModel.Generator)
                .generateAndSave(testClass, dryRun, TestGeneratorUtil.getMainClassName())
            if (changed) {
                InconsistencyChecker.inconsistencyChecker(dryRun).add(testSourceFilePath)
            }
        }
    }

    override fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        coreDefaultConfiguration(builder)
        val testClass = testSuite.getTestClass(testInstance::class.java.name) ?: return
        testClass.applyConfiguration(testInstance, builder)
    }

    fun TestGroupGeneration.configure(configurator: Configurator) {
        addConfigurator(configurator)
    }

    fun TestClassGeneration.configure(configurator: Configurator) {
        addConfiguration(configurator)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val generator = args.getOrNull(0)
            if (generator != null) {
                val cls = Class.forName(generator)
                if (TestGenerator::class.java.isAssignableFrom(cls)) {
                    val instance = (cls.kotlin.objectInstance ?: cls.kotlin.createInstance()) as TestGenerator
                    instance.generateSuite()
                } else
                    error("Class $generator is not a subtype of dev.rnett.kcp.development.testing.generation.TestGenerator")
            } else {
                AutoGenerator.generateSuite()
            }
        }
    }
}

object AutoGenerator : TestGenerator() {
    override fun TestGroupGeneration.generateTests() {
    }
}