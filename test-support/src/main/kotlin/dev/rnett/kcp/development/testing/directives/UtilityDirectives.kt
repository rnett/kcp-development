package dev.rnett.kcp.development.testing.directives

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object UtilityDirectives : SimpleDirectivesContainer() {
    val IMPORTS by stringDirective(
        description = "A list of automatically added imports."
    )
    val BOX_OPT_IN by stringDirective(
        description = "A list of opt-in annotations to add to the box function."
    )
}