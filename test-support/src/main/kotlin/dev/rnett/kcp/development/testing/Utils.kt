package dev.rnett.kcp.development.testing

import org.jetbrains.kotlin.test.directives.model.ComposedRegisteredDirectives
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.defaultDirectives
import org.jetbrains.kotlin.test.services.moduleStructure

public fun TestServices.allDirectives(file: TestFile): RegisteredDirectives = ComposedRegisteredDirectives(
    buildList {
        add(defaultDirectives)
        moduleStructure.modules.filter { file in it.files }.forEach {
            add(it.directives)
        }
        add(file.directives)
    }
)