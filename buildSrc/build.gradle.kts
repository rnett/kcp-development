plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.gradle.plugin)
    api(libs.dokka.plugin)
    api(libs.publishing.plugin)
}
