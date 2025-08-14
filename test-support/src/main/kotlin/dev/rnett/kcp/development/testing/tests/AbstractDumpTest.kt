package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.tests.KcpDevelopmentCompilerTest.Companion.applyDefaults
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.handlers.IrPrettyKotlinDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.IrTextDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.IrTreeVerifierHandler
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureIrHandlersStep
import org.jetbrains.kotlin.test.configuration.additionalK2ConfigurationForIrTextTest
import org.jetbrains.kotlin.test.configuration.commonConfigurationForJvmTest
import org.jetbrains.kotlin.test.configuration.commonHandlersForCodegenTest
import org.jetbrains.kotlin.test.configuration.setupDefaultDirectivesForIrTextTest
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP
import org.jetbrains.kotlin.test.directives.TestPhaseDirectives.LATEST_PHASE_IN_PIPELINE
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.PhasedPipelineChecker
import org.jetbrains.kotlin.test.services.TestPhase
import org.jetbrains.kotlin.utils.bind

open class AbstractDumpTest : AbstractFirLightTreeJvmIrTextTest(), KcpDevelopmentCompilerTest, AppliesDynamicConfigurators {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return ClasspathBasedStandardLibrariesPathProvider
    }

    override fun configuration(builder: TestConfigurationBuilder) {
        builder.applyDefaults()
        builder.defaultDirectives {
            +IGNORE_DEXING
        }
    }

    final override fun configure(builder: TestConfigurationBuilder) {
        // super.super
        with(builder) {
            commonConfigurationForJvmTest(targetFrontend, frontendFacade, frontendToBackendConverter, backendFacade)
            commonHandlersForCodegenTest()
            setupDefaultDirectivesForIrTextTest()
            configureIrHandlersStep {
                useHandlers(
                    ::IrTextDumpHandler,
                    ::IrTreeVerifierHandler,
                    ::IrPrettyKotlinDumpHandler,
//                    ::IrSourceRangesDumpHandler, //TODO disabled because it uses *.fir.txt as the dump, which conflicts with the actual FIR dump
                )
            }

            defaultDirectives {
                LATEST_PHASE_IN_PIPELINE with TestPhase.BACKEND
            }

            useAfterAnalysisCheckers(
                ::BlackBoxCodegenSuppressor,
                ::PhasedPipelineChecker.bind(TestPhase.BACKEND)
            )
            enableMetaInfoHandler()
        }
        // super
        builder.additionalK2ConfigurationForIrTextTest(parser)

        applyConfiguration(builder)
        builder.defaultDirectives {
            +FIR_DUMP
        }
    }
}