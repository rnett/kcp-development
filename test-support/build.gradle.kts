plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Kotlin compiler plugin test support"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    api(kotlin("compiler-internal-test-framework"))
    api(kotlin("compiler"))
    api(kotlin("stdlib"))
    api(project(":core"))
    api(kotlin("test-junit5"))
    implementation(kotlin("reflect"))
}