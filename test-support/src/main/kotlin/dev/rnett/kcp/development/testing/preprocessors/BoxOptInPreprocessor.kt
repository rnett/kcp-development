package dev.rnett.kcp.development.testing.preprocessors

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.DefaultsDsl
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isKtFile

/**
 * Adds an `@OptIn` annotation for [optIns] classes to the `box` function, if it exists.
 */
class BoxOptInPreprocessor(val optIns: Set<String>, testServices: TestServices) : ReversibleSourceFilePreprocessor(testServices) {

    val optInString = "@OptIn([${optIns.joinToString(", ") { "$it::class" }}])\n"
    val boxFun = "fun box()"

    override fun revert(file: TestFile, actualContent: String): String {
        if (!file.isKtFile) return actualContent
        if(optIns.isEmpty()) return actualContent

        return actualContent.replaceFirst(optInString, "")
    }

    override fun process(file: TestFile, content: String): String {
        if (!file.isKtFile) return content
        if(optIns.isEmpty()) return content

        return content.replaceFirst(boxFun, optInString + boxFun)
    }
}

@DefaultsDsl
fun TestConfigurationBuilder.useBoxOptInPreprocessor(
    imports: Set<String>
) {
    useSourcePreprocessor(
        {
            BoxOptInPreprocessor(
                imports,
                it
            )
        }
    )
}

@DefaultsDsl
fun TestConfigurationBuilder.useBoxOptInPreprocessor(
    vararg imports: String
) {
    useBoxOptInPreprocessor(imports.toSet())
}