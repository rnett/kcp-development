package dev.rnett.kcp.development

import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.the

public class CompilerGradleSupportPluginDevelopmentPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.github.gmazzo.buildconfig")


        extensions.extraProperties["kotlin.stdlib.default.dependency"] = "false"

        target.dependencies {
            "compileOnly"(kotlin("stdlib"))
            "compileOnly"(kotlin("gradle-plugin-api"))
            "compileOnly"(gradleApi())
        }


        val extension: CompilerGradleSupportPluginDevelopmentExtension = extensions.create<CompilerGradleSupportPluginDevelopmentExtension>(CompilerGradleSupportPluginDevelopmentExtension.NAME).apply {
            compilerPluginProjectPath.convention(":compiler-plugin")
            compilerPluginProjectPath.finalizeValueOnRead()
        }

        pluginManager.withPlugin("com.github.gmazzo.buildconfig") {
            val buildConfig = the<BuildConfigExtension>()
            buildConfig.apply {
                useKotlinOutput {
                    internalVisibility = true
                }
                packageName.set(provider { target.group.toString() })

                afterEvaluate {
                    val pluginProject = project(extension.compilerPluginProjectPath.get())

                    // must match the one in compiler-plugin-buildconfig.gradle.kts
                    buildConfigField("String", "KOTLIN_PLUGIN_ID", provider { "\"${pluginProject.group}.${pluginProject.name}\"" })

                    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", provider { "\"${pluginProject.group}\"" })
                    buildConfigField("String", "KOTLIN_PLUGIN_NAME", provider { "\"${pluginProject.name}\"" })
                    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", provider { "\"${pluginProject.version}\"" })
                }
            }
        }
    }
}