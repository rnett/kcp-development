package dev.rnett.kcp.development.testing

internal object SysProps {
    val defaultPackage by lazy { System.getProperty("default.package")!! }
    val testGenRoot by lazy { System.getProperty("kcp.dev.test-gen")!! }
    val testDataRoot by lazy { System.getProperty("kcp.dev.test-data")!! }
    val pluginRegistrar by lazy { System.getProperty("kcp.dev.plugin-registrar") }
}