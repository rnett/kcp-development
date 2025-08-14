package dev.rnett.kcp.development

plugins {
    java
    id("com.github.gmazzo.buildconfig")
}

setProperty("kotlin.stdlib.default.dependency", false)

val extension = extensions.create<CompilerGradleSupportPluginDevelopmentExtension>(CompilerGradleSupportPluginDevelopmentExtension.NAME).apply {
    compilerPluginProjectPath.convention(":compiler-plugin")
    compilerPluginProjectPath.finalizeValueOnRead()

    additionalLibraryProjectPaths.convention(emptyMap())
    additionalLibraryProjectPaths.finalizeValueOnRead()
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("gradle-plugin-api"))
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }
    packageName = provider { group.toString() }

    // must match the one in compiler-plugin-buildconfig.gradle.kts
    buildConfigField("String", "KOTLIN_PLUGIN_ID", provider { "\"${group}.${name}\"" })

    afterEvaluate {
        val pluginProject = project(extension.compilerPluginProjectPath.get())
        buildConfigField("String", "KOTLIN_PLUGIN_GROUP", provider { "\"${pluginProject.group}\"" })
        buildConfigField("String", "KOTLIN_PLUGIN_NAME", provider { "\"${pluginProject.name}\"" })
        buildConfigField("String", "KOTLIN_PLUGIN_VERSION", provider { "\"${pluginProject.version}\"" })



        extension.additionalLibraryProjectPaths.get().forEach { (name, project) ->
            val libraryProject = project(project)

            val libraryCoordinates = buildString {
                append(libraryProject.group)
                append(":")
                append(libraryProject.name)
                val versionString = libraryProject.version.toString().ifEmpty { null }?.takeUnless { it == "undefined" }
                if (versionString != null) {
                    append(":")
                    append(versionString)
                }
            }

            buildConfigField(
                type = "String",
                name = "${name.uppercase().replace("-", "_")}_LIBRARY_COORDINATES",
                expression = "\"${libraryCoordinates}\""
            )
        }
    }
}