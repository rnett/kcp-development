package dev.rnett.kcp.development.testing.tests.levels

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest

public open class AbstractLeveledFirTest : AbstractFirLightTreeDiagnosticsTest() {
    override fun configure(builder: TestConfigurationBuilder) {
        val levels = TestSpec.forTestClass(this)
        levels.preConfigure(this, builder)
        super.configure(builder)
        levels.configure(this, builder)
    }
}