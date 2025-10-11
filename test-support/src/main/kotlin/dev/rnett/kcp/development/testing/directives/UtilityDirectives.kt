package dev.rnett.kcp.development.testing.directives

import dev.rnett.kcp.development.testing.directives.preprocessors.useBoxOptInPreprocessor
import dev.rnett.kcp.development.testing.directives.preprocessors.useImportsPreprocessor
import dev.rnett.kcp.development.testing.generation.BaseTestGenerator
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.StringDirective

/**
 * See [useImportsPreprocessor] and [useBoxOptInPreprocessor].
 * Configured automatically based on the test generation setup when using [BaseTestGenerator].
 */
public object UtilityDirectives : SimpleDirectivesContainer() {
    public val IMPORTS: StringDirective by stringDirective(
        description = "A list of automatically added imports."
    )
    public val BOX_OPT_IN: StringDirective by stringDirective(
        description = "A list of opt-in annotations to add to the box function."
    )
}