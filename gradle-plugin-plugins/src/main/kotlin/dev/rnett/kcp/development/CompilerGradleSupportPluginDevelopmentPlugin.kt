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
        pluginManager.withPlugin("com.github.gmazzo.buildconfig") {
            val buildConfig = the<BuildConfigExtension>()


            val extension: CompilerGradleSupportPluginDevelopmentExtension = extensions.create<CompilerGradleSupportPluginDevelopmentExtension>(CompilerGradleSupportPluginDevelopmentExtension.NAME).apply {
                compilerPluginProjectPath.convention(":compiler-plugin")
                compilerPluginProjectPath.finalizeValueOnRead()

                additionalLibraryProjectPaths.convention(emptyMap())
                additionalLibraryProjectPaths.finalizeValueOnRead()
            }

            extensions.extraProperties["kotlin.stdlib.default.dependency"] = "false"

            dependencies {
                "compileOnly"(kotlin("stdlib"))
                "compileOnly"(kotlin("gradle-plugin-api"))
            }

            buildConfig.apply {
                useKotlinOutput {
                    internalVisibility = true
                }
                packageName.set(provider { group.toString() })

                // must match the one in compiler-plugin-buildconfig.gradle.kts
                buildConfigField("String", "KOTLIN_PLUGIN_ID", provider { "\"${group}.${name}\"" })

                afterEvaluate {
                    val pluginProject = project(extension.compilerPluginProjectPath.get())
                    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", provider { "\"${pluginProject.group}\"" })
                    buildConfigField("String", "KOTLIN_PLUGIN_NAME", provider { "\"${pluginProject.name}\"" })
                    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", provider { "\"${pluginProject.version}\"" })



                    extension.additionalLibraryProjectPaths.get().forEach { (name, project) ->
                        val libraryProject = project(project)

                        val libraryCoordinates = buildString {
                            append(libraryProject.group)
                            append(":")
                            append(libraryProject.name)
                            val versionString = libraryProject.version.toString().ifEmpty { null }?.takeUnless { it == "undefined" }
                            if (versionString != null) {
                                append(":")
                                append(versionString)
                            }
                        }

                        buildConfigField(
                            type = "String",
                            name = "${name.uppercase().replace("-", "_")}_LIBRARY_COORDINATES",
                            expression = "\"${libraryCoordinates}\""
                        )
                    }
                }
            }
        }
    }
}