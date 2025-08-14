package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.configuration.ConfigurationHost
import dev.rnett.kcp.development.testing.configuration.Configurator
import dev.rnett.kcp.development.testing.configuration.ConfigureWith
import org.jetbrains.kotlin.generators.InconsistencyChecker
import org.jetbrains.kotlin.generators.MethodGenerator
import org.jetbrains.kotlin.generators.NewTestGeneratorImpl
import org.jetbrains.kotlin.generators.ReflectionBasedTargetBackendComputer
import org.jetbrains.kotlin.generators.TestGroupSuite
import org.jetbrains.kotlin.generators.forEachTestClassParallel
import org.jetbrains.kotlin.generators.model.AnnotationArgumentModel
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.util.TestGeneratorUtil
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.reflect.full.createInstance

//TODO I want to be able to set default configurators for a scope

//TODO I'd like to be able to have a .directives or similar file in the test data that affects it and subdirs

abstract class TestGenerator : ConfigurationHost {
    val disableAutogeneration: Boolean get() = false
    abstract fun TestGroupGeneration.generateTests()

    private fun doGeneration(group: TestGroupGeneration): Unit = with(group) {
        if (!disableAutogeneration)
            autoGenerateTests()
        generateTests()
    }

    val testSuite by lazy {
        val suite = TestSuiteGeneration(TestGroupSuite(ReflectionBasedTargetBackendComputer), this::class)
        suite.testGroup(
            testDataRoot = "src/testData",
            testsRoot = "src/test-gen",
        ) {
            annotation(AnnotationModel(ConfigureWith::class.java, listOf(AnnotationArgumentModel("host", this@TestGenerator::class))))
            doGeneration(this)
        }
        suite
    }

    @OptIn(ExperimentalPathApi::class)
    internal fun generateSuite(dryRun: Boolean = false, additionalMethodGenerators: List<MethodGenerator<Nothing>> = emptyList()) {
        Path("src/test-gen").deleteRecursively()

        testSuite.internal.forEachTestClassParallel { testClass ->
            val (changed, testSourceFilePath) = NewTestGeneratorImpl(additionalMethodGenerators)
                .generateAndSave(testClass, dryRun, TestGeneratorUtil.getMainClassName())
            if (changed) {
                InconsistencyChecker.inconsistencyChecker(dryRun).add(testSourceFilePath)
            }
        }
    }

    override fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        val testClass = testSuite.getTestClass(testInstance::class.java.name) ?: return
        testClass.applyConfiguration(builder)
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