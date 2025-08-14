package dev.rnett.kcp.development

import org.gradle.api.Plugin
import org.gradle.api.Project

class CompilerPluginPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply {
            apply(CompilerPluginBasePlugin::class.java)
            apply(CompilerPluginBuildConfigPlugin::class.java)
            apply(CompilerPluginTestingPlugin::class.java)
            apply(CompilerPluginShadowPlugin::class.java)
        }
    }
}