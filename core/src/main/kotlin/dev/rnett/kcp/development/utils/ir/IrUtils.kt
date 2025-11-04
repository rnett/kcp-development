package dev.rnett.kcp.development.utils.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isFunctionOrKFunction
import org.jetbrains.kotlin.name.SpecialNames

/**
 * Create a lambda expression.
 *
 * The type will be inferred from the built function if not provided, which means that in that scenario it must have a defined return type.
 * If [functionType] is a well-formed function type with a concrete, explicit return type, the returnType will be inferred from it.
 * Don't rely on this unless the function type is a constant.
 */
@ExperimentalIrHelpers
public fun createLambda(
    context: IrPluginContext,
    parent: IrDeclarationParent? = null,
    functionType: IrType? = null,
    builder: IrSimpleFunction.() -> Unit,
): IrFunctionExpressionImpl {

    // We're intentionally ignoring whether the function is suspending or not.  For some reason handling it actually causes errors

    val lambdaFunction = context.irFactory.buildFun {
        visibility = DescriptorVisibilities.LOCAL
        modality = Modality.FINAL
        name = SpecialNames.ANONYMOUS
    }.apply {
        parent?.let { this.parent = it }
        origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        if (functionType != null && functionType.isFunctionOrKFunction() && functionType is IrSimpleType) {
            functionType.arguments.last().typeOrNull?.let {
                returnType = it
            }
        }
        this.builder()
    }

    val functionType = functionType ?: context.irBuiltIns.functionN(lambdaFunction.parameters.size)
        .typeWith(lambdaFunction.parameters.map { it.type } + listOf(lambdaFunction.returnType))

    return IrFunctionExpressionImpl(
        UNDEFINED_OFFSET,
        UNDEFINED_OFFSET,
        type = functionType,
        origin = IrStatementOrigin.LAMBDA,
        function = lambdaFunction
    )
}