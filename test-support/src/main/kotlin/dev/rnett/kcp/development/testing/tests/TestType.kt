package dev.rnett.kcp.development.testing.tests

import kotlin.reflect.KClass

//TODO a more defined progression Diagnostic -> FIR -> IR -> Run.  That allows you to mix and match parts.

enum class TestType(val directoryName: String, val testSuffix: String, val cls: KClass<out AppliesDynamicConfigurators>) {
    Diagnostic("diagnostics", "Diagnostics", AbstractDiagnosticsTest::class),
    Fir("fir", "Fir", AbstractDumpingFirDiagnosticsTest::class),
    Ir("ir", "Ir", AbstractIrDumpTest::class),
    Dump("dump", "Dump", AbstractDumpTest::class),
    Box("box", "Box", AbstractDumpingJvmBoxTest::class),
    BlackBox("run", "BlackBox", AbstractJvmBoxTest::class);

    companion object {
        val byDirectoryName = entries.associateBy { it.directoryName }

        init {
            require(entries.map { it.directoryName }.toSet().size == entries.size) { "All directory names must be unique" }
            require(entries.map { it.testSuffix }.toSet().size == entries.size) { "All test suffixes must be unique" }
        }
    }
}