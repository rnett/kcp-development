plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.kcp-development.compiler-plugin")
}

compilerPluginDevelopment {
    testGenerator = "dev.rnett.kcp.tests.TestGenerator"
}