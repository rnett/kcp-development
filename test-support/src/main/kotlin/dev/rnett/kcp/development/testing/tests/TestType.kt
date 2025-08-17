package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec

public enum class TestType(public val spec: TestSpec) {
    Diagnostics(TestLevel.Diagnostics.full),
    Fir(TestLevel.Fir.only),
    Ir(TestLevel.Ir.only),
    Compile(TestLevel.Ir.full),
    Run(TestLevel.Run.only),
    Box(TestLevel.Run.full);

    public val directoryName: String by lazy { name.lowercase() }

    public val testSuiteName: String by lazy { "${name}TestGenerated" }

    public companion object {
        public val byDirectoryName: Map<String, TestType> = entries.associateBy { it.directoryName }
    }
}