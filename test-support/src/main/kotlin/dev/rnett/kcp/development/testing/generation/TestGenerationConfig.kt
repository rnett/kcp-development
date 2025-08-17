package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel

public interface TestGenerationConfigBuilder {

    public fun testsPackage(vararg packageNames: String)

    public fun annotation(annotation: AnnotationModel)
    public fun method(method: MethodModel)

    public fun addLevel(level: TestLevel)
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