package dev.rnett.kcp.development

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named

public class CompilerPluginShadowPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(CompilerPluginBasePlugin::class.java)
        pluginManager.apply("com.gradleup.shadow")

        pluginManager.withPlugin("com.gradleup.shadow") {
            tasks.named<ShadowJar>("shadowJar") {
                archiveClassifier.set("")
                dependencies {
                    exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
                }
            }
        }
    }
}