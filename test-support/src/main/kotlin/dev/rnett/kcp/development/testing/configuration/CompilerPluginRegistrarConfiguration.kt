package dev.rnett.kcp.development.testing.configuration

import dev.rnett.kcp.development.testing.generation.BaseTestGenerator
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

/**
 * Configures a compiler plugin.
 * Configured automatically based on the test generation setup when using [BaseTestGenerator].
 */
@ExperimentalCompilerApi
public class CompilerPluginRegistrarConfigurator(testServices: TestServices, public val registrar: CompilerPluginRegistrar) : EnvironmentConfigurator(testServices) {
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        with(registrar) { registerExtensions(configuration) }
    }
}

/**
 * Configures a compiler plugin.
 * Configured automatically based on the test generation setup when using [BaseTestGenerator].
 */
@ExperimentalCompilerApi
public fun TestConfigurationBuilder.withCompilerPluginRegistrar(registrar: CompilerPluginRegistrar): Unit =
    useConfigurators({ CompilerPluginRegistrarConfigurator(it, registrar) })