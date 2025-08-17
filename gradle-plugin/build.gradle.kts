plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
    id("java-gradle-plugin")
    `kotlin-dsl`
    alias(libs.plugins.buildconfig)
}

description = "A Gradle plugin to set up development of Kotlin compiler plugin support Gradle plugin"

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("gradle-plugin-api"))

    api(libs.buildconfig.plugin)
}

gradlePlugin {
    plugins {
        create("KcpDevelopmentGradlePluginPlugin") {
            id = "dev.rnett.kcp-development.gradle-plugin"
            implementationClass = "dev.rnett.kcp.development.CompilerGradleSupportPluginDevelopmentPlugin"
            displayName = "KCP-Development - gradle"
            description = "Sets up Gradle configuration for Kotlin compiler plugin support Gradle plugin development."
        }
    }
}