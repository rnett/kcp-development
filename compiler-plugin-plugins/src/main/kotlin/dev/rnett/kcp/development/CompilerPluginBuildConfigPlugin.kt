package dev.rnett.kcp.development

import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

public class CompilerPluginBuildConfigPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(CompilerPluginBasePlugin::class.java)
        pluginManager.apply("com.github.gmazzo.buildconfig")
        pluginManager.withPlugin("com.github.gmazzo.buildconfig") {
            the<BuildConfigExtension>().apply {
                useKotlinOutput {
                    internalVisibility = true
                }

                packageName.set(provider { target.group.toString() })
                // must match the one in gradle-plugin.gradle.kts
                buildConfigField("String", "KOTLIN_PLUGIN_ID", provider { "\"${target.group}.${target.name}\"" })
            }
        }
    }
}