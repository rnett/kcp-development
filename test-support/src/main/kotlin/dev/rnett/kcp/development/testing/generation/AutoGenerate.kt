package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.TestType
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


fun TestGenerationBuilder.autoGenerateTests(skip: (Path) -> Boolean = { false }) {
    path.listDirectoryEntries().forEach {
        if (it.isDirectory()) {
            if (it.name in TestType.byDirectoryName) {
                group(it.name) {
                    +TestType.byDirectoryName.getValue(it.name)
                    generateThisTest()
                }
            } else {
                group(it.name) {
                    autoGenerateTests(skip)
                }
            }
        }
    }
}