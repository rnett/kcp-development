package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.generation.configuration.ConfigurationHost
import dev.rnett.kcp.development.testing.generation.configuration.RuntimeConfigurationMethodModel
import dev.rnett.kcp.development.testing.preprocessors.PackagePreprocessor
import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.runtime.useTestRuntime
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.generators.MethodGenerator
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.listDirectoryEntries
import kotlin.reflect.full.createInstance

//TODO imports directives, with default methods on TestGenerator

abstract class TestGenerator : ConfigurationHost() {
    open val testDataRoot: Path = Path("src/testData")
    open val cleanGeneratedRoot: Boolean = true
    open val testGenerationRoot: Path = Path("src/test-gen")

    open val testsRootPackage: String = this::class.java.packageName

    open val disableAutogeneration: Boolean get() = false

    open fun TestGenerationBuilder.generateTests() {}

    open val imports: Set<String> = emptySet()
    open val additionalMethods: List<MethodGenerator<*>> = emptyList()

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

    private fun createTestGenerationBuilder(): TestGenerationBuilderImplementation = TestGenerationBuilderImplementation(TestGenerationPath.Root(testDataRoot)).apply {
        method(RuntimeConfigurationMethodModel(setOf(this@TestGenerator::class)))
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

    val testSpecs by lazy {
        createTestGenerationBuilder().createGenerationSpecs(testsRootPackage)
    }

    @OptIn(ExperimentalPathApi::class)
    internal fun generateSuite(dryRun: Boolean = false, additionalMethodGenerators: List<MethodGenerator<Nothing>> = emptyList()) {
        if (cleanGeneratedRoot) {
            testGenerationRoot.listDirectoryEntries().forEach { it.deleteRecursively() }
        }

        generateTestGroupSuiteWithJUnit5(
            additionalMethodGenerators = additionalMethodGenerators + additionalMethods + RuntimeConfigurationMethodModel.Generator
        ) {
            testGroup(
                testGenerationRoot.toString(),
                testDataRoot.toString()
            ) {
                testSpecs.forEach { (_, spec) ->
                    spec.applyTo(null, this)
                }
            }
        }
    }

    override fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        coreDefaultConfiguration(builder)
        defaultConfiguration(builder)
        val testClass = testSpecs[testInstance::class.java.name] ?: error("No test spec for ${testInstance::class.java.name}")
        testClass.generatedFrom.applyConfiguration(builder)
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
    override val testsRootPackage: String = System.getProperty("default.package") ?: "tests.autogenerated"
    override fun TestGenerationBuilder.generateTests() {
    }
}