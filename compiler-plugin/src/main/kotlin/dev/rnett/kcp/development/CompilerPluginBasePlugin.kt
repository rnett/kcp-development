package dev.rnett.kcp.development

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

public class CompilerPluginBasePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("java-base")

        if (!pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) {
            throw kotlin.IllegalStateException("KCP development plugin requires org.jetbrains.kotlin.jvm to be applied")
        }

        val extension = extensions.create<CompilerPluginDevelopmentExtension>(CompilerPluginDevelopmentExtension.Companion.NAME).apply {
            testGenerator.finalizeValueOnRead()

            useTestGenerator.convention(true)
            useTestGenerator.finalizeValueOnRead()

            addTestSupportDependency.convention(true)
            addTestSupportDependency.finalizeValueOnRead()

            addCoreDependency.convention(true)
            addCoreDependency.finalizeValueOnRead()

            testGenerationRoot.convention(layout.projectDirectory.dir("src/test-gen"))
            testGenerationRoot.finalizeValueOnRead()

            testDataRoot.convention(layout.projectDirectory.dir("src/testData"))
            testDataRoot.finalizeValueOnRead()
        }

        extensions.extraProperties["kotlin.stdlib.default.dependency"] = "false"

        the<KotlinJvmExtension>().apply {
            compilerOptions {
                freeCompilerArgs.add("-Xcontext-parameters")
            }
        }

        dependencies {
            "compileOnly"(kotlin("stdlib"))
            "compileOnly"(kotlin("compiler"))
        }

        afterEvaluate {
            if (extension.addTestSupportDependency.get()) {
                if (pluginManager.hasPlugin("java-test-fixtures")) {
                    dependencies {
                        "testFixturesApi"("${BuildConfig.OWN_GROUP}:test-support:${BuildConfig.OWN_VERSION}")
                    }
                }
            }
            if (extension.addCoreDependency.get()) {
                dependencies {
                    "implementation"("${BuildConfig.OWN_GROUP}:core:${BuildConfig.OWN_VERSION}")
                }
            }
        }
    }
}