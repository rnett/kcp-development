plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
    id("java-gradle-plugin")
    `kotlin-dsl`
    alias(libs.plugins.buildconfig)
}

description = "A Gradle plugin to set up development of Kotlin compiler plugins"

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("gradle-plugin-api"))

    api(libs.shadow.plugin)
    api(libs.buildconfig.plugin)
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }

    packageName("dev.rnett.kcp.development")
    buildConfigField("String", "OWN_GROUP", "\"${group}\"")
    buildConfigField("String", "OWN_VERSION", "\"${version}\"")
}

gradlePlugin {
    plugins {
        create("KcpDevelopmentCompilerPluginBasePlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.base"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginBasePlugin"
            displayName = "KCP-Development - base"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
        create("KcpDevelopmentCompilerPluginShadowPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.shadow"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginShadowPlugin"
            displayName = "KCP-Development - shadow"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
        create("KcpDevelopmentCompilerPluginBuildConfigPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.buildconfig"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginBuildConfigPlugin"
            displayName = "KCP-Development - buildconfig"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
        create("KcpDevelopmentCompilerPluginTestingPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.testing"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginTestingPlugin"
            displayName = "KCP-Development - testing"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
        create("KcpDevelopmentCompilerPluginServicesPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.services"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginAutoServicesPlugin"
            displayName = "KCP-Development - services"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
        create("KcpDevelopmentCompilerPluginPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginPlugin"
            displayName = "KCP-Development"
            description = "Sets up Gradle configuration for Kotlin compiler plugin development."
        }
    }
}