package build

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(Shared::toolchain)

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}