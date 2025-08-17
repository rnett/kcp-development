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
