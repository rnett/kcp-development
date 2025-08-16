package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.registrar.BaseSpecCompilerPluginRegistrar
import dev.rnett.kcp.development.testing.configuration.TestSpecProducer
import dev.rnett.kcp.development.testing.configuration.withCompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@OptIn(ExperimentalCompilerApi::class)
sealed interface CompilerPluginSpec {
    fun configure(builder: TestConfigurationBuilder)
    data class Basic<R : CompilerPluginRegistrar>(val cls: KClass<out R>) : CompilerPluginSpec {
        override fun configure(builder: TestConfigurationBuilder) {
            builder.withCompilerPluginRegistrar(cls.createInstance())
        }
    }

    data class WithTestSpec<T>(val cls: KClass<out BaseSpecCompilerPluginRegistrar<T>>, val testSpec: TestSpecProducer<T>) : CompilerPluginSpec {
        override fun configure(builder: TestConfigurationBuilder) {
            builder.withCompilerPluginRegistrar(cls.createInstance(), testSpec)
        }
    }

    companion object {
        fun <R : CompilerPluginRegistrar> basic(cls: KClass<out R>) = Basic(cls)
        inline fun <reified T : CompilerPluginRegistrar> basic() = Basic(T::class)
        fun <T> withTestSpec(cls: KClass<out BaseSpecCompilerPluginRegistrar<T>>, testSpec: TestSpecProducer<T>) = WithTestSpec(cls, testSpec)
        inline fun <reified R : BaseSpecCompilerPluginRegistrar<T>, T> withTestSpec(testSpec: TestSpecProducer<T>) = WithTestSpec(R::class, testSpec)

        fun <R : BaseSpecCompilerPluginRegistrar<T>, T> Basic<R>.withTestSpec(testSpecProducer: TestSpecProducer<T>) = WithTestSpec(cls, testSpecProducer)
    }
}

