package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec

/**
 * A test "type" that includes one or more [TestLevel]s.
 * Will be auto-applied for tests in directories with the name of the type (case-insensitive).
 */
public enum class TestType(public val spec: TestSpec) {
    /**
     * Run diagnostics tests, no dumps - [TestLevel.Diagnostics] only.
     */
    Diagnostics(TestLevel.Diagnostics.full),

    /**
     * Run and dump FIR - [TestLevel.FIR] only.
     */
    Fir(TestLevel.FIR.only),

    /**
     * Combines [Diagnostics] and [Fir].
     */
    Frontend(TestLevel.Diagnostics.full + TestLevel.FIR.full),

    /**
     * Run and dump IR - [TestLevel.IR] only.
     */
    Ir(TestLevel.IR.only),

    /**
     * Alias for [Ir]. If additional backend levels are added in the future, will be updated to include them.
     */
    Backend(TestLevel.IR.only),

    /**
     * Run diagnostics, FIR, and IR - [TestLevel.Diagnostics], [TestLevel.FIR], and [TestLevel.IR].
     */
    Compile(TestLevel.IR.full),

    /**
     * Run the `box()` execution test - [TestLevel.Run] only.
     */
    Run(TestLevel.Run.only),

    /**
     * Run [Compile] and the `box()` execution test - [TestLevel.Diagnostics], [TestLevel.FIR], [TestLevel.IR], and [TestLevel.Run]
     */
    Box(TestLevel.Run.full);

    public val directoryName: String by lazy { name.lowercase() }

    public val testSuiteName: String by lazy { "${name}TestGenerated" }

    public companion object {
        public val byDirectoryName: Map<String, TestType> = entries.associateBy { it.directoryName }
    }
}