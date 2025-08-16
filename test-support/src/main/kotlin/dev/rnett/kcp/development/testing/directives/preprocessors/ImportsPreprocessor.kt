package dev.rnett.kcp.development.testing.directives.preprocessors

import dev.rnett.kcp.development.testing.allDirectives
import dev.rnett.kcp.development.testing.directives.UtilityDirectives
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.DefaultsDsl
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isKtFile

class ImportsPreprocessor(testServices: TestServices) : ReversibleSourceFilePreprocessor(testServices) {
    fun importsString(imports: Set<String>) = "\n" + imports.joinToString("\n") { "import $it" } + "\n"

    fun calculateImports(file: TestFile): Set<String> {
        return testServices.allDirectives(file)[UtilityDirectives.IMPORTS].toSet()
    }

    override fun revert(file: TestFile, actualContent: String): String {
        if (!file.isKtFile || file.isAdditional) return actualContent
        val imports = calculateImports(file)

        if (imports.isEmpty()) return actualContent

        return actualContent.replaceFirst(importsString(imports), "")
    }

    override fun process(file: TestFile, content: String): String {
        if (!file.isKtFile || file.isAdditional) return content

        val imports = calculateImports(file)

        if (imports.isEmpty()) return content

        return if (content.lines().any { it.startsWith("package ") }) {
            buildString {
                content.lines().forEach {
                    appendLine(it)
                    if (it.trim().startsWith("package ")) {
                        append(importsString(imports))
                    }
                }
            }
        } else {
            importsString(imports) + content
        }
    }
}

/**
 * Adds imports based on the [UtilityDirectives.IMPORTS] directive.
 */
@DefaultsDsl
fun TestConfigurationBuilder.useImportsPreprocessor(
) {
    useSourcePreprocessor(
        ::ImportsPreprocessor
    )
    useDirectives(UtilityDirectives)
}