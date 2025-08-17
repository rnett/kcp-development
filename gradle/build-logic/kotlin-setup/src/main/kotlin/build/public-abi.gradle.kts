package build

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

extensionIfPresent<KotlinJvmExtension>() {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    extensionIfPresent<AbiValidationExtension> {
        enabled = true
    }
}
tasks.named("check") {
    dependsOn("checkLegacyAbi")
}