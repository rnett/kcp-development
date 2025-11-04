package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrFactory

/**
 * A type that provides a [IrPluginContext] - for use as a context parameter.
 */
@ExperimentalIrHelpers
public interface WithIrContext {
    public val context: IrPluginContext
    public val builtIns: IrBuiltIns get() = context.irBuiltIns
    public val factory: IrFactory get() = context.irFactory
}