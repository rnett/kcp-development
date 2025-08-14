package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.configuration.Configurator
import dev.rnett.kcp.development.testing.configuration.TestConfigurator
import dev.rnett.kcp.development.testing.tests.AppliesDynamicConfigurators
import dev.rnett.kcp.development.testing.tests.TestType
import org.jetbrains.kotlin.generators.TestGroup
import org.jetbrains.kotlin.generators.TestGroup.TestClass
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5
import org.jetbrains.kotlin.generators.getDefaultSuiteTestClassName
import org.jetbrains.kotlin.generators.model.AnnotationArgumentModel
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File
import java.lang.reflect.Modifier
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.name
import kotlin.io.path.relativeTo
import kotlin.io.path.visitFileTree
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

//TODO I want to be able to set default configurators for a scope

//TODO I'd like to be able to have a .directives or similar file in the test data that affects it and subdirs

interface TestGenerator {
    fun TestGroup.generateTests()

    companion object {
        @OptIn(ExperimentalPathApi::class)
        inline fun generateTestSuite(crossinline block: TestGroup.() -> Unit) {
            generateTestGroupSuiteWithJUnit5 {
                Path("src/test-gen").deleteRecursively()
                testGroup(
                    testDataRoot = "src/testData",
                    testsRoot = "src/test-gen",
                ) {
                    block()
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val generator = args.getOrNull(0)
            if (generator != null) {
                val cls = Class.forName(generator)
                if (TestGenerator::class.java.isAssignableFrom(cls)) {
                    val instance = (cls.kotlin.objectInstance ?: cls.kotlin.createInstance()) as TestGenerator
                    generateTestSuite {
                        instance.apply { generateTests() }
                    }
                } else
                    error("Class $generator is not a subtype of dev.rnett.kcp.development.testing.generation.TestGenerator")
            } else {
                generateTestSuite {
                    autoGenerateTests()
                }
            }
        }

        fun TestGroup.autoGenerateTests(skip: (Path) -> Boolean = { false }) {
            Path(testDataRoot).visitFileTree {
                onPreVisitDirectory { it, attr ->
                    if (skip(it))
                        return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE

                    if (attr.isDirectory && it.name in TestType.byDirectoryName) {
                        val type = TestType.byDirectoryName.getValue(it.name)
                        testClassForDirectory(it.relativeTo(Path(testDataRoot)).toString(), type)
                        return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
                    }

                    FileVisitResult.CONTINUE
                }
            }
        }

        fun Array<out KClass<out Configurator>>.toAnnotationList() = map {
            require(!it.java.isAnonymousClass && !it.java.isLocalClass) { "Can't create annotation from a local or anonymous configurator class" }
            require(it.java.modifiers.and(Modifier.PUBLIC) != 0) { "Can't create annotation from a non-public configurator class" }
            AnnotationModel(TestConfigurator::class.java, listOf(AnnotationArgumentModel("configurator", it)))
        }

        inline fun <reified T : AppliesDynamicConfigurators> TestGroup.testClass(
            vararg configurators: KClass<out Configurator>,
            suiteTestClassName: String = getDefaultSuiteTestClassName(T::class.java.simpleName),
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            noinline init: TestClass.() -> Unit
        ) {
            testClass<T>(suiteTestClassName, useJunit4, annotations + configurators.toAnnotationList(), init)
        }

        fun TestGroup.testClass(
            testKClass: Class<out AppliesDynamicConfigurators>,
            vararg configurators: KClass<out Configurator>,
            baseTestClassName: String = testKClass.name,
            suiteTestClassName: String = getDefaultSuiteTestClassName(baseTestClassName.substringAfterLast('.')),
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            init: TestClass.() -> Unit
        ) {
            testClass(
                testKClass,
                baseTestClassName,
                suiteTestClassName,
                useJunit4,
                annotations + configurators.toAnnotationList(),
                init
            )
        }

        fun TestGroup.testClass(
            type: TestType,
            vararg configurators: KClass<out Configurator>,
            suiteTestClassName: String = type.testSuffix + "TestGenerated",
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            init: TestClass.() -> Unit
        ) {
            testClass(
                type.cls.java,
                type.cls.java.name,
                suiteTestClassName,
                useJunit4,
                annotations + configurators.toAnnotationList(),
                init
            )
        }

        /**
         * Creates a test class with a single model and a unique name derived from [relativeRootPath].
         */
        inline fun <reified T> TestGroup.testClassForDirectory(
            relativeRootPath: String,
            suiteTestClassName: String = testClassNamePrefixForDirectory(relativeRootPath) + getDefaultSuiteTestClassName(T::class.java.simpleName),
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            recursive: Boolean = true,
            excludeParentDirs: Boolean = false,
            extension: String? = "kt", // null string means dir (name without dot)
            pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
            excludedPattern: String? = null,
            testMethod: String = "doTest",
            singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
            testClassName: String? = null, // specific name for generated test class
            // which backend will be used in test. Specifying value may affect some test with
            // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
            targetBackend: TargetBackend? = null,
            excludeDirs: List<String> = listOf(),
            excludeDirsRecursively: List<String> = listOf(),
            filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
            skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
            deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
            noinline skipSpecificFile: (File) -> Boolean = { false },
            skipTestAllFilesCheck: Boolean = false,
            generateEmptyTestClasses: Boolean = true, // All test classes will be generated, even if empty
            nativeTestInNonNativeTestInfra: Boolean = false,
        ) {
            val cls = T::class.java
            testClass(
                cls,
                baseTestClassName = cls.name,
                suiteTestClassName = suiteTestClassName,
                useJunit4 = useJunit4,
                annotations = annotations,
            ) {
                model(
                    relativeRootPath = relativeRootPath,
                    recursive = recursive,
                    excludeParentDirs = excludeParentDirs,
                    extension = extension,
                    pattern = pattern,
                    excludedPattern = excludedPattern,
                    testMethod = testMethod,
                    singleClass = singleClass,
                    testClassName = testClassName,
                    targetBackend = targetBackend,
                    excludeDirs = excludeDirs,
                    excludeDirsRecursively = excludeDirsRecursively,
                    filenameStartsLowerCase = filenameStartsLowerCase,
                    skipIgnored = skipIgnored,
                    deep = deep,
                    skipSpecificFile = skipSpecificFile,
                    skipTestAllFilesCheck = skipTestAllFilesCheck,
                    generateEmptyTestClasses = generateEmptyTestClasses,
                    nativeTestInNonNativeTestInfra = nativeTestInNonNativeTestInfra,
                )
            }
        }

        /**
         * Creates a test class with a single model and a unique name derived from [relativeRootPath].
         */
        inline fun <reified T : AppliesDynamicConfigurators> TestGroup.testClassForDirectory(
            relativeRootPath: String,
            vararg configurators: KClass<out Configurator>,
            suiteTestClassName: String = testClassNamePrefixForDirectory(relativeRootPath) + getDefaultSuiteTestClassName(T::class.java.simpleName),
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            recursive: Boolean = true,
            excludeParentDirs: Boolean = false,
            extension: String? = "kt", // null string means dir (name without dot)
            pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
            excludedPattern: String? = null,
            testMethod: String = "doTest",
            singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
            testClassName: String? = null, // specific name for generated test class
            // which backend will be used in test. Specifying value may affect some test with
            // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
            targetBackend: TargetBackend? = null,
            excludeDirs: List<String> = listOf(),
            excludeDirsRecursively: List<String> = listOf(),
            filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
            skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
            deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
            noinline skipSpecificFile: (File) -> Boolean = { false },
            skipTestAllFilesCheck: Boolean = false,
            generateEmptyTestClasses: Boolean = true, // All test classes will be generated, even if empty
            nativeTestInNonNativeTestInfra: Boolean = false,
        ) {
            testClassForDirectory<T>(
                relativeRootPath,
                suiteTestClassName,
                useJunit4,
                annotations + configurators.toAnnotationList(),
                recursive,
                excludeParentDirs,
                extension,
                pattern,
                excludedPattern,
                testMethod,
                singleClass,
                testClassName,
                targetBackend,
                excludeDirs,
                excludeDirsRecursively,
                filenameStartsLowerCase,
                skipIgnored,
                deep,
                skipSpecificFile,
                skipTestAllFilesCheck,
                generateEmptyTestClasses,
                nativeTestInNonNativeTestInfra,
            )
        }

        /**
         * Creates a test class with a single model and a unique name derived from [relativeRootPath].
         */
        fun TestGroup.testClassForDirectory(
            testKClass: Class<out AppliesDynamicConfigurators>,
            relativeRootPath: String,
            vararg configurators: KClass<out Configurator>,
            baseTestClassName: String = testKClass.name,
            suiteTestClassName: String = testClassNamePrefixForDirectory(relativeRootPath) + getDefaultSuiteTestClassName(baseTestClassName.substringAfterLast('.')),
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            recursive: Boolean = true,
            excludeParentDirs: Boolean = false,
            extension: String? = "kt", // null string means dir (name without dot)
            pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
            excludedPattern: String? = null,
            testMethod: String = "doTest",
            singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
            testClassName: String? = null, // specific name for generated test class
            // which backend will be used in test. Specifying value may affect some test with
            // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
            targetBackend: TargetBackend? = null,
            excludeDirs: List<String> = listOf(),
            excludeDirsRecursively: List<String> = listOf(),
            filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
            skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
            deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
            skipSpecificFile: (File) -> Boolean = { false },
            skipTestAllFilesCheck: Boolean = false,
            generateEmptyTestClasses: Boolean = true, // All test classes will be generated, even if empty
            nativeTestInNonNativeTestInfra: Boolean = false,
        ) {
            testClass(
                testKClass,
                *configurators,
                baseTestClassName = baseTestClassName,
                suiteTestClassName = suiteTestClassName,
                useJunit4 = useJunit4,
                annotations = annotations,
            ) {
                model(
                    relativeRootPath = relativeRootPath,
                    recursive = recursive,
                    excludeParentDirs = excludeParentDirs,
                    extension = extension,
                    pattern = pattern,
                    excludedPattern = excludedPattern,
                    testMethod = testMethod,
                    singleClass = singleClass,
                    testClassName = testClassName,
                    targetBackend = targetBackend,
                    excludeDirs = excludeDirs,
                    excludeDirsRecursively = excludeDirsRecursively,
                    filenameStartsLowerCase = filenameStartsLowerCase,
                    skipIgnored = skipIgnored,
                    deep = deep,
                    skipSpecificFile = skipSpecificFile,
                    skipTestAllFilesCheck = skipTestAllFilesCheck,
                    generateEmptyTestClasses = generateEmptyTestClasses,
                    nativeTestInNonNativeTestInfra = nativeTestInNonNativeTestInfra,
                )
            }
        }

        @PublishedApi
        internal inline fun TestGroup.testClassNamePrefixForDirectory(relativeRootPath: String, ignoreTrailingSegmentsWhile: (String) -> Boolean = { false }): String {
            val root = Path(testDataRoot)
            val path = root.resolve(relativeRootPath).relativeTo(root)

            val segments = path.map { it.name }.dropLastWhile(ignoreTrailingSegmentsWhile)

            return segments.joinToString("") { it.replaceFirstChar { it.titlecase() } }
        }

        /**
         * Creates a test class with a single model and a unique name derived from [relativeRootPath].
         */
        fun TestGroup.testClassForDirectory(
            relativeRootPath: String,
            type: TestType,
            vararg configurators: KClass<out Configurator>,
            useJunit4: Boolean = false,
            annotations: List<AnnotationModel> = emptyList(),
            extension: String? = "kt", // null string means dir (name without dot)
            pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
            excludedPattern: String? = null,
            testMethod: String = "doTest",
            singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
            testClassName: String? = null, // specific name for generated test class
            // which backend will be used in test. Specifying value may affect some test with
            // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
            targetBackend: TargetBackend? = null,
            excludeDirs: List<String> = listOf(),
            excludeDirsRecursively: List<String> = listOf(),
            filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
            skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
            deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
            skipSpecificFile: (File) -> Boolean = { false },
            skipTestAllFilesCheck: Boolean = false,
            generateEmptyTestClasses: Boolean = true, // All test classes will be generated, even if empty
            nativeTestInNonNativeTestInfra: Boolean = false,
        ) {
            val className = testClassNamePrefixForDirectory(relativeRootPath) { it == type.directoryName } + type.testSuffix + "TestGenerated"

            testClassForDirectory(
                type.cls.java,
                relativeRootPath,
                *configurators,
                baseTestClassName = type.cls.java.name,
                suiteTestClassName = className,
                useJunit4 = useJunit4,
                annotations = annotations,
                recursive = true,
                excludeParentDirs = false,
                extension = extension,
                pattern = pattern,
                excludedPattern = excludedPattern,
                singleClass = singleClass,
                testClassName = testClassName,
                targetBackend = targetBackend,
                excludeDirs = excludeDirs,
                excludeDirsRecursively = excludeDirsRecursively,
                filenameStartsLowerCase = filenameStartsLowerCase,
                skipIgnored = skipIgnored,
                deep = deep,
                skipSpecificFile = skipSpecificFile,
                skipTestAllFilesCheck = skipTestAllFilesCheck,
                generateEmptyTestClasses = generateEmptyTestClasses,
                nativeTestInNonNativeTestInfra = nativeTestInNonNativeTestInfra,
            )
        }
    }
}