package dev.rnett.kcp.development.testing.tests.levels

import java.util.EnumSet


public enum class TestLevel {
    Diagnostics,
    FIR,
    IR,
    Run;

    public val full: TestSpec by lazy { TestSpec(EnumSet.range(Diagnostics, this)) }
    public val only: TestSpec by lazy { TestSpec(EnumSet.of(this)) }
}

