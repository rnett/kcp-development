package dev.rnett.kcp.development.testing.generation

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest

interface Configurator {
    fun TestConfigurationBuilder.configure(testInstance: AbstractKotlinCompilerTest) {

    }
}