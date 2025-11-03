package dev.rnett.kcp.development

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

public class CompilerPluginShadowPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(CompilerPluginBasePlugin::class.java)
        pluginManager.apply("com.gradleup.shadow")

        // poison the non-shadow variant
        configurations.named("runtimeElements") {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named("disabled-usage"))
            }
        }

        pluginManager.withPlugin("com.gradleup.shadow") {
            tasks.named<ShadowJar>("shadowJar") {
                archiveClassifier.set("")
                dependencies {
                    exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
                    exclude(dependency("org.jetbrains.kotlin:kotlin-compiler"))
                }
            }
            tasks.named<Jar>("jar") {
                archiveClassifier.set("ignored")
            }
        }
    }
}