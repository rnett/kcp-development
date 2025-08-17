package dev.rnett.kcp.development.options

import org.jetbrains.kotlin.cli.common.toBooleanLenient

public abstract class CompilerOptionsHost {
    private val _options = mutableListOf<CompilerOption<*>>()
    public val options: List<CompilerOption<*>> = _options

    public fun <T> singular(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> T?,
    ): Lazy<CompilerOption.Singular<T & Any>> = CompilerOption.Singular(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    public fun <T> singular(
        name: String,
        defaultValue: T,
        valueDescription: String,
        description: String,
        transform: (String) -> T?,
    ): Lazy<CompilerOption.SingularWithDefault<T>> = CompilerOption.SingularWithDefault(name, valueDescription, description, transform, defaultValue)
        .also { _options += it }
        .let(::lazyOf)

    public fun <T> repeated(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> T?,
    ): Lazy<CompilerOption.Repeated<T>> = CompilerOption.Repeated(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    public fun <K : Any, V : Any> keyed(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
        transform: (String) -> Pair<K, V>?
    ): Lazy<CompilerOption.Keyed<K, V>> = CompilerOption.Keyed(name, valueDescription, description, required, transform)
        .also { _options += it }
        .let(::lazyOf)

    public fun singularString(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true
    ): Lazy<CompilerOption.Singular<String>> = singular(name, valueDescription, description, required) { it }

    public fun singularString(
        name: String,
        defaultValue: String,
        valueDescription: String,
        description: String,
    ): Lazy<CompilerOption.SingularWithDefault<String>> = singular(name, defaultValue, valueDescription, description) { it }

    public fun repeatedString(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean = true,
    ): Lazy<CompilerOption.Repeated<String>> = repeated(name, valueDescription, description, required) { it }

    public fun flag(
        name: String,
        description: String,
        required: Boolean = true
    ): Lazy<CompilerOption.Singular<Boolean>> = singular(name, "<true|false>", description, required) { it.toBooleanLenient() }

    public fun flag(
        name: String,
        defaultValue: Boolean,
        description: String,
    ): Lazy<CompilerOption.SingularWithDefault<Boolean>> = singular(name, defaultValue, "<true|false>", description) { it.toBooleanLenient() ?: defaultValue }
}