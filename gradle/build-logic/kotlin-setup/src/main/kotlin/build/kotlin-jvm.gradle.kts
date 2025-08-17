package build

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(Shared::toolchain)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}