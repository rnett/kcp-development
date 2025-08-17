package build

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

internal object Shared {
    fun toolchain(spec: JavaToolchainSpec) = with(spec) {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    fun configureTestTask(task: org.gradle.api.tasks.testing.Test) = with(task) {
        useJUnitPlatform()
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    }
}

inline fun <reified T : Any> KotlinBaseExtension.extensionIfPresent(block: T.() -> Unit) {
    (this as ExtensionAware)
    this.extensions.findByType(T::class.java)?.apply(block)
}

inline fun <reified T : Any> Project.extensionIfPresent(block: T.() -> Unit) {
    this.extensions.findByType(T::class.java)?.apply(block)
}

internal inline val Project.gradlePublishing: PublishingExtension
    get() = extensions.getByType(PublishingExtension::class.java)

internal fun Project.mavenPublications(action: Action<MavenPublication>) {
    gradlePublishing.publications.withType(MavenPublication::class.java).configureEach(action)
}

internal fun Project.mavenPublicationsWithoutPluginMarker(action: Action<MavenPublication>) {
    mavenPublications {
        if (!this.name.endsWith("PluginMarkerMaven")) {
            action.execute(this)
        }
    }
}