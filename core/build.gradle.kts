plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Kotlin compiler support and utilities"

dependencies {
    compileOnly(kotlin("compiler"))
    compileOnly(kotlin("stdlib"))
}
