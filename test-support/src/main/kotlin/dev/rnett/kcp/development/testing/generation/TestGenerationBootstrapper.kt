package dev.rnett.kcp.development.testing.generation

import kotlin.reflect.full.createInstance

// hard coded in gradle plugin
object TestGenerationBootstrapper {
    @JvmStatic
    fun main(args: Array<String>) {
        val generator = args.getOrNull(0)
        if (generator != null) {
            val cls = Class.forName(generator)
            if (BaseTestGenerator::class.java.isAssignableFrom(cls)) {
                val instance = (cls.kotlin.objectInstance ?: cls.kotlin.createInstance()) as BaseTestGenerator
                instance.generateSuite()
            } else
                error("Class $generator is not a subtype of dev.rnett.kcp.development.testing.generation.TestGenerator")
        } else {
            AutoGenerator.generateSuite()
        }
    }
}