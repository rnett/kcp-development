plugins {
    alias(libs.plugins.kotlin.jvm)
}

description = "Kotlin compiler support and utilities"

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(kotlin("compiler"))
    api(kotlin("stdlib"))
}
