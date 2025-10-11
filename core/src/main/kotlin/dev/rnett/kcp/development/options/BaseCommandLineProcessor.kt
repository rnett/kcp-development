package dev.rnett.kcp.development.options

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

/**
 * An enhanced [CommandLineProcessor] that has its options configured via a [CompilerOptionsHost].
 */
@ExperimentalCompilerApi
public abstract class BaseCommandLineProcessor : CommandLineProcessor {
    public abstract val options: CompilerOptionsHost
    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        options.options.find { it.name == option.optionName }?.processOption(value, configuration)
            ?: error("Unknown option ${option.optionName}")
    }

    override val pluginOptions: Collection<AbstractCliOption>
        get() = options.options.map { it.cliOption }
}