package dev.rnett.kcp.development.testing.configuration

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Repeatable
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(ConfigurationExtension::class)
annotation class ConfigureWith(val host: KClass<out ConfigurationHost>)

interface ConfigurationHost {
    fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder)
}

class ConfigurationExtension : TestInstancePostProcessor {
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        if (testInstance !is AbstractKotlinCompilerTest)
            return

        val hosts = testInstance::class.java.annotations.filterIsInstance<ConfigureWith>()
            .map {
                it.host.objectInstance ?: it.host.createInstance()
            }

        val configurationProp = testInstance::class.memberProperties.first { it.name == "configuration" } as KProperty1<AbstractKotlinCompilerTest, TestConfigurationBuilder.() -> Unit>
        configurationProp.isAccessible = true

        val oldValue = configurationProp.get(testInstance)

        val newValue: TestConfigurationBuilder.() -> Unit = {
            oldValue(this)
            hosts.forEach {
                it.configureTest(testInstance, this)
            }
        }

        configurationProp.set(testInstance, newValue)

    }
}