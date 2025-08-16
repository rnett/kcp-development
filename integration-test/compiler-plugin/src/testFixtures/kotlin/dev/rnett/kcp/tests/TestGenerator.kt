package dev.rnett.kcp.tests

import dev.rnett.kcp.development.testing.generation.BaseTestGenerator
import dev.rnett.kcp.development.testing.generation.TestGenerationBuilder
import dev.rnett.kcp.development.testing.generation.optInBox
import kotlin.io.path.ExperimentalPathApi
import kotlin.time.ExperimentalTime

class TestGenerator : BaseTestGenerator() {
    override val boxOptIns: Set<String> = setOf(ExperimentalStdlibApi::class.qualifiedName!!)

    override fun TestGenerationBuilder.generateTests() {
        optInBox(ExperimentalTime::class.qualifiedName!!)

        group("generated") {
            optInBox(ExperimentalPathApi::class.qualifiedName!!)

            group("ir") {
                tests()
            }
        }
    }
}