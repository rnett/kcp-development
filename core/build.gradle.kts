plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Kotlin compiler support and utilities"

dependencies {
    api(kotlin("compiler"))
    api(kotlin("stdlib"))
}
