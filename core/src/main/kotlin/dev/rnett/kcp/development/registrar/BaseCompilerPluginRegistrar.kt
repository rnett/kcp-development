package dev.rnett.kcp.development.registrar

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@ExperimentalCompilerApi
abstract class BaseCompilerPluginRegistrar<T> : CompilerPluginRegistrar() {

    abstract val registrar: SpecBasedCompilerPluginRegistrar<T>
    abstract fun produceSpec(configuration: CompilerConfiguration): T

    final override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        registrar.registerExtensions(this, produceSpec(configuration))
    }
}

//TODO needs a better, not so much conflicting name
@ExperimentalCompilerApi
abstract class SpecBasedCompilerPluginRegistrar<T> {
    abstract fun irExtension(spec: T): IrGenerationExtension?
    abstract fun firExtension(spec: T): FirExtensionRegistrar?

    fun registerExtensions(storage: CompilerPluginRegistrar.ExtensionStorage, spec: T) = with(storage) {
        irExtension(spec)?.let { IrGenerationExtension.registerExtension(it) }
        firExtension(spec)?.let { FirExtensionRegistrarAdapter.registerExtension(it) }
        registerAdditionalExtensions(spec)
    }

    open fun CompilerPluginRegistrar.ExtensionStorage.registerAdditionalExtensions(spec: T) {}
}