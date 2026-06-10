package dev.rnett.kcp.development.testing.tests.levels

import org.jetbrains.kotlin.test.builders.NonGroupingPhaseTestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest

/**
 * The test class used for any tests that include [TestLevel.FIR] or [TestLevel.Diagnostics] but not [TestLevel.IR] or [TestLevel.Run].
 */
public open class AbstractLeveledFirTest : AbstractFirLightTreeDiagnosticsTest() {
    override fun configure(builder: NonGroupingPhaseTestConfigurationBuilder) {
        val levels = TestSpec.forTestClass(this)
        levels.preConfigure(this, builder)
        super.configure(builder)
        levels.configure(this, builder)
    }
}