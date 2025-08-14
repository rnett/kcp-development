package dev.rnett.kcp.development.options

import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

sealed class CompilerOption<T>(
    val name: String,
    valueDescription: String,
    description: String,
    required: Boolean,
    allowMultipleOccurrences: Boolean,
) {
    abstract fun processOption(value: String, configuration: CompilerConfiguration)
    abstract fun getOption(configuration: CompilerConfiguration): T?

    val cliOption: CliOption = CliOption(name, valueDescription, description, required, allowMultipleOccurrences)

    val key: CompilerConfigurationKey<T> = CompilerConfigurationKey.create<T>(name)

    sealed class WithDefault<T>(name: String, valueDescription: String, description: String, required: Boolean, allowMultipleOccurrences: Boolean) :
        CompilerOption<T>(name, valueDescription, description, required, allowMultipleOccurrences) {
        abstract fun defaultValue(): T

        fun getOptionOrDefault(configuration: CompilerConfiguration): T = getOption(configuration) ?: defaultValue()

    }

    class Singular<T : Any>(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean,
        val transform: (String) -> T?,
    ) : CompilerOption<T>(name, valueDescription, description, required, false) {
        override fun processOption(value: String, configuration: CompilerConfiguration) {
            configuration.putIfNotNull(key, transform(value))
        }

        override fun getOption(configuration: CompilerConfiguration): T? {
            return configuration.get(key)
        }
    }

    class SingularWithDefault<T>(
        name: String,
        valueDescription: String,
        description: String,
        val transform: (String) -> T?,
        val defaultValue: T,
    ) : WithDefault<T>(name, valueDescription, description, false, false) {
        override fun processOption(value: String, configuration: CompilerConfiguration) {
            configuration.putIfNotNull(key, transform(value))
        }

        override fun getOption(configuration: CompilerConfiguration): T? {
            return configuration.get(key)
        }

        override fun defaultValue(): T = defaultValue
    }

    class Repeated<T>(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean,
        val transform: (String) -> T?,
    ) : WithDefault<List<T>>(name, valueDescription, description, required, true) {
        override fun processOption(value: String, configuration: CompilerConfiguration) {
            val value = transform(value) ?: return
            configuration.add(key, value)
        }

        override fun getOption(configuration: CompilerConfiguration): List<T> {
            return configuration.getList(key)
        }

        override fun defaultValue(): List<T> = emptyList()
    }

    class Keyed<K : Any, V : Any>(
        name: String,
        valueDescription: String,
        description: String,
        required: Boolean,
        val transform: (String) -> Pair<K, V>?,
    ) : WithDefault<Map<K, V>>(name, valueDescription, description, required, true) {
        override fun processOption(value: String, configuration: CompilerConfiguration) {
            val value = transform(value) ?: return
            configuration.put(key, value.first, value.second)
        }

        override fun getOption(configuration: CompilerConfiguration): Map<K, V> {
            return configuration.getMap(key)
        }

        override fun defaultValue(): Map<K, V> = emptyMap()
    }
}

operator fun <T : Any> CompilerConfiguration.get(option: CompilerOption<T>): T? = option.getOption(this)

operator fun <T : Any> CompilerConfiguration.get(option: CompilerOption.WithDefault<T>): T = option.getOptionOrDefault(this)