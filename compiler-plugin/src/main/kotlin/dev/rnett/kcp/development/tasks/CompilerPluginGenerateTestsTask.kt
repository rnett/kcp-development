package dev.rnett.kcp.development.tasks

import dev.rnett.kcp.development.CompilerPluginDevelopmentExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class CompilerPluginGenerateTestsTask @Inject constructor(
    private val providers: ProviderFactory,
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputDirectory
    abstract val testDataDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedTestsDirectory: DirectoryProperty

    @get:Input
    abstract val testGenerator: Property<String>

    @get:Input
    abstract val useTestGenerator: Property<Boolean>

    @get:Input
    abstract val defaultPackage: Property<String>

    @get:InputFiles
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:Input
    abstract val workingDirectory: Property<File>

    @get:Nested
    abstract val launcher: Property<JavaLauncher>

    @get:Input
    @get:Optional
    abstract val compilerPluginRegistrar: Property<String>

    @TaskAction
    fun generateTests() {
        execOperations.javaexec {
            executable = launcher.get().executablePath.asFile.absolutePath
            classpath(this@CompilerPluginGenerateTestsTask.classpath)
            workingDir(workingDir)

            systemProperty("default.package", defaultPackage.get())
            systemProperty("kcp.dev.test-gen", generatedTestsDirectory.get().asFile.relativeTo(workingDir).path)
            systemProperty("kcp.dev.test-data", testDataDirectory.get().asFile.relativeTo(workingDir).path)

            compilerPluginRegistrar.orNull?.let {
                systemProperty("kcp.dev.plugin-registrar", it)
            }

            if (useTestGenerator.get()) {
                mainClass.set(CompilerPluginDevelopmentExtension.Companion.TEST_GENERATOR_MAIN)
                args(testGenerator.get())
            } else {
                mainClass.set(testGenerator.get())
            }

//            standardOutput = System.out
//            errorOutput = System.err

        }.apply {
            rethrowFailure()
            assertNormalExitValue()
        }
    }
}