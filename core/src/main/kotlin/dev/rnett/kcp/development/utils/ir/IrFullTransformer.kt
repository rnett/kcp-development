package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

@ExperimentalIrHelpers
public abstract class IrFullTransformer(override val context: IrPluginContext) : IrElementTransformerVoid(), WithIrContext {

}