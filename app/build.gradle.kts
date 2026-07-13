plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Version of the *published* library the `published` flavour builds against.
 *
 * It defaults to this repo's own VERSION_NAME, which is what makes the flavour
 * useful in both directions:
 *  - before a release, `publishToMavenLocal` puts exactly this version into
 *    mavenLocal, so the flavour gates the artifact we are about to ship;
 *  - after a release, the identical coordinate resolves from JitPack, so the
 *    APK we attach to the GitHub Release is itself proof the publish worked.
 *
 * Override with `-PmushafVersion=<v>` to build against any released version.
 */
val mushafVersion: String =
    (findProperty("mushafVersion") ?: property("VERSION_NAME")).toString()

android {
    namespace = "com.mushafimad.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mushafimad.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        // The app carries the library's version: an APK is only ever a snapshot of
        // one library version, and the user must be able to tell which.
        versionCode = property("VERSION_CODE").toString().toInt()
        versionName = mushafVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // `source` and `published` build the *same* app code against two different
    // spellings of the same library, so any gap between "works in the repo" and
    // "works for a consumer" shows up as a build or test failure rather than as a
    // bug report. This is the pair that would have caught the missing WAKE_LOCK.
    flavorDimensions += "library"
    productFlavors {
        create("source") {
            dimension = "library"
            // Distinct applicationId so both flavours can be installed side by side
            // and compared on one device.
            applicationIdSuffix = ".source"
            versionNameSuffix = "-source"
        }
        create("published") {
            dimension = "library"
            // No suffix: this is the shippable app, and its APK is what gets
            // attached to the GitHub Release.
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            // Minified: this is what proves the library ships working *consumer*
            // ProGuard rules in its AAR. Realm, Koin and Media3 are all reflective.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Debug-signed so the release APK can be installed and driven without a
            // keystore. This app is a demo, not a Play Store listing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // So the UI can show which library version it is running against rather
        // than restating it in a string literal that goes stale on the next
        // release - the pipeline auto-bumps the patch, so a hardcoded one lies
        // immediately.
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

/*
 * AppManifestPermissionsTest reads the checked-in AndroidManifest.xml at runtime to
 * assert this app declares no library-owned permission. Gradle cannot see that
 * dependency: the manifest is not an input to the unit-test task, so changing it
 * leaves the task UP-TO-DATE and the test is skipped - the guard would silently pass
 * for exactly the edit it exists to catch. Declaring it as an input fixes that, and
 * passing the absolute path keeps the test independent of the working directory.
 */
tasks.withType<Test>().configureEach {
    val manifest = layout.projectDirectory.file("src/main/AndroidManifest.xml")
    inputs.file(manifest).withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty("app.manifest.path", manifest.asFile.absolutePath)
}

dependencies {
    // The library, two ways. Nothing else in this file may differ between the
    // flavours - the whole point is that the app code cannot tell them apart.
    "sourceImplementation"(project(":mushaf-ui"))
    "publishedImplementation"("com.github.YahiaRagae.mushaf-imad-android:mushaf-ui:$mushafVersion")

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
