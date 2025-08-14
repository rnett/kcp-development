package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.kcp.development.testing.tests.levels.TestSpec

enum class TestType(val spec: TestSpec) {
    Diagnostics(TestLevel.Diagnostics.full),
    Fir(TestLevel.Fir.only),
    Ir(TestLevel.Ir.only),
    Compile(TestLevel.Ir.full),
    Run(TestLevel.Run.only),
    Box(TestLevel.Run.full);

    val directoryName by lazy { name.lowercase() }

    val testSuiteName by lazy { "${name}TestGenerated" }

    companion object {
        val byDirectoryName = entries.associateBy { it.directoryName }
    }
}