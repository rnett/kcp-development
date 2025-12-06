package build

plugins {
    kotlin("jvm")
}

// JVM-only Kotlin configuration
kotlin {
    // Use Java 17 toolchain
    jvmToolchain(17)

    // Common compiler flags
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    add("testImplementation", kotlin("test"))
}

// Unified test task configuration
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
}
