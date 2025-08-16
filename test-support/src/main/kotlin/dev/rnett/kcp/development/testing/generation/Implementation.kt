package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.TestType
import dev.rnett.kcp.development.testing.tests.levels.TestSpec
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo
import kotlin.reflect.KClass

sealed interface TestGenerationPath {
    val relativePath: Path?
    val path: Path
    val root: Path
    val parent: TestGenerationBuilderImplementation? get() = null

    data class Root(override val root: Path) : TestGenerationPath {
        override val relativePath: Path? = null
        override val path: Path = root
    }

    data class Child(val ownRelativePath: Path?, override val parent: TestGenerationBuilderImplementation) : TestGenerationPath {
        override val relativePath: Path? = ownRelativePath
        override val path: Path = ownRelativePath?.let { parent.pathSpec.path.resolve(it) } ?: parent.pathSpec.path
        override val root: Path get() = parent.pathSpec.root
    }
}

class TestGenerationBuilderImplementation(val pathSpec: TestGenerationPath) : TestGenerationBuilder {
    override val path: Path
        get() = pathSpec.path
    override val pathFromRoot: Path?
        get() = path.relativeTo(pathSpec.root).takeIf { it.count() != 0 }

    val parent: TestGenerationBuilderImplementation? get() = pathSpec.parent

    val children = mutableListOf<TestGenerationBuilderImplementation>()
    val childrenByPath = mutableMapOf<Path, TestGenerationBuilderImplementation>()

    val ancestors: List<TestGenerationBuilderImplementation> by lazy { parent?.ancestors.orEmpty() + this }

    val configurations: MutableList<TestConfigurationBuilder.() -> Unit> = mutableListOf()
    val generationConfigs: MutableList<TestGenerationConfigBuilder.() -> Unit> = mutableListOf()

    val generatedTests = mutableMapOf<Path?, TestData>()

    data class TestData(
        val testClassName: String?,
        val testBaseClass: KClass<*>?,
        val arguments: TestArguments,
    )

    override fun configure(block: TestConfigurationBuilder.() -> Unit) {
        configurations += block
    }

    override fun configureGeneration(block: TestGenerationConfigBuilder.() -> Unit) {
        generationConfigs += block
    }

    override fun group(path: String?, inferPackageNames: Boolean, inferType: Boolean, block: TestGenerationBuilder.() -> Unit) {
        val actualPath = path?.let { Path(it) }
        if (actualPath != null) {
            val overlapping = childrenByPath.filter { it.key.startsWith(actualPath) || actualPath.startsWith(it.key) }
            if (overlapping.isNotEmpty())
                error("Already registered test group with path that overlaps $actualPath: $overlapping")
        }
        val child = TestGenerationBuilderImplementation(TestGenerationPath.Child(actualPath, this))

        if (inferPackageNames && actualPath != null) {
            child.configureGeneration {
                testsPackage(*actualPath.map { it.toString() }.toTypedArray())
            }
        }

        if (inferType && actualPath != null) {
            val types = actualPath.mapNotNull { TestType.byDirectoryName[it.name] }
            if (types.isNotEmpty()) {
                child.configureGeneration {
                    types.forEach {
                        it.spec.levels.forEach { level -> addLevel(level) }
                    }
                }
            }
        }

        child.block()

        children.add(child)
        if (actualPath != null)
            childrenByPath[actualPath] = child
    }

    override fun generateTests(
        path: String,
        testClassName: String?,
        customBaseClass: KClass<*>?,
        arguments: TestArguments
    ) {
        if (pathFromRoot == null) error("Can't generate a test with no relative path")
        generatedTests[Path(path)] = TestData(testClassName, customBaseClass, arguments)
    }

    override fun generateThisTest(
        testClassName: String?,
        customBaseClass: KClass<*>?,
        arguments: TestArguments
    ) {
        if (pathFromRoot == null) error("Can't generate a test with no relative path")
        generatedTests[null] = TestData(testClassName, customBaseClass, arguments)
    }

    fun applyConfiguration(builder: TestConfigurationBuilder) {
        ancestors.forEach { it.configurations.forEach { builder.it() } }
        configurations.forEach { builder.it() }
    }
}

fun TestGenerationBuilderImplementation.createGenerationSpecs(rootPackage: String): Map<String, GeneratedTestSpec> = buildMap { createAndPutGenerationSpecs(rootPackage, this) }

private fun TestGenerationBuilderImplementation.createAndPutGenerationSpecs(rootPackage: String, specs: MutableMap<String, GeneratedTestSpec>) {
    val config = TestGenerationConfig()
    ancestors.forEach { it.generationConfigs.forEach { config.it() } }
    val spec = TestSpec(config.levels)

    generatedTests.forEach { (testPath, data) ->
        val path = if (testPath == null) pathFromRoot else pathFromRoot?.resolve(testPath) ?: testPath
        if (path == null)
            error("Can't generate test with a null path")

        val suiteName = buildList {
            addAll(rootPackage.trim('.').split(".").filter { it.isNotBlank() })
            addAll(config.testsPackage)
            add(adjustTestName(data.testClassName ?: guessTestClassNameFromPath(testPath ?: pathSpec.relativePath ?: ancestors.firstNotNullOf { it.pathSpec.relativePath })))
        }.joinToString(".")

        if (suiteName in specs) {
            throw IllegalArgumentException("Duplicate test name: $suiteName")
        }

        if (data.testClassName == null && spec.testClass() == null)
            error("Can't generate test without test levels and no custom base class")

        specs[suiteName] = GeneratedTestSpec(
            this,
            suiteName,
            path.toString(),
            data.testBaseClass ?: spec.testClass()!!,
            config.annotations(),
            config.methods,
            data.arguments,
        )
    }

    children.forEach {
        it.createAndPutGenerationSpecs(rootPackage, specs)
    }
}

private fun adjustTestName(name: String): String {
    return when {
        name.endsWith("TestGenerated") -> name
        name.endsWith("TestsGenerated") -> name
        name.endsWith("Test") -> name + "Generated"
        name.endsWith("Tests") -> name + "Generated"
        else -> name + "TestGenerated"
    }
}

private fun guessTestClassNameFromPath(path: Path): String {
    return path.joinToString("") { it.name.capitalizeAsciiOnly() }
}