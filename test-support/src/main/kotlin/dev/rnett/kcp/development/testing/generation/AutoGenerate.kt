package dev.rnett.kcp.development.testing.generation

import dev.rnett.kcp.development.testing.tests.TestType
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.relativeTo
import kotlin.io.path.visitFileTree


fun TestGroupGeneration.autoGenerateTests(relativeRootPath: String? = "auto", skip: (Path) -> Boolean = { false }) {
    val root = Path(internal.testDataRoot)
    val relativeRoot = relativeRootPath?.let { root.resolve(it) } ?: root
    relativeRoot.visitFileTree {
        onPreVisitDirectory { it, attr ->
            if (skip(it))
                return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE

            if (attr.isDirectory && it.name in TestType.byDirectoryName) {
                val type = TestType.byDirectoryName.getValue(it.name)
                testClassForDirectory(it.relativeTo(Path(internal.testDataRoot)).toString(), type)
                return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
            }

            FileVisitResult.CONTINUE
        }
    }
}