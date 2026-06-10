package build

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

extensionIfPresent<KotlinJvmExtension>() {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}
tasks.named("check") {
    dependsOn("checkLegacyAbi")
}
