package build

import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("com.vanniktech.maven.publish.base")
    id("build.dokka")
}

extensionIfPresent<MavenPublishBaseExtension> {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(group.toString(), project.name, project.version.toString())

    pom {
        name = "KCP-Development - ${project.name}"
        description = provider { project.description }
        inceptionYear = "2025"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "rnett"
                name = "Ryan Nett"
                url = "https://github.com/rnett/"
            }
        }
        scm {
            url = "https://github.com/rnett/kcp-development/"
            connection = "scm:git:git://github.com/rnett/kcp-development.git"
            developerConnection = "scm:git:ssh://git@github.com/rnett/kcp-development.git"
        }
    }
}

plugins.withId("org.gradle.java-test-fixtures") {
    val component = components["java"] as AdhocComponentWithVariants
    component.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
    component.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }
}

afterEvaluate {
    val hasGradlePlugin = project.plugins.hasPlugin("java-gradle-plugin")
    val hasKotlinJvm = project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")

    if (hasKotlinJvm) {
        extensionIfPresent<JavaPluginExtension> {
            withSourcesJar()
        }

        val javadocTask = tasks.register<Jar>("dokkaJavadocJar") {
            val task = tasks.named("dokkaGeneratePublicationHtml")
            dependsOn(task)
            from(task)
            archiveClassifier.set("javadoc")
        }

        mavenPublicationsWithoutPluginMarker {
            artifact(javadocTask)
        }
    }

    if (hasKotlinJvm && !hasGradlePlugin) {
        gradlePublishing.publications.register<MavenPublication>("maven") {
            val componentName = "java"

            from(project.components.getByName(componentName))
        }
    }
}