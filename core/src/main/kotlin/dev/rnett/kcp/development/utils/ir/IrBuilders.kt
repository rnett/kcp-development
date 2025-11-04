package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalIrHelpers
@OptIn(ExperimentalContracts::class)
context(ctx: WithIrContext)
public inline fun <R> IrSymbolOwner.withBuilder(startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET, block: DeclarationIrBuilder.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return ctx.builtIns.createIrBuilder(symbol, startOffset, endOffset).block()
}


@ExperimentalIrHelpers
@OptIn(ExperimentalContracts::class)
context(ctx: WithIrContext)
public inline fun <R> withUnscopedBuilder(startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET, block: IrBuilder.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return object : IrBuilder(ctx.context, startOffset, endOffset) {}.block()
}