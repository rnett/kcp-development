plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

val versionFile = providers.fileContents(layout.rootDirectory.file("version.txt"))

gradle.beforeProject {
    group = "dev.rnett.kcp-development"
    version = versionFile.asText.get().trim()
}

include(
    ":compiler-plugin-plugins",
    ":gradle-plugin-plugins",
    ":test-support",
    ":core"
)

rootProject.name = "kcp-development"