plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    `kotlin-dsl`
    alias(libs.plugins.buildconfig)
}

description = "A Gradle plugin to set up development of Kotlin compiler plugin support Gradle plugin"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("gradle-plugin-api"))

    api(libs.buildconfig.plugin)
}
