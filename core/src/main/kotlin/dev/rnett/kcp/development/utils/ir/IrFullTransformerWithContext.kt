package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalIrHelpers
public abstract class IrFullTransformerWithContext(override val context: IrPluginContext) : IrElementTransformerVoidWithContext(), WithIrContext {

    @OptIn(ExperimentalContracts::class)
    protected inline fun <R> withBuilderForCurrentScope(startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET, block: DeclarationIrBuilder.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        val symbol = currentScope?.scope?.scopeOwnerSymbol ?: currentFile.symbol

        return builtIns.createIrBuilder(symbol, startOffset, endOffset).block()
    }

}