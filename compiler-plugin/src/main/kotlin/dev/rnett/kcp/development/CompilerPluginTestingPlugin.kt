package dev.rnett.kcp.development

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import org.gradle.plugins.ide.idea.model.IdeaModel

class CompilerPluginTestingPlugin : Plugin<Project> {
    fun Test.setLibraryProperty(propName: String, jarName: String) {
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
                java.srcDir("src/test-gen")
                resources.srcDir("src/testData")
            }
        }

        the<IdeaModel>().apply {
            module.generatedSourceDirs.add(projectDir.resolve("src/test-gen"))
        }


        val projectGroup = provider { group.toString().replace("-", ".") }

        tasks.named<Test>("test") {
            useJUnitPlatform()
            dependsOn(compilerTestRuntimeClasspath)
            workingDir = projectDir

            val conf = compilerTestRuntimeClasspath.map { it.asPath }

            doFirst {

                systemProperty("default.package", projectGroup.get())

                systemProperty("compilerTestRuntime.classpath", conf.get())

                // Properties required to run the internal test framework.
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
                setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
            }
            systemProperty("idea.ignore.disabled.plugins", "true")
            systemProperty("idea.home.path", projectDir)
        }

        val generateTests by tasks.registering(JavaExec::class) {
            inputs.dir(layout.projectDirectory.dir("src/testData"))
                .withPropertyName("testData")
                .withPathSensitivity(PathSensitivity.RELATIVE)

            outputs.dir(layout.projectDirectory.dir("src/test-gen"))
                .withPropertyName("generatedTests")

            doFirst {
                systemProperty("default.package", projectGroup.get())
            }

            classpath = javaExtension.sourceSets.named("testFixtures").get().runtimeClasspath
            mainClass.set(extension.useTestGenerator.flatMap { if (it) provider { CompilerPluginDevelopmentExtension.TEST_GENERATOR_MAIN } else extension.testGenerator })
            argumentProviders.add { listOfNotNull(extension.testGenerator.orNull).filter { extension.useTestGenerator.get() } }
            workingDir = projectDir
        }

        val clearDumps by tasks.registering(Delete::class) {
            delete(
                fileTree(layout.projectDirectory.dir("src/testData")) {
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