# Module test-support

## Testing your compiler plugin

This module adds some wrappers around the Kotlin compiler test framework to make it easier to test plugins.
Conventions are focused around testing an individual plugin, not the compiler as a whole.
In practice this means it is designed for:

* A single test per test data file
* Lots of test data files, not a lot of distinct compiler configurations

It requires the use of the `dev.rnett.kcp-development.compiler-plugin.testing` Gradle plugin.

Like when using the base test framework, you create a test generator in `src/testFixtures` and put the test data in `src/testData`.
Unlike the base framework, you don't create a `main` method, instead, you create a class with a no-arg constructor that extends `BaseTestGenerator`.
Set the Gradle `compilerPluginDevelopment.testGenerator` property to the FQN of this class in your build script.
Auto-generation (see below) is also available and is used by default.

Even if you don't extend `BaseTestGenerator`, you can set `compilerPluginDevelopment.testGenerator` to your main class and `compilerPluginDevelopment.useTestGenerator` to false.
You can still use the utilities in this module, just not the test generation DSL.

When implementing `BaseTestGenerator`, the primary method to implement is `generateTests`, which is a DSL for setting up your test suites.
You configure nested groups corresponding to `testData` directories, and the compiler test setup to use for each group.
**You really want your implementation to be an object.**
A no-arg class will also work.

Each test group has a level:

* `Diagnostics` - run the compiler up to the FIR diagnostic checkers and report diagnostics
* `FIR` - run the compiler through the FIR phases and dump the FIR.
* `IR` - run the compiler through the IR phases and dump the IR.
* `Run` - fully run the compiler, and run the resulting `box` method.

They are not cumulative: you could have a `Diagnostics + Run` test, that would not dump FIR or IR.

Each test group may also include compiler test configuration (on `TestConfigurationBuilder`), compiler configuration (on `CompilerConfiguration`), and generation configuration (e.g. generated test packages and annotations).

### Example

```kotlin
// prefer object over class - otherwise you'll reinstantiate the class every test run
object TestGenerator : BaseTestGenerator() {
    // sets the root package for generated tests
    override val testsRootPackage: String = "my.compiler.plugin.tests"

    // adds @OptIn annotations to any present `fun box()` method (via a preprocessor)
    override val boxOptIns: Set<String> = setOf(ExperimentalStdlibApi::class.qualifiedName!!)

    // adds @OptIn annotations to the test files (via a preprocessor)
    override val optInts: Set<String> = setOf(InternalMyPluginApi::class.qualifiedName!!)

    // adds imports to the test files (via a preprocessor)
    override val imports: Set<String> = setOf("my.compiler.plugin.runtime.*")

    override fun TestGenerationBuilder.generateTests() {
        // corresponds to the `/src/testData/generated` directory
        group("generated") { // this: TestGenerationBuilder
            // add additional opt-ins to the `box()` method for descendent tests
            optInBox(ExperimentalPathApi::class.qualifiedName!!)

            // enable the diagnostics level on descendent tests
            +TestLevel.Diagnostics

            // corresponds to the `/src/testData/generated/ir` directory
            // automatically adds the `IR` level due to the directory name
            group("ir") {

                // adds directives for descendent tests
                directives(MyDirectives) {
                    +MY_DIRECTIVE
                }

                // generate tests for this directory
                tests()
            }
        }
    }
}
```

with a corresponding directory structure of

```
src/testData/
    generated/
        ir/
            test.kt
```

Note that each file in testData corresponds to a single generated test method.

All methods on the `TestGenerationBuilder` apply to the current group and any descendents.
Generation of actual tests is triggered by the `tests()` method, which generates a test class with methods for every `.kt` file in the directory and subdirectories (by default).
`tests` accepts a `TestArguments` class with many arguments that are passed directly to the underlying Kotlin compiler test framework.

Adding groups with a name/directory matching a known test type will automatically add the levels for that type (not case sensitive):

* `diagnostics` - `Diagnostics`
* `fir` - `FIR`
* `ir` - `IR`
* `compile` - `Diagnostics + FIR + IR`
* `run` - `Run`
* `box` - `Diagnostics + FIR + IR + Run`

You can use a type explicitly by passing the `TestType` enum to the `group` call or using `+TestType.Diagnostics`, etc.

### Autogeneration

If you do not specify a `compilerPluginDevelopment.testGenerator` for Gradle, or your generator doesn't override `disableAutogeneration`, then the test generator will automatically generate test classes for tests under `testData/auto`.
Test type inference applies here - a test in `auto/ir` or `auto/mytests/ir` would be added using the IR test type.

### Plugin configuration

If you have configured Gradle with the `compilerPluginDevelopment.compilerPluginRegistrar` property, then your compiler plugin will automatically be added to the test configuration.

If you are using a `BaseSpecCompilerPluginRegistrar`, you can set the spec as part of the test generation DSL using `withTestSpec(MyRegistrar::class, mySpec)`

### How does it work?

How can we apply configuration like this, not in a test base class?
Well, when you use `BaseTestGenerator`, each generated test has its `configure` overridden with an implementation that calls `super`, but also calls an internal `configureTest` method on your `BaseTestGenerator` implementation.
This is why you want it to be an object (a no-arg class will still work, but will re-create it each time).
The `BaseTestGenerator` is capable of figuring out where in your test group DSL hierarchy the test came from and applying the appropriate configuration.

# Package dev.rnett.kcp.development.testing.configuration

Configuration for using a compiler plugin in the test framework.

# Package dev.rnett.kcp.development.testing.directives

Utility directives for things like imports.

# Package dev.rnett.kcp.development.testing.generation

The test generation DSL.

# Package dev.rnett.kcp.development.testing.preprocessors

Utility preprocessors for things like package declarations.

# Package dev.rnett.kcp.development.testing.tests

Test types, levels, and abstract base classes.