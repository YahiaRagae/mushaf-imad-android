// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.realm) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.binary.compatibility.validator)
}

// Public API contract: mushaf-core and mushaf-ui .api files are the frozen
// surface consumers depend on. Run `apiDump` to regenerate after an
// intentional (additive-only) change; CI runs `apiCheck`.
apiValidation {
    ignoredProjects += listOf("sample")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
