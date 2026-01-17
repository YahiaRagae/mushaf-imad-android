# Mushaf Imad - Modularization Migration Plan

## Version 1.0.0 - Initial Release

### Module Structure

```
android/
├── library/              # Monolithic library
│   ├── data/
│   ├── domain/
│   ├── di/
│   └── ui/
└── sample/
```

### Published Artifacts

```
com.mushafimad:library:1.0.0
```

### Consumer Usage

```kotlin
dependencies {
    implementation("com.mushafimad:library:1.0.0")
}
```

---

## Version 2.0.0 - Modular Architecture (Non-Breaking)

### Module Structure

```
android/
├── mushaf-domain/        # Pure domain models, repository interfaces
│   ├── models/
│   └── repository/
│
├── mushaf-audio/         # Audio services and reciters
│   ├── AudioPlayerService
│   ├── ReciterService
│   └── AyahTimingService
│
├── mushaf-data/          # Data layer implementation
│   ├── local/            # Realm database
│   ├── repository/       # Repository implementations
│   ├── cache/
│   └── providers/
│
├── mushaf-core/          # Facade module (aggregator)
│   └── Re-exports: domain, audio, data
│
├── mushaf-ui/            # UI components
│   ├── mushaf/
│   ├── player/
│   ├── search/
│   └── theme/
│
└── sample/
```

### Published Artifacts

```
com.mushafimad:mushaf-domain:2.0.0
com.mushafimad:mushaf-audio:2.0.0
com.mushafimad:mushaf-data:2.0.0
com.mushafimad:mushaf-core:2.0.0     (facade - includes all above)
com.mushafimad:mushaf-ui:2.0.0
```

### Consumer Usage

#### Option 1: No Changes Required (Recommended for Most Users)

```kotlin
dependencies {
    // Existing users - no changes needed
    implementation("com.mushafimad:mushaf-core:2.0.0")

    // Or use UI module (includes core transitively)
    implementation("com.mushafimad:mushaf-ui:2.0.0")
}
```

**Result:** Gets all functionality exactly as before.

#### Option 2: Granular Dependencies (Advanced Users)

```kotlin
dependencies {
    // Use case: Custom UI with domain models only
    implementation("com.mushafimad:mushaf-domain:2.0.0")
}
```

```kotlin
dependencies {
    // Use case: Audio-only app
    implementation("com.mushafimad:mushaf-domain:2.0.0")
    implementation("com.mushafimad:mushaf-audio:2.0.0")
}
```

```kotlin
dependencies {
    // Use case: Data layer + custom audio implementation
    implementation("com.mushafimad:mushaf-domain:2.0.0")
    implementation("com.mushafimad:mushaf-data:2.0.0")
}
```

---

## Migration Steps (Internal Implementation)

### Step 1: Create Sub-Modules

Create new module directories:
- `mushaf-domain/`
- `mushaf-audio/`
- `mushaf-data/`

### Step 2: Move Code

**mushaf-domain:**
- `library/domain/models/` → `mushaf-domain/src/main/java/com/mushafimad/core/domain/models/`
- `library/domain/repository/` → `mushaf-domain/src/main/java/com/mushafimad/core/domain/repository/`

**mushaf-audio:**
- `library/data/audio/` → `mushaf-audio/src/main/java/com/mushafimad/core/data/audio/`

**mushaf-data:**
- `library/data/local/` → `mushaf-data/src/main/java/com/mushafimad/core/data/local/`
- `library/data/repository/` → `mushaf-data/src/main/java/com/mushafimad/core/data/repository/`
- `library/data/cache/` → `mushaf-data/src/main/java/com/mushafimad/core/data/cache/`
- `library/data/providers/` → `mushaf-data/src/main/java/com/mushafimad/core/data/providers/`

**mushaf-ui:**
- `library/ui/` → `mushaf-ui/src/main/java/com/mushafimad/ui/`

### Step 3: Configure Dependencies

**mushaf-domain/build.gradle.kts:**
```kotlin
// No dependencies (pure Kotlin/Android)
```

**mushaf-audio/build.gradle.kts:**
```kotlin
dependencies {
    api(project(":mushaf-domain"))
    implementation(libs.media3.exoplayer)
    implementation(libs.hilt.android)
}
```

**mushaf-data/build.gradle.kts:**
```kotlin
dependencies {
    api(project(":mushaf-domain"))
    implementation(libs.realm.kotlin)
    implementation(libs.coil)
    implementation(libs.hilt.android)
}
```

**mushaf-core/build.gradle.kts:**
```kotlin
dependencies {
    // Re-export all sub-modules
    api(project(":mushaf-domain"))
    api(project(":mushaf-audio"))
    api(project(":mushaf-data"))
}
```

**mushaf-ui/build.gradle.kts:**
```kotlin
dependencies {
    api(project(":mushaf-core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.hilt.navigation.compose)
}
```

### Step 4: Update settings.gradle.kts

```kotlin
include(":mushaf-domain")
include(":mushaf-audio")
include(":mushaf-data")
include(":mushaf-core")
include(":mushaf-ui")
include(":sample")
```

### Step 5: Update Package Names

**Important:** Keep existing package structure:
```
com.mushafimad.core.domain.*
com.mushafimad.core.data.*
com.mushafimad.ui.*
```

**Do NOT change** to:
```
com.mushafimad.domain.*    ❌ Breaking change
com.mushafimad.audio.*     ❌ Breaking change
```

### Step 6: Update Sample App

```kotlin
dependencies {
    // No changes needed - uses mushaf-ui as before
    implementation(project(":mushaf-ui"))
}
```

### Step 7: Publishing

Configure publishing for all modules:
- `mushaf-domain:2.0.0`
- `mushaf-audio:2.0.0`
- `mushaf-data:2.0.0`
- `mushaf-core:2.0.0`
- `mushaf-ui:2.0.0`

---

## Version 3.0.0+ - Optional Facade Deprecation

### Optional: Deprecate mushaf-core Facade

After 1-2 years, optionally deprecate the facade module:

```kotlin
// mushaf-core/build.gradle.kts
@Deprecated(
    message = "mushaf-core is deprecated. Use mushaf-domain, mushaf-audio, mushaf-data directly.",
    level = DeprecationLevel.WARNING
)
```

### Version 4.0.0 - Remove Facade (Breaking)

Remove `mushaf-core` entirely. Users must migrate to granular dependencies.

**Migration:**
```kotlin
// Before (v3.x)
implementation("com.mushafimad:mushaf-core:3.0.0")

// After (v4.x)
implementation("com.mushafimad:mushaf-domain:4.0.0")
implementation("com.mushafimad:mushaf-audio:4.0.0")
implementation("com.mushafimad:mushaf-data:4.0.0")

// Or just use UI module
implementation("com.mushafimad:mushaf-ui:4.0.0")
```

---

## Summary

| Version | Breaking Change | mushaf-core Status | User Action Required |
|---------|-----------------|-------------------|---------------------|
| 1.0.0 | N/A | Monolithic library | N/A |
| 2.0.0 | ❌ No | Facade (re-exports sub-modules) | None - optional granular deps |
| 3.0.0 | ❌ No | Deprecated (still works) | Optional migration |
| 4.0.0 | ✅ Yes | Removed | Must use granular deps or mushaf-ui |

---

**Last Updated:** January 17, 2026
