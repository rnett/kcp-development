# Module core

Provides utilities for declaring command line processors and plugin registrars.

## BaseCommandLineProcessor and CompilerOptionsHost

`CompilerOptionsHost` is a base class for an object that declares the compiler plugin's CLI and compiler configuration options.

For example:

```kotlin

object MyPluginOptions : CompilerOptionsHost {
    val isExperimentalTransformEnabled by flag("experimental-transform", "Enable experimental transform")
    val packageToTransform by singularString("package", "<package FQN>", "Package to transform")
}

class MyCliProcessor : BaseCommandLineProcessor(MyPluginOptions) {
    override val pluginId: String = "my-plugin"
    override val options: CompilerOptionsHost = MyPluginOptions
}

```

This takes care of parsing the command line arguments and adding them to the compiler configuration.
The properties can be used to access the configuration, e.g. `compilerConfiguration[MyPluginOptions.packageToTransform]`.

## BaseSpecCompilerPluginRegistrar

`BaseSpecCompilerPluginRegistrar` is a base class for a compiler plugin registrar that uses a two-step process to create a spec object from compiler configuration, and then create extensions from that spec object.
This is useful for clarity, but also because the spec can easily be set in tests - that way you don't need to muck around with individual compiler argument strings.

# Package dev.rnett.kcp.development.options

`CompilerOptionsHost` and `BaseCommandLineProcessor`.

# Package dev.rnett.kcp.development.registrar

`BaseSpecCompilerPluginRegistrar`.