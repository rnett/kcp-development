package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

@ExperimentalIrHelpers
public abstract class IrFullProcessor(override val context: IrPluginContext) : IrVisitorVoid(), WithIrContext {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

}