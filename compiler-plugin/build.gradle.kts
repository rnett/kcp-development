plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    `kotlin-dsl`
    alias(libs.plugins.buildconfig)
}

description = "A Gradle plugin to set up development of Kotlin compiler plugins"

kotlin {
    jvmToolchain(17)
}

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
        }
        create("KcpDevelopmentCompilerPluginShadowPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.shadow"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginShadowPlugin"
        }
        create("KcpDevelopmentCompilerPluginBuildConfigPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.buildconfig"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginBuildConfigPlugin"
        }
        create("KcpDevelopmentCompilerPluginTestingPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.testing"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginTestingPlugin"
        }
        create("KcpDevelopmentCompilerPluginServicesPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin.services"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginAutoServicesPlugin"
        }
        create("KcpDevelopmentCompilerPluginPlugin") {
            id = "dev.rnett.kcp-development.compiler-plugin"
            implementationClass = "dev.rnett.kcp.development.CompilerPluginPlugin"
        }
    }
}