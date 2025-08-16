package dev.rnett.kcp.development.testing.generation

import auto.test.box.dev.rnett.kcp.development.testing.SysProps
import dev.rnett.kcp.development.testing.tests.TestType
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


fun TestGenerationBuilder.autoGenerateTests(skip: (Path) -> Boolean = { false }) {
    if (!path.exists() || !path.isDirectory()) {
        return
    }

    path.listDirectoryEntries().forEach {
        if (it.isDirectory()) {
            if (it.name in TestType.byDirectoryName) {
                group(it.name) {
                    tests()
                }
            } else {
                group(it.name) {
                    autoGenerateTests(skip)
                }
            }
        }
    }
}

object AutoGenerator : BaseTestGenerator() {
    override val testsRootPackage: String = SysProps.defaultPackage
}