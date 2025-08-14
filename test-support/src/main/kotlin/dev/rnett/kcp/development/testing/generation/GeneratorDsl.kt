package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.generation.configuration.ConfigurationHost
import dev.rnett.kcp.development.testing.generation.configuration.RuntimeConfigurationMethodModel
import org.jetbrains.kotlin.generators.TestGroup
import org.jetbrains.kotlin.generators.TestGroupSuite
import org.jetbrains.kotlin.generators.getDefaultSuiteTestClassName
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.RunTestMethodModel
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import kotlin.reflect.KClass

sealed class TestClassConfig(private val parent: TestClassConfig?) {
    private val _configurators = mutableListOf<Configurator>()

    internal fun addConfigurator(configurator: Configurator) {
        _configurators += configurator
    }

    internal val configurators: List<Configurator> get() = _configurators + (parent?.configurators.orEmpty())

    private val _annotations = mutableListOf<AnnotationModel>()

    fun annotation(annotation: AnnotationModel) {
        _annotations += annotation
    }

    val annotations: List<AnnotationModel> get() = _annotations + (parent?.annotations.orEmpty())
}

class TestSuiteGeneration(val internal: TestGroupSuite, val configurationHosts: Set<KClass<out ConfigurationHost>>) : TestClassConfig(null) {
    private val _testClasses = mutableMapOf<String, TestClassGeneration>()

    fun testGroup(
        testsRoot: String,
        testDataRoot: String,
        testRunnerMethodName: String = RunTestMethodModel.METHOD_NAME,
        additionalRunnerArguments: List<String> = emptyList(),
        init: TestGroupGeneration.() -> Unit
    ) {
        internal.testGroup(testsRoot, testDataRoot, testRunnerMethodName, additionalRunnerArguments) {
            TestGroupGeneration(this, this@TestSuiteGeneration).init()
        }
    }

    internal fun register(testClassName: String, testClass: TestClassGeneration) {
        val old = _testClasses.put(testClassName, testClass)
        check(old == null) { "Test class $testClassName already registered" }
    }

    fun getTestClass(testClassName: String): TestClassGeneration? = _testClasses[testClassName]
}

class TestGroupGeneration(val internal: TestGroup, val parent: TestClassConfig) : TestClassConfig(parent) {

    val suite: TestSuiteGeneration by lazy {
        when (parent) {
            is TestGroupGeneration -> parent.suite
            is TestSuiteGeneration -> parent
        }
    }

    inline fun group(block: TestGroupGeneration.() -> Unit) =
        TestGroupGeneration(internal, this).block()

    inline fun <reified T> testClass(
        suiteTestClassName: String = getDefaultSuiteTestClassName(T::class.java.simpleName),
        useJunit4: Boolean = false,
        annotations: List<AnnotationModel> = emptyList(),
        noinline init: TestClassGeneration.() -> Unit
    ) {
        internal.testClass<T>(suiteTestClassName, useJunit4, this.annotations + annotations) {
            TestClassGeneration(this, this@TestGroupGeneration).init()
        }
    }

    fun testClass(
        testBaseClass: KClass<*>,
        suiteTestClassName: String = getDefaultSuiteTestClassName(testBaseClass.java.simpleName),
        useJunit4: Boolean = false,
        annotations: List<AnnotationModel> = emptyList(),
        init: TestClassGeneration.() -> Unit
    ) {
        val testClassJava = testBaseClass.java
        internal.testClass(testClassJava, testClassJava.name, suiteTestClassName, useJunit4, this.annotations + annotations) {
            TestClassGeneration(this, this@TestGroupGeneration).init()
        }
    }
}

class TestClassGeneration(val internal: TestGroup.TestClass, val parent: TestGroupGeneration) {
    init {
        parent.suite.register(internal.suiteTestClassName, this)
        internal.method(RuntimeConfigurationMethodModel(parent.suite.configurationHosts))
    }

    internal fun applyConfiguration(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        configurators.forEach {
            with(it) { builder.configure(testInstance) }
        }
    }

    private val _configurators = mutableListOf<Configurator>()

    internal fun addConfiguration(configurator: Configurator) {
        _configurators += configurator
    }

    internal val configurators: List<Configurator> get() = _configurators + (parent?.configurators.orEmpty())
}