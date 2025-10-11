package dev.rnett.kcp.development.testing.runtime

import dev.rnett.kcp.development.testing.generation.BaseTestGenerator
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

private val runtimeClasspath =
    System.getProperty("compilerTestRuntime.classpath")?.split(File.pathSeparator)?.map(::File)
        ?: error("Unable to get a valid classpath from 'compilerTestRuntime.classpath' property")

/**
 * A helper configurator that adds the classpath from the "compilerTestRuntime.classpath" system property to the compile classpath.
 * Autoconfigured by the Gradle plugin and automatically added by [BaseTestGenerator].
 */
public class RuntimeEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        for (file in runtimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }
}

/**
 * A helper configurator that adds the classpath from the "compilerTestRuntime.classpath" system property to the runtime classpath.
 * Autoconfigured by the Gradle plugin and automatically added by [BaseTestGenerator].
 */
public class RuntimeClassPathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> {
        return runtimeClasspath
    }
}

/**
 * Adds both [RuntimeEnvironmentConfigurator] and [RuntimeClassPathProvider] to the test services.
 * Autoconfigured by the Gradle plugin and automatically added by [BaseTestGenerator].
 */
public fun TestConfigurationBuilder.useTestRuntime() {
    useConfigurators(::RuntimeEnvironmentConfigurator)
    useCustomRuntimeClasspathProviders(::RuntimeClassPathProvider)
}