package dev.rnett.kcp.development.testing.configuration

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

//TODO use a Junit5 extension instead to be able to look up configurators dynamically

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestConfigurator(val configurator: KClass<out Configurator>)

abstract class Configurator {
    abstract fun TestConfigurationBuilder.configure()
}

fun AbstractKotlinCompilerTest.applyDynamicConfigurators(builder: TestConfigurationBuilder) {
    applyDynamicConfigurators(this, builder)
}

internal fun applyDynamicConfigurators(testInstance: Any, builder: TestConfigurationBuilder) {
    val configurators = testInstance::class.java.annotations.filterIsInstance<TestConfigurator>().map {
        it.configurator.let { it.objectInstance ?: it.createInstance() } as Configurator
    }
    configurators.distinct().forEach {
        with(it) { builder.configure() }
    }
}