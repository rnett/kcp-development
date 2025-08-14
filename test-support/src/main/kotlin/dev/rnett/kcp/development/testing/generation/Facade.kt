package dev.rnett.kcp.development.testing.generation

import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File


fun TestClassGeneration.method(method: MethodModel) {
    internal.method(method)
}

fun TestClassGeneration.modelForDirectoryBasedTest(
    relativePath: String,
    testDirectoryName: String,
    extension: String? = "kt",
    excludeParentDirs: Boolean = false,
    recursive: Boolean = true,
    targetBackend: TargetBackend? = null,
    excludedPattern: String? = null,
) {
    internal.modelForDirectoryBasedTest(relativePath, testDirectoryName, extension, excludeParentDirs, recursive, targetBackend, excludedPattern)
}

fun TestClassGeneration.model(
    relativeRootPath: String = "",
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
    internal.model(
        relativeRootPath,
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