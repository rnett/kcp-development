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
    public val pathFromRoot: Path?
    public val path: Path

    public fun configure(block: TestConfigurationBuilder.() -> Unit)

    public fun configureGeneration(block: TestGenerationConfigBuilder.() -> Unit)

    public fun group(path: String? = null, inferPackageNames: Boolean = true, inferType: Boolean = true, block: TestGenerationBuilder.() -> Unit)

    public fun tests(
        path: String,
        testClassName: String? = null,
        customBaseClass: KClass<*>? = null,
        arguments: TestArguments = TestArguments()
    )

    public fun tests(
        testClassName: String? = null,
        customBaseClass: KClass<*>? = null,
        arguments: TestArguments = TestArguments()
    )
}

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

@TestGenerationDslMarker
public fun TestGenerationBuilder.group(path: String? = null, vararg levels: TestLevel, block: TestGenerationBuilder.() -> Unit): Unit =
    group(path) {
        levels.forEach { addLevel(it) }
        block()
    }

@TestGenerationDslMarker
public fun TestGenerationBuilder.group(path: String? = null, type: TestType, block: TestGenerationBuilder.() -> Unit): Unit = group(path, inferPackageNames = true, inferType = false) {
    +type
    block()
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.import(vararg imports: String) {
    directives {
        IMPORTS.with(imports.toList())
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.optIn(vararg optIns: String) {
    directives {
        OPT_IN.with(optIns.toList())
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.optInBox(vararg optIns: String) {
    directives {
        BOX_OPT_IN.with(optIns.toList())
        IMPORTS.with(optIns.toList())
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.import(vararg imports: KClass<*>) {
    directives {
        IMPORTS.with(imports.map { it.qualifiedName!! })
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.optIn(vararg optIns: KClass<*>) {
    directives {
        OPT_IN.with(optIns.map { it.qualifiedName!! })
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.optInBox(vararg optIns: KClass<*>) {
    directives {
        val qualifiedNames = optIns.map { it.qualifiedName!! }
        BOX_OPT_IN.with(qualifiedNames)
        IMPORTS.with(qualifiedNames)
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.addLevel(level: TestLevel): Unit = configureGeneration { addLevel(level) }

@TestGenerationDslMarker
public fun TestGenerationBuilder.removeLevel(level: TestLevel): Unit = configureGeneration { removeLevel(level) }

@TestGenerationDslMarker
public fun TestGenerationBuilder.testsPackage(vararg packageNames: String): Unit = configureGeneration { testsPackage(*packageNames) }

@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestLevel.unaryPlus() {
    generator.addLevel(this)
}

@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestLevel.unaryMinus() {
    generator.removeLevel(this)
}

@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestSpec.unaryPlus() {
    this.levels.forEach { generator.addLevel(it) }
}

@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestSpec.unaryMinus() {
    this.levels.forEach { generator.removeLevel(it) }
}

@TestGenerationDslMarker
context(generator: TestGenerationBuilder)
public operator fun TestType.unaryPlus() {
    +this.spec
}


@TestGenerationDslMarker
public fun TestGenerationBuilder.registerDirectives(vararg containers: DirectivesContainer): Unit = configure {
    useDirectives(*containers)
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.directives(vararg containers: DirectivesContainer, block: RegisteredDirectivesBuilder.() -> Unit) {
    configure {
        useDirectives(*containers)
        defaultDirectives {
            block()
        }
    }
}

@TestGenerationDslMarker
public fun TestGenerationBuilder.annotation(annotation: AnnotationModel): Unit = configureGeneration { annotation(annotation) }

@TestGenerationDslMarker
public fun TestGenerationBuilder.method(method: MethodModel): Unit = configureGeneration { method(method) }


@TestGenerationDslMarker
@OptIn(ExperimentalCompilerApi::class)
public fun <T> TestGenerationBuilder.withTestSpec(configurator: KClass<out BaseSpecCompilerPluginRegistrar<T>>, spec: T) {
    withCompilerConfiguration {
        add(BaseSpecCompilerPluginRegistrar.testSpecKey, BaseSpecCompilerPluginRegistrar.TestSpec(configurator, spec))
    }
}

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