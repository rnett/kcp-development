package dev.rnett.kcp.development.testing.generation

import org.jetbrains.kotlin.generators.dsl.TestGroup
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel
import kotlin.reflect.KClass

internal data class GeneratedTestSpec(
    val generatedFrom: TestGenerationBuilderImplementation,
    val suiteName: String,
    val testDataPath: String,
    val baseClass: KClass<*>,
    val annotations: List<AnnotationModel>,
    val methods: List<MethodModel<*>>,
    val arguments: TestArguments
) {
    fun applyTo(rootPackage: String?, testGroup: TestGroup, additionalMethods: List<MethodModel<*>> = emptyList()) {
        testGroup.testClass(
            baseClass.java,
            baseTestClassName = baseClass.java.name,
            suiteTestClassName = rootPackage?.let { it.trimEnd('.') + ".$suiteName" } ?: suiteName,
            annotations = annotations
        ) {
            additionalMethods.forEach { this.method(it) }
            methods.forEach { this.method(it) }
            model(
                relativeRootPath = testDataPath,
                recursive = arguments.recursive,
                excludeParentDirs = arguments.excludeParentDirs,
                extension = arguments.extension,
                pattern = arguments.pattern,
                excludedPattern = arguments.excludedPattern,
                testMethod = arguments.testMethod,
                testClassName = null,
                targetBackend = arguments.targetBackend,
                excludeDirs = arguments.excludeDirs,
                excludeDirsRecursively = arguments.excludeDirsRecursively,
                skipTestAllFilesCheck = arguments.skipTestAllFilesCheck,
            )
        }
    }
}