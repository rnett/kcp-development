package test

import dev.rnett.kcp.development.registrar.BaseCompilerPluginRegistrar
import dev.rnett.kcp.development.registrar.SpecBasedCompilerPluginRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

data class Spec(val name: String)

@OptIn(ExperimentalCompilerApi::class)
class Registrar : BaseCompilerPluginRegistrar<Spec>() {
    override val registrar: SpecBasedCompilerPluginRegistrar<Spec>
        get() = TODO("Not yet implemented")

    override fun produceSpec(configuration: CompilerConfiguration): Spec {
        return Spec("prod")
    }

    override val supportsK2: Boolean = true
}

@OptIn(ExperimentalCompilerApi::class)
object PluginRegistrar : SpecBasedCompilerPluginRegistrar<Spec>() {
    override fun irExtension(spec: Spec): IrGenerationExtension? = null

    override fun firExtension(spec: Spec): FirExtensionRegistrar? = null
}
