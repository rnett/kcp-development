package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.tests.KcpDevelopmentCompilerTest.Companion.applyDefaults
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_DUMP
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

abstract class AbstractDumpingFirDiagnosticsTest : AbstractFirLightTreeDiagnosticsTest(), KcpDevelopmentCompilerTest, AppliesDynamicConfigurators {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return ClasspathBasedStandardLibrariesPathProvider
    }

    override fun configuration(builder: TestConfigurationBuilder) {
        builder.applyDefaults()
    }

    final override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        applyConfiguration(builder)
        builder.defaultDirectives {
            +FIR_DUMP
        }
    }
}