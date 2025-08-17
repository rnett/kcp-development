package dev.rnett.kcp.development.testing.tests.levels

import org.jetbrains.kotlin.generators.model.AnnotationArgumentModel
import org.jetbrains.kotlin.generators.model.AnnotationModel
import org.jetbrains.kotlin.test.backend.handlers.IrPrettyKotlinDumpHandler
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureIrHandlersStep
import org.jetbrains.kotlin.test.builders.firHandlersStep
import org.jetbrains.kotlin.test.configuration.setupHandlersForDiagnosticTest
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_IR
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_KT_IR
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.DIAGNOSTICS
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.LANGUAGE
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirCfgDumpHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDumpHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirScopeDumpHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirVFirDumpHandler
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import java.util.EnumSet
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
public annotation class TestWithLevel(val level: TestLevel)

public data class TestSpec(val levels: EnumSet<TestLevel>) {
    public constructor(levels: Collection<TestLevel>) : this(
        if (levels.isEmpty()) EnumSet.noneOf(TestLevel::class.java) else EnumSet.copyOf(levels)
    )

    public constructor(first: TestLevel, vararg rest: TestLevel) : this(EnumSet.of(first, *rest))

    public operator fun contains(level: TestLevel): Boolean = level in levels

    public fun forEachLevel(block: (TestLevel) -> Unit) {
        levels.forEach(block)
    }

    public fun forEachNotLevel(block: (TestLevel) -> Unit) {
        EnumSet.complementOf(levels).forEach(block)
    }

    public operator fun plus(other: TestSpec): TestSpec = TestSpec(levels + other.levels)
    public operator fun plus(other: TestLevel): TestSpec = TestSpec(levels + other)
    public operator fun minus(other: TestLevel): TestSpec = TestSpec(levels - other)

    public fun annotations(): List<AnnotationModel> = levels.map { AnnotationModel(TestWithLevel::class.java, listOf(AnnotationArgumentModel("level", it))) }

    public fun testClass(): KClass<*>? {
        if (levels.isEmpty())
            return null

        val max = levels.max()
        return when (max) {
            TestLevel.Diagnostics,
            TestLevel.Fir -> AbstractLeveledFirTest::class

            TestLevel.Ir -> AbstractLeveledIrTest::class
            TestLevel.Run -> AbstractLeveledBoxTest::class
        }
    }

    public companion object {
        internal fun forTestClass(testClass: Any) = TestSpec(testClass::class.java.getAnnotationsByType(TestWithLevel::class.java).map { it.level })
    }

    public fun preConfigure(test: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
    }

    public fun configure(test: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder) {
        with(builder) {
            forEachLevel {
                when (it) {
                    TestLevel.Diagnostics -> {
                        defaultDirectives {
                            -DIAGNOSTICS
                            DIAGNOSTICS.with(listOf("+infos", "+warnings", "+errors"))
                            LANGUAGE + "+EnableDfaWarningsInK2"
                        }
                        // because this is applied later
                        forTestsNotMatching(Regex("$^")) {
                            defaultDirectives {
                                -DIAGNOSTICS
                                DIAGNOSTICS.with(listOf("+infos", "+warnings", "+errors"))
                            }
                        }
                        if (test !is AbstractLeveledFirTest && test !is AbstractLeveledBoxTest) {
                            firHandlersStep {
                                setupHandlersForDiagnosticTest()
                            }
                        }
                    }

                    TestLevel.Fir -> {
                        defaultDirectives {
                            +FIR_DUMP
                        }
                        if (test !is AbstractLeveledFirTest && test !is AbstractLeveledIrTest) {
                            firHandlersStep {
                                useHandlers(
                                    ::FirDumpHandler,
                                    ::FirCfgDumpHandler,
                                    ::FirVFirDumpHandler,
                                    ::FirScopeDumpHandler,
                                )
                            }
                        }
                    }

                    TestLevel.Ir -> {
                        defaultDirectives {
                            +DUMP_IR
                            +DUMP_KT_IR
                            +IGNORE_DEXING
                        }
                        if (test !is AbstractLeveledIrTest) {
                            configureIrHandlersStep {
                                useHandlers(::IrPrettyKotlinDumpHandler)
                            }
                        }
                    }

                    TestLevel.Run -> {}
                }
            }
            forEachNotLevel {
                when (it) {
                    TestLevel.Diagnostics -> {
                        defaultDirectives {
                            -DIAGNOSTICS
                            DIAGNOSTICS.with(listOf("-infos", "-warnings", "-errors"))
                        }
                    }

                    TestLevel.Fir -> {
                        defaultDirectives {
                            -FIR_DUMP
                        }
                    }

                    TestLevel.Ir -> defaultDirectives {
                        -DUMP_IR
                        -DUMP_KT_IR
                    }

                    TestLevel.Run -> {}
                }
            }
        }
    }

    public fun badTestSuiteName(): String = levels.joinToString("") { it.name }
}