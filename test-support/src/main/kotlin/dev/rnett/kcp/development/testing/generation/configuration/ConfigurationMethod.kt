package dev.rnett.kcp.development.testing.generation.configuration

import org.jetbrains.kotlin.generators.MethodGenerator
import org.jetbrains.kotlin.generators.model.MethodModel
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.utils.Printer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

public abstract class ConfigurationHost {
    public abstract fun configureTest(testInstance: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder)

    public companion object {

        @JvmStatic
        public fun applyRuntimeConfiguration(test: AbstractKotlinCompilerTest, builder: TestConfigurationBuilder, vararg hosts: Class<out ConfigurationHost>) {
            val instances = hosts.toSet().map {
                (it.kotlin.objectInstance ?: it.kotlin.createInstance()) as ConfigurationHost
            }
            instances.forEach {
                it.configureTest(test, builder)
            }
        }
    }
}

public class RuntimeConfigurationMethodModel(public val hosts: Set<KClass<out ConfigurationHost>>) : MethodModel<RuntimeConfigurationMethodModel>() {
    override val generator: MethodGenerator<RuntimeConfigurationMethodModel> = Generator
    override val name: String = "configuration"
    override val dataString: String? = null
    override val tags: List<String> = emptyList()

    init {
        require(hosts.all { it.java.canonicalName != null }) { "ConfigurationHost classes must have a canonical name" }
    }

    override fun imports(): Collection<Class<*>> {
        return super.imports() + hosts.map { it.java } + ConfigurationHost::class.java + TestConfigurationBuilder::class.java
    }

    override val isTestMethod: Boolean = false

    override val shouldBeGeneratedForInnerTestClass: Boolean = false

    public fun shouldBeGenerated(): Boolean = hosts.isNotEmpty()

    public object Generator : MethodGenerator<RuntimeConfigurationMethodModel>() {

        override fun generateSignature(method: RuntimeConfigurationMethodModel, p: Printer) {
            p.println("@Override")
            p.print("public void configure(TestConfigurationBuilder builder)")
        }

        override fun generateBody(method: RuntimeConfigurationMethodModel, p: Printer) {
            p.println("super.configure(builder);")
            p.println("ConfigurationHost.applyRuntimeConfiguration(this, builder, ${method.hosts.joinToString { it.java.simpleName + ".class" }});")
        }
    }
}