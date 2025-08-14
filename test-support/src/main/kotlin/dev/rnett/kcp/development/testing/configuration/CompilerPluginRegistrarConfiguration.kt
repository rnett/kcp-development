package dev.rnett.kcp.development.testing.configuration

import dev.rnett.kcp.development.registrar.SpecBasedCompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

//TODO look at having a Gradle plugin property that points to the registrar & cli processors.  Would be provided to tests as a sys prop, and this could be auto-registered.

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

fun interface SpecProducer<T> {
    fun produceSpec(module: TestModule, configuration: CompilerConfiguration): T
}

//TODO alter spec produced by compiler config instead of producing from scratch?  Would need to wrap in a Result

@ExperimentalCompilerApi
class SpecBasedCompilerPluginRegistrarConfigurator<T>(
    testServices: TestServices,
    val registrar: SpecBasedCompilerPluginRegistrar<T>,
    val specProducer: SpecProducer<T>,
) : EnvironmentConfigurator(testServices) {
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        registrar.registerExtensions(this, specProducer.produceSpec(module, configuration))
    }
}

@ExperimentalCompilerApi
fun <T> TestConfigurationBuilder.withCompilerPluginRegistrar(registrar: SpecBasedCompilerPluginRegistrar<T>, specProducer: SpecProducer<T>) =
    useConfigurators({ SpecBasedCompilerPluginRegistrarConfigurator(it, registrar, specProducer) })