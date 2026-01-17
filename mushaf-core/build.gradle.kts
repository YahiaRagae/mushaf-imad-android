plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.realm)
}

android {
    namespace = "com.mushafimad.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Version info for v1.0.0
        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")
        buildConfigField("int", "VERSION_CODE", "1")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        buildConfig = true
        compose = false  // CRITICAL: No Compose in core module
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // AndroidX Core (NO Compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Lifecycle (ViewModel may be used in core for future features)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Realm (Database)
    implementation(libs.realm.library.base)

    // Media3 (Audio Playback)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.common)

    // JSON Parsing
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    // DataStore (Preferences)
    implementation(libs.androidx.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
