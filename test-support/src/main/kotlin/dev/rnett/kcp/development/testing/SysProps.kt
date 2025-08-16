package auto.test.box.dev.rnett.kcp.development.testing

object SysProps {
    val defaultPackage by lazy { System.getProperty("default.package")!! }
    val testGenRoot by lazy { System.getProperty("kcp.dev.test-gen")!! }
    val testDataRoot by lazy { System.getProperty("kcp.dev.test-data")!! }
    val pluginRegistrar by lazy { System.getProperty("kcp.dev.plugin-registrar") }
}