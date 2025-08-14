package dev.rnett.kcp.development.options

import org.jetbrains.kotlin.cli.common.toBooleanLenient

abstract class CompilerOptionsHost {
    private val _options = mutableListOf<CompilerOption<*>>()
    val options: List<CompilerOption<*>> = _options

    fun <T> singular(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> T?,
    ) = CompilerOption.Singular(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    fun <T> singular(
        name: String,
        defaultValue: T,
        valueDescription: String,
        description: String,
        transform: (String) -> T?,
    ) = CompilerOption.SingularWithDefault(name, valueDescription, description, transform, defaultValue)
        .also { _options += it }
        .let(::lazyOf)

    fun <T> repeated(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> T?,
    ) = CompilerOption.Repeated(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    fun <K : Any, V : Any> keyed(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> Pair<K, V>?
    ) = CompilerOption.Keyed(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    fun singularString(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true
    ) = singular(name, valueDescription, description, required) { it }

    fun singularString(
        name: String,
        defaultValue: String,
        valueDescription: String,
        description: String,
    ) = singular(name, defaultValue, valueDescription, description) { it }

    fun repeatedString(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
    ) = repeated(name, valueDescription, description, required) { it }

    fun flag(
        name: String,
        description: String,
        required: Boolean = true
    ) = singular(name, "<true|false>", description, required) { it.toBooleanLenient() }

    fun flag(
        name: String,
        defaultValue: Boolean,
        description: String,
    ) = singular(name, defaultValue, "<true|false>", description) { it.toBooleanLenient() ?: defaultValue }
}