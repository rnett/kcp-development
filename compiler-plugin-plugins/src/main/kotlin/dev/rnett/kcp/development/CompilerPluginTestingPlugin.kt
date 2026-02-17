package dev.rnett.kcp.development

import dev.rnett.kcp.development.tasks.CompilerPluginGenerateTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.ide.idea.model.IdeaModel

public class CompilerPluginTestingPlugin : Plugin<Project> {
    private fun Test.setLibraryProperty(propName: String, jarName: String) {
        val path = classpath
            .files
            .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
            ?.absolutePath
            ?: return
        systemProperty(propName, path)
    }

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(CompilerPluginBasePlugin::class.java)
        pluginManager.apply("java-test-fixtures")
        pluginManager.apply("idea")

        val extension = the<CompilerPluginDevelopmentExtension>()

        val compilerTestRuntimeClasspath by configurations.registering {
            isCanBeResolved = true
            isTransitive = true
        }

        dependencies {

            "testFixturesApi"(kotlin("test-junit5"))
            "testFixturesApi"(kotlin("compiler-internal-test-framework"))
            "testFixturesApi"(kotlin("compiler"))

            // Dependencies required to run the internal test framework.
            "testRuntimeOnly"(kotlin("script-runtime"))
            "testRuntimeOnly"(kotlin("annotations-jvm"))
        }


        val javaExtension = the<JavaPluginExtension>()
        javaExtension.sourceSets.apply {
            named("test") {
                java.srcDir(extension.testGenerationRoot)
                resources.srcDir(extension.testDataRoot)
            }
        }

        val javaToolchains = the<JavaToolchainService>()

        the<IdeaModel>().apply {
            afterEvaluate {
                module.generatedSourceDirs.add(extension.testGenerationRoot.get().asFile.relativeTo(projectDir))
            }
        }


        val projectGroup = provider { group.toString().replace("-", ".") }
        val projectDir = this.projectDir

        tasks.named<Test>("test") {
            useJUnitPlatform()
            dependsOn(compilerTestRuntimeClasspath)
            workingDir = projectDir

            val conf = compilerTestRuntimeClasspath.map { it.asPath }

            doFirst {
                systemProperty("compilerTestRuntime.classpath", conf.get())

                // Properties required to run the internal test framework.
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")


                systemProperty("default.package", projectGroup.get())
                systemProperty("kcp.dev.test-gen", extension.testGenerationRoot.get().asFile.relativeTo(projectDir).path)
                systemProperty("kcp.dev.test-data", extension.testDataRoot.get().asFile.relativeTo(projectDir).path)

                extension.compilerPluginRegistrar.orNull?.let {
                    systemProperty("kcp.dev.plugin-registrar", it)
                }

                val parallelTests = extension.parallelTests.get()
                if (parallelTests != 0) {
                    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
                    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
                    if (parallelTests > 0)
                        systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", parallelTests.toString())
                }
            }
            systemProperty("idea.ignore.disabled.plugins", "true")
            systemProperty("idea.home.path", projectDir)
        }

        val generateTests by tasks.registering(CompilerPluginGenerateTestsTask::class) {
            testDataDirectory.set(extension.testDataRoot)
            generatedTestsDirectory.set(extension.testGenerationRoot)

            testGenerator.set(extension.testGenerator)
            useTestGenerator.set(extension.useTestGenerator)

            defaultPackage.set(projectGroup)
            classpath.from(javaExtension.sourceSets.named("testFixtures").get().runtimeClasspath)
            workingDirectory.set(projectDir)

            launcher.convention(javaToolchains.launcherFor { })

            compilerPluginRegistrar.set(extension.compilerPluginRegistrar)
        }

        val clearDumps by tasks.registering(Delete::class) {
            delete(
                fileTree(extension.testDataRoot) {
                    include("**/*.fir.txt")
                    include("**/*.fir.*.txt")
                }
            )
        }

        tasks.named("test") {
            mustRunAfter(clearDumps)
        }

        tasks.named("compileTestKotlin") {
            dependsOn(generateTests)
        }

    }
}