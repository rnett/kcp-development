package dev.rnett.kcp.development.testing.generation

import org.jetbrains.kotlin.generators.TestGroup
import org.jetbrains.kotlin.generators.getDefaultSuiteTestClassName
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo


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
inline fun <reified T> TestGroupGeneration.testClassForDirectory(
    relativeRootPath: String,
    suiteTestClassName: String = internal.testClassNamePrefixForDirectory(relativeRootPath) + getDefaultSuiteTestClassName(T::class.java.simpleName),
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
    val cls = T::class
    testClass(
        cls,
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