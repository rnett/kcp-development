# Module compiler-plugin-plugins

A suite of Gradle plugins that shoule be applied to your compiler plugin project.

* `dev.rnett.kcp-development.compiler-plugin` - apply all of the other plugins
* `dev.rnett.kcp-development.compiler-plugin.base` - set up the extensions, basic configuration, and add the kcp-development runtime dependencies if configured to
* `dev.rnett.kcp-development.compiler-plugin.shadow` - applies the `com.gradleup.shadow` plugin and configures it to produce shaded compiler plugin jars. This is required for compiler plugins to be successfully consumed.
* `dev.rnett.kcp-development.compiler-plugin.buildconfig` - applies the `com.github.gmazzo.buildconfig` plugin and configures it to add a `KOTLIN_PLUGIN_ID` field based on the project's group and name.
* `dev.rnett.kcp-development.compiler-plugin.services` - generated service declarations for the compiler plugin registrar and command line processor, if configured with the extension.
* `dev.rnett.kcp-development.compiler-plugin.testing` - sets up the Kotlin compiler testing framework, with additional options for the kcp-development `test-support` library.
  This includes adding a `compilerTestRuntimeClasspath` configuration that can be used to configure the compile- and run-time classpath for compiler tests.

For how to set up the plugin consult the [extension](dev.rnett.kcp.development.CompilerPluginDevelopmentExtension) documentation (the extension is registered as `compilerPluginDevelopment`).
No properties are required to get started, but you will likely want to set the following:

```kotlin
compilerPluginDevelopment {
    // the class of your test generator, an object that extends `BaseTestGenerator` in the `testFixtures` source set 
    testGenerator = "my.plugin.tests.generator.MyTestGenerator"
    // your `CommandLineProcessor` class. Service declarations will not be created if this is not set.
    commandLineProcessor = "my.plugin.MyCommandLineProcessor"
    // your `CompilerPluginRegistrar` class. Service declarations will not be created if this is not set. If set, will automatically be used in `BaseTestGenerator`-generated tests.
    compilerPluginRegistrar = "my.plugin.MyCompilerPluginRegistrar"

}
```

