package dev.rnett.kcp.development.testing.configuration

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

@ExperimentalCompilerApi
class CompilerPluginRegistrarConfigurator(testServices: TestServices, val registrar: CompilerPluginRegistrar) : EnvironmentConfigurator(testServices) {
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        with(registrar) { registerExtensions(configuration) }
    }
}

@ExperimentalCompilerApi
fun TestConfigurationBuilder.withCompilerPluginRegistrar(registrar: CompilerPluginRegistrar) =
    useConfigurators({ CompilerPluginRegistrarConfigurator(it, registrar) })

fun interface TestSpecProducer<T> {
    fun produceSpec(original: Result<T>, module: TestModule, configuration: CompilerConfiguration): T
}