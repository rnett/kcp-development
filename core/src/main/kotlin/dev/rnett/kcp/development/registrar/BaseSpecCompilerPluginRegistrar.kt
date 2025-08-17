package dev.rnett.kcp.development.registrar

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import kotlin.reflect.KClass

@ExperimentalCompilerApi
public abstract class BaseSpecCompilerPluginRegistrar<T> : CompilerPluginRegistrar() {

    public abstract val extensions: SpecBasedCompilerPluginExtensions<T>
    public abstract fun produceSpec(configuration: CompilerConfiguration): T


    public companion object {
        public val testSpecKey: CompilerConfigurationKey<List<TestSpec<*>>> = CompilerConfigurationKey<List<TestSpec<*>>>("testSpecs")
    }

    public data class TestSpec<T>(val forRegistrar: KClass<out BaseSpecCompilerPluginRegistrar<T>>, val spec: T)

    final override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {

        val testSpecs = configuration[testSpecKey] ?: emptyList()
        val ownTestSpec = testSpecs.lastOrNull { it.forRegistrar == this@BaseSpecCompilerPluginRegistrar::class }

        if (ownTestSpec == null) {
            extensions.registerExtensions(this, produceSpec(configuration))
        } else {
            val spec = ownTestSpec.spec as T
            extensions.registerExtensions(this, spec)
        }

    }
}

@ExperimentalCompilerApi
public abstract class SpecBasedCompilerPluginExtensions<T> {
    public abstract fun irExtension(spec: T): IrGenerationExtension?
    public abstract fun firExtension(spec: T): FirExtensionRegistrar?

    public fun registerExtensions(storage: CompilerPluginRegistrar.ExtensionStorage, spec: T): Unit = with(storage) {
        irExtension(spec)?.let { IrGenerationExtension.registerExtension(it) }
        firExtension(spec)?.let { FirExtensionRegistrarAdapter.registerExtension(it) }
        registerAdditionalExtensions(spec)
    }

    public open fun CompilerPluginRegistrar.ExtensionStorage.registerAdditionalExtensions(spec: T) {}
}