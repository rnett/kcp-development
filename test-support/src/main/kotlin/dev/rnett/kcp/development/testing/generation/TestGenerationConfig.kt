package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel

/**
 * Configuration options for the test class generator.
 */
public interface TestGenerationConfigBuilder {

    /**
     * Adds to the package name of generated test classes.
     */
    public fun testsPackage(vararg packageNames: String)

    /**
     * Adds annotations to the generated test classes.
     */
    public fun annotation(annotation: AnnotationModel)

    /**
     * Adds methods to the generated test classes.
     */
    public fun method(method: MethodModel)

    /**
     * Adds a test level to the generated tests.
     */
    public fun addLevel(level: TestLevel)

    /**
     * Removes a test level from the generated tests.
     */
    public fun removeLevel(level: TestLevel)
}

internal class TestGenerationConfig() : TestGenerationConfigBuilder {
    val testsPackage: MutableList<String> = mutableListOf()
    private val annotations: MutableList<AnnotationModel> = mutableListOf()
    val methods: MutableList<MethodModel> = mutableListOf()
    val levels: MutableSet<TestLevel> = mutableSetOf()

    fun annotations() = annotations + TestSpec(levels).annotations()

    override fun testsPackage(vararg packageNames: String) {
        testsPackage += packageNames.toList()
    }

    override fun annotation(annotation: AnnotationModel) {
        annotations += annotation
    }

    override fun method(method: MethodModel) {
        methods += method
    }

    override fun addLevel(level: TestLevel) {
        levels += level
    }

    override fun removeLevel(level: TestLevel) {
        levels -= level
    }
}