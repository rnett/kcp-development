package dev.rnett.kcp.development.registrar

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import kotlin.reflect.KClass

/**
 * A [CompilerPluginRegistrar] that uses a specification/configuration [T] to control its behavior.
 * The spec is derived from the compiler configuration and can be easily overridden in tests.
 */
@ExperimentalCompilerApi
public abstract class BaseSpecCompilerPluginRegistrar<T> : CompilerPluginRegistrar() {

    public abstract fun irExtension(spec: T): IrGenerationExtension?
    public abstract fun firExtension(spec: T): FirExtensionRegistrar?

    public open fun ExtensionStorage.registerAdditionalExtensions(spec: T) {}

    public abstract fun produceSpec(configuration: CompilerConfiguration): T


    public companion object {
        public val testSpecKey: CompilerConfigurationKey<List<TestSpec<*>>> = CompilerConfigurationKey<List<TestSpec<*>>>("testSpecs")
    }

    public data class TestSpec<T>(val forRegistrar: KClass<out BaseSpecCompilerPluginRegistrar<T>>, val spec: T)

    final override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val testSpecs = configuration[testSpecKey] ?: emptyList()
        val ownTestSpec = testSpecs.lastOrNull { it.forRegistrar == this@BaseSpecCompilerPluginRegistrar::class }

        val spec = ownTestSpec?.let { it.spec as T } ?: produceSpec(configuration)

        irExtension(spec)?.let { IrGenerationExtension.registerExtension(it) }
        firExtension(spec)?.let { FirExtensionRegistrarAdapter.registerExtension(it) }
        registerAdditionalExtensions(spec)

    }
}