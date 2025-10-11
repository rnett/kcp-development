package dev.rnett.kcp.development.testing.tests.levels

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
import org.jetbrains.kotlin.test.directives.TestPhaseDirectives.LATEST_PHASE_IN_PIPELINE
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest
import org.jetbrains.kotlin.test.services.PhasedPipelineChecker
import org.jetbrains.kotlin.test.services.TestPhase
import org.jetbrains.kotlin.utils.bind

/**
 * The test class used for any tests that include [TestLevel.IR] but not [TestLevel.Run].
 */
public open class AbstractLeveledIrTest : AbstractFirLightTreeJvmIrTextTest() {

    override fun configure(builder: TestConfigurationBuilder) {
        // this - pre super
        val levels = TestSpec.forTestClass(this)
        levels.preConfigure(this, builder)


        // AbstractJvmIrTextTest
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
        // AbstractFirJvmIrTextTest
        builder.additionalK2ConfigurationForIrTextTest(parser)

        // this - post super
        levels.configure(this, builder)
    }
}