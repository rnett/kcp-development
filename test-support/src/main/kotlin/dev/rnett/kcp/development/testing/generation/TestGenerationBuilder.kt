package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.registrar.BaseSpecCompilerPluginRegistrar
import dev.rnett.kcp.development.testing.directives.UtilityDirectives.BOX_OPT_IN
import dev.rnett.kcp.development.testing.directives.UtilityDirectives.IMPORTS
import dev.rnett.kcp.development.testing.tests.TestType
import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.builders.RegisteredDirectivesBuilder
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.OPT_IN
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KClass

@DslMarker
public annotation class TestGenerationDslMarker

@TestGenerationDslMarker
public interface TestGenerationBuilder {
    /**
     * The relative path of this group's directory, from the test data root.
     */
    public val pathFromRoot: Path?

    /**
     * The absolute test data directory for this group.
     */
    public val path: Path

    /**
     * Apply compiler test framework configuration.
     * Applies to this group's tests, and any descendents.
     */
    @TestGenerationDslMarker
    public fun configure(block: TestConfigurationBuilder.() -> Unit)

    /**
     * Apply test class generation configuration.
     * Applies to this group's tests, and any descendents.
     */
    @TestGenerationDslMarker
    public fun configureGeneration(block: TestGenerationConfigBuilder.() -> Unit)

    /**
     * Create a sub-group.
     *
     * @param path the relative path of the group's directory, from this group's directory.  If null, does not alter the path, but still only applies configuration to descendents.
     * @param inferPackageNames whether to infer package names from the relative path of this group
     * @param inferType whether to infer the type of this group from the relative path of this group. See [TestType] for options.
     */
    public fun group(path: String? = null, inferPackageNames: Boolean = true, inferType: Boolean = true, block: TestGenerationBuilder.() -> Unit)

    /**
     * Generate tests for files in `./$path` (from this group's directory) and any sub-directories.
     * Should only be used in leaf groups.
     *
     * @param path the path to generate tests from
     * @param testClassName the name of the test class to generate, or null to use the default
     * @param customBaseClass the base class of the test class to generate, or null to use the base class for the test levels configured.
     * @param arguments the arguments to pass to the test generator
     */
    public fun tests(
        path: String,
        testClassName: String? = null,
        customBaseClass: KClass<*>? = null,
        arguments: TestArguments = TestArguments()
    )

    /**
     * Generate tests for all files in this group's directory and any sub-directories.
     * Should only be used in leaf groups.
     *
     * @param testClassName the name of the test class to generate, or null to use the default
     * @param customBaseClass the base class of the test class to generate, or null to use the base class for the test levels configured.
     * @param arguments the arguments to pass to the test generator
     */
    public fun tests(
        testClassName: String? = null,
        customBaseClass: KClass<*>? = null,
        arguments: TestArguments = TestArguments()
    )
}

/**
 * Test arguments to pass to the underlying Kotlin compiler plugin test framework test generator.
 * See [org.jetbrains.kotlin.generators.TestGroup.TestClass.model] for details.
 */
public data class TestArguments(
    val recursive: Boolean = true,
    val excludeParentDirs: Boolean = false,
    val extension: String? = "kt", // null string means dir (name without dot)
    val pattern: String = if (extension == null) """^([^\.]+)$""" else "^(.+)\\.$extension\$",
    val excludedPattern: String? = null,
    val testMethod: String = "doTest",
    val singleClass: Boolean = false, // if true then tests from subdirectories will be flattened to single class
//    val testClassName: String? = null, // specific name for generated test class
    // which backend will be used in test. Specifying value may affect some test with
    // directives TARGET_BACKEND/DONT_TARGET_EXACT_BACKEND won't be generated
    val targetBackend: TargetBackend? = null,
    val excludeDirs: List<String> = listOf(),
    val excludeDirsRecursively: List<String> = listOf(),
    val filenameStartsLowerCase: Boolean? = null, // assert that file is properly named
    val skipIgnored: Boolean = false, // pretty meaningless flag, affects only few test names in one test runner
    val deep: Int? = null, // specifies how deep recursive search will follow directory with testdata
    val skipSpecificFile: (File) -> Boolean = { false },
    val skipTestAllFilesCheck: Boolean = false,
    val generateEmptyTestClasses: Boolean = true, // All test classes will be generated, even if empty
    val nativeTestInNonNativeTestInfra: Boolean = false,
)

/**
 * Create a group with the given [levels].
 *
 * @see TestGenerationBuilder.group
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.group(path: String? = null, vararg levels: TestLevel, block: TestGenerationBuilder.() -> Unit): Unit =
    group(path) {
        levels.forEach { addLevel(it) }
        block()
    }

/**
 * Create a group with the given test [type].
 *
 * @see TestGenerationBuilder.group
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.group(path: String? = null, type: TestType, block: TestGenerationBuilder.() -> Unit): Unit = group(path, inferPackageNames = true, inferType = false) {
    +type
    block()
}

/**
 * Add imports to this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.import(vararg imports: String) {
    directives {
        IMPORTS.with(imports.toList())
    }
}

/**
 * Add opt-in annotations to this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.optIn(vararg optIns: String) {
    directives {
        OPT_IN.with(optIns.toList())
    }
}

/**
 * Add opt-in annotations to the `box()` functions in this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.optInBox(vararg optIns: String) {
    directives {
        BOX_OPT_IN.with(optIns.toList())
        IMPORTS.with(optIns.toList())
    }
}

/**
 * Add imports to this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.import(vararg imports: KClass<*>) {
    directives {
        IMPORTS.with(imports.map { it.qualifiedName!! })
    }
}

/**
 * Add opt-in annotations to this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.optIn(vararg optIns: KClass<*>) {
    directives {
        OPT_IN.with(optIns.map { it.qualifiedName!! })
    }
}

/**
 * Add opt-in annotations to the `box()` functions in this group's tests, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.optInBox(vararg optIns: KClass<*>) {
    directives {
        val qualifiedNames = optIns.map { it.qualifiedName!! }
        BOX_OPT_IN.with(qualifiedNames)
        IMPORTS.with(qualifiedNames)
    }
}

/**
 * Add a test level to this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.addLevel(level: TestLevel): Unit = configureGeneration { addLevel(level) }

/**
 * Remove a test level from this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.removeLevel(level: TestLevel): Unit = configureGeneration { removeLevel(level) }

//TODO test name prefix

/**
 * Add a to the package name of test classes generated by this group and any descendents..
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.testsPackage(vararg packageNames: String): Unit = configureGeneration { testsPackage(*packageNames) }

/**
 * Add a test level to this group, and any descendents.
 */
@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestLevel.unaryPlus() {
    generator.addLevel(this)
}

/**
 * Remove a test level from this group, and any descendents.
 */
@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestLevel.unaryMinus() {
    generator.removeLevel(this)
}

/**
 * Add test levels to this group, and any descendents.
 */
@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestSpec.unaryPlus() {
    this.levels.forEach { generator.addLevel(it) }
}

/**
 * Remove test levels from this group, and any descendents.
 */
@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestSpec.unaryMinus() {
    this.levels.forEach { generator.removeLevel(it) }
}

/**
 * Add a test type to this group, and any descendents.
 */
@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestType.unaryPlus() {
    +this.spec
}

/**
 * Register directive containers for this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.registerDirectives(vararg containers: DirectivesContainer): Unit = configure {
    useDirectives(*containers)
}

/**
 * Register directive containers and configure directives for this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.directives(vararg containers: DirectivesContainer, block: RegisteredDirectivesBuilder.() -> Unit) {
    configure {
        useDirectives(*containers)
        defaultDirectives {
            block()
        }
    }
}

/**
 * Add test class annotations to this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.annotation(annotation: AnnotationModel): Unit = configureGeneration { annotation(annotation) }

/**
 * Add test class methods to this group, and any descendents.
 */
@TestGenerationDslMarker
public fun TestGenerationBuilder.method(method: MethodModel): Unit = configureGeneration { method(method) }

/**
 * Use the specified [spec] for the corresponding [pluginRegistrar] for this group, and any descendents (unless overridden).
 */
@TestGenerationDslMarker
@OptIn(ExperimentalCompilerApi::class)
public fun <T> TestGenerationBuilder.withTestSpec(pluginRegistrar: KClass<out BaseSpecCompilerPluginRegistrar<T>>, spec: T) {
    withCompilerConfiguration {
        add(BaseSpecCompilerPluginRegistrar.testSpecKey, BaseSpecCompilerPluginRegistrar.TestSpec(pluginRegistrar, spec))
    }
}

/**
 * Apply compiler configuration to this group, and any descendents.
 */
@TestGenerationDslMarker
@OptIn(ExperimentalCompilerApi::class)
public fun TestGenerationBuilder.withCompilerConfiguration(configurator: CompilerConfiguration.(TestModule) -> Unit) {
    configure {
        useConfigurators({
            object : EnvironmentConfigurator(it) {
                override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
                    configuration.configurator(module)
                }
            }
        })
    }
}