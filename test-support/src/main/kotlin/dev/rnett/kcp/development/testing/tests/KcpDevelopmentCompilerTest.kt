package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.preprocessors.PackagePreprocessor
import dev.rnett.kcp.development.testing.preprocessors.useBoxOptInPreprocessor
import dev.rnett.kcp.development.testing.preprocessors.useImportsPreprocessor
import dev.rnett.kcp.development.testing.runtime.useTestRuntime
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives

interface KcpDevelopmentCompilerTest {
    companion object {
        fun TestConfigurationBuilder.applyDefaults() {
            defaultDirectives {
                JVM_TARGET.with(JvmTarget.JVM_17)
                +FULL_JDK
                +WITH_STDLIB
            }
        }
    }

    val imports: Set<String> get() = emptySet()

    val boxOptIns: Set<String> get() = emptySet()

    val globalOptIns: Set<String> get() = emptySet()

    fun configuration(builder: TestConfigurationBuilder)
}

internal fun KcpDevelopmentCompilerTest.applyConfiguration(builder: TestConfigurationBuilder) {
    builder.useTestRuntime()
    builder.useSourcePreprocessor(::PackagePreprocessor)
    builder.useImportsPreprocessor(imports)
    builder.useBoxOptInPreprocessor(boxOptIns)
    builder.defaultDirectives {
        LanguageSettingsDirectives.OPT_IN.with(globalOptIns.toList())
    }
    configuration(builder)
}