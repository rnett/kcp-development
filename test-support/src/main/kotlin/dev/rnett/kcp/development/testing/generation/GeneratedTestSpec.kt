package dev.rnett.kcp.development.testing.generation

import org.jetbrains.kotlin.generators.TestGroup
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import kotlin.reflect.KClass

data class GeneratedTestConfiguration(
    val configurationAppliers: List<TestConfigurationBuilder.() -> Unit>
)

data class GeneratedTestSpec(
    val generatedFrom: TestGenerationBuilderImplementation,
    val suiteName: String,
    val testDataPath: String,
    val baseClass: KClass<*>,
    val annotations: List<AnnotationModel>,
    val methods: List<MethodModel>,
    val arguments: TestArguments
) {
    fun applyTo(rootPackage: String?, testGroup: TestGroup) {
        testGroup.testClass(
            baseClass.java,
            suiteTestClassName = rootPackage?.let { it.trimEnd('.') + ".$suiteName" } ?: suiteName,
            useJunit4 = false,
            annotations = annotations
        ) {
            methods.forEach { this.method(it) }
            model(
                relativeRootPath = testDataPath,
                recursive = arguments.recursive,
                excludeParentDirs = arguments.excludeParentDirs,
                extension = arguments.extension,
                pattern = arguments.pattern,
                excludedPattern = arguments.excludedPattern,
                testMethod = arguments.testMethod,
                singleClass = arguments.singleClass,
                testClassName = null,
                targetBackend = arguments.targetBackend,
                excludeDirs = arguments.excludeDirs,
                excludeDirsRecursively = arguments.excludeDirsRecursively,
                filenameStartsLowerCase = arguments.filenameStartsLowerCase,
                skipIgnored = arguments.skipIgnored,
                deep = arguments.deep,
                skipSpecificFile = arguments.skipSpecificFile,
                skipTestAllFilesCheck = arguments.skipTestAllFilesCheck,
                generateEmptyTestClasses = arguments.generateEmptyTestClasses,
                nativeTestInNonNativeTestInfra = arguments.nativeTestInNonNativeTestInfra,
            )
        }
    }
}