package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.runtime.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.kcp.development.testing.tests.KcpDevelopmentCompilerTest.Companion.applyDefaults
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class AbstractIrDumpTest : AbstractFirLightTreeJvmIrTextTest(), KcpDevelopmentCompilerTest, AppliesDynamicConfigurators {
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
        super.configure(builder)
        applyConfiguration(builder)
        builder.defaultDirectives {
        }
    }
}