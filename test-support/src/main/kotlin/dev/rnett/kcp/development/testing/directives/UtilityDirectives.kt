package dev.rnett.kcp.development.testing.directives

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.directives.model.StringDirective

public object UtilityDirectives : SimpleDirectivesContainer() {
    public val IMPORTS: StringDirective by stringDirective(
        description = "A list of automatically added imports."
    )
    public val BOX_OPT_IN: StringDirective by stringDirective(
        description = "A list of opt-in annotations to add to the box function."
    )
}