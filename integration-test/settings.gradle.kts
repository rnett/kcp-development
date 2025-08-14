plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        register("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

gradle.beforeProject {
    group = "dev.rnett.kcp-development.test"
    version = "0.0.1-SNAPSHOT"
}

includeBuild("..")

include(
    ":compiler-plugin",
    ":gradle-plugin",
    ":annotations"
)

rootProject.name = "kcp-development-test"