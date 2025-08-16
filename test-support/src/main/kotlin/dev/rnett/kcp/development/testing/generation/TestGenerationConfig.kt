package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel

interface TestGenerationConfigBuilder {

    fun testsPackage(vararg packageNames: String)

    fun annotation(annotation: AnnotationModel)
    fun method(method: MethodModel)

    fun addLevel(level: TestLevel)
    fun removeLevel(level: TestLevel)
}

class TestGenerationConfig() : TestGenerationConfigBuilder {
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