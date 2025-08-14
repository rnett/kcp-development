package dev.rnett.kcp.development.testing.tests.levels

import java.util.EnumSet


enum class TestLevel {
    Diagnostics,
    Fir,
    Ir,
    Run;

    val full: TestSpec by lazy { TestSpec(EnumSet.range(Diagnostics, this)) }
    val only: TestSpec by lazy { TestSpec(EnumSet.of(this)) }
}

