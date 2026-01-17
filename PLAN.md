# Mushaf Imad - Development Plan & Status

## 📊 Progress Overview

**Current Status:** Core Features Complete (6/8 phases) | 🔴 Critical: Background Audio Missing

### Completed Phases
- [x] Phase 1: Foundation ✅
- [x] Phase 2: Image-Based Mushaf View ✅
- [x] Phase 3: Audio Player ✅
- [x] Phase 4: Audio Player Integration ✅
- [x] Phase 5: Search Functionality ✅
- [x] Phase 6: Sample App Restructuring ✅

### In Progress / Planned
- [ ] Phase 7: Background Audio Playback 🔴 **CRITICAL - NEXT**
- [ ] Phase 8: Library Modularization (after Phase 7)

### Quick Stats
- **Lines of Code:** ~15,000+ (Android)
- **Completion:** 75% (6/8 phases complete)
- **Critical Blockers:** Background audio playback
- **Time to Production:** 1-2 weeks (after Phase 7)

---

## Project Overview

Mushaf Imad is a cross-platform Quran reader library providing high-quality Mushaf page display with audio recitation support. The library uses image-based rendering for authentic Mushaf appearance and includes synchronized verse highlighting during audio playback.

**Platforms:**
- iOS (Swift/SwiftUI) - ✅ Complete (v1.0.4)
- Android (Kotlin/Jetpack Compose) - 🔴 Missing background audio

---

## Completed Work

### Phase 1: Foundation ✅
- [x] Project structure setup
- [x] Dependency injection with Hilt
- [x] Data models (Verse, Chapter, MushafType, etc.)
- [x] Local data providers for Quran metadata
- [x] Repository pattern implementation

### Phase 2: Image-Based Mushaf View ✅
- [x] **Image Loading System**
  - [x] Line-by-line image rendering from assets
  - [x] Efficient caching and loading strategies
  - [x] Support for different Mushaf types (HAFS_1441, etc.)

- [x] **Verse Interaction**
  - [x] Tap detection on verses
  - [x] Verse highlighting (selected + audio playback)
  - [x] Verse position mapping from JSON metadata

- [x] **Fasel (Verse Markers)**
  - [x] PNG-based fasel decoration rendering
  - [x] Proper positioning over verse endings
  - [x] Support for different fasel styles

- [x] **Navigation**
  - [x] Page-by-page navigation (604 pages)
  - [x] Swipe gestures for page turning
  - [x] Chapter/Juz information display
  - [x] Reading position persistence

- [x] **Theming**
  - [x] Multiple reading themes (Comfortable, Sepia, Night, etc.)
  - [x] Color scheme customization
  - [x] RTL (Right-to-Left) layout support
  - [x] Material 3 Design integration

### Phase 3: Audio Player ✅
- [x] **Audio Infrastructure**
  - [x] ExoPlayer (Media3) integration
  - [x] 18 reciters with different recitation styles (Rewaya)
  - [x] Verse timing synchronization (AyahTimingService)
  - [x] Network audio streaming

- [x] **Player Features**
  - [x] Play/Pause/Stop controls
  - [x] Verse navigation (next/previous)
  - [x] Playback speed control (0.5x - 2.0x)
  - [x] Repeat mode
  - [x] Progress bar with seek support
  - [x] Real-time verse highlighting during playback

- [x] **Reciter Management**
  - [x] ReciterService with SharedPreferences persistence
  - [x] Reciter selection dialog
  - [x] Support for multiple recitation styles (Hafs, Warsh, etc.)
  - [x] Audio URL generation for each reciter

- [x] **UI Components**
  - [x] QuranPlayerView (Compose UI matching iOS design)
  - [x] ReciterPickerDialog
  - [x] MushafWithPlayerView (integrated Mushaf + audio player)
  - [x] Playback state indicators

- [x] **Audio Configuration**
  - [x] Proper audio focus handling
  - [x] Wake lock for network streaming
  - [x] Audio becoming noisy handling (headphone disconnect)
  - [x] Speech content type optimization

### Phase 4: Audio Player Integration ✅
- [x] MainActivity with MushafWithPlayerView integration
- [x] Audio player toggle functionality
- [x] Settings bottom sheet (theme, color scheme, Mushaf type)
- [x] Internet permissions for audio streaming
- [x] Initial sample app with basic features

### Phase 5: Search Functionality ✅
- [x] **Search Infrastructure**
  - [x] SearchViewModel with comprehensive state management
  - [x] Search history tracking and suggestions
  - [x] Debounced search (300ms) for better performance
  - [x] Support for multiple search types (All, Verses, Chapters)

- [x] **Search UI**
  - [x] SearchView with Material 3 design
  - [x] Search bar with filters
  - [x] Verse and chapter result items
  - [x] Empty states and error handling
  - [x] Search history display

- [x] **Search Integration**
  - [x] Navigation to verse pages from search results
  - [x] Chapter navigation from search
  - [x] Proper back stack management

- [x] **Data Layer**
  - [x] `searchableText` fields in Verse and Chapter models
  - [x] `VerseRepository.searchVerses()` implementation
  - [x] `ChapterRepository.searchChapters()` implementation
  - [x] Case-insensitive search support

### Phase 6: Sample App Restructuring ✅
- [x] **Navigation Implementation**
  - [x] Using Navigation Compose 2.8.5
  - [x] Proper navigation stack with back button support
  - [x] String-based navigation routes (sealed class pattern)
  - [x] Screen-level composables
  - [x] Note: Using Navigation Compose 2.8.5 but NOT using type-safe navigation features

- [x] **Category-Based Architecture** (Matching iOS)
  - [x] **Quick Start**: Chapters List, Read the Mushaf
  - [x] **Features**: Search, Theme Customization
  - [x] **Audio**: Mushaf with Audio, Audio Player UI
  - [x] **Helpful Links**: View on GitHub

- [x] **Enhanced UX**
  - [x] Home screen with categorized demos
  - [x] Proper navigation flow (push, not replace)
  - [x] Dedicated screens for each feature
  - [x] Material 3 design throughout

- [x] **UI Components**
  - [x] VerseFasel previews (multiple scales and themes)
  - [x] Improved list items with icons
  - [x] Section headers
  - [x] Link items for external resources

---

## Phase 7: Background Audio Playback (🔴 Critical - Planned)

### Overview

**Goal:** Enable proper background audio playback with system controls and lock screen integration

**Status:** CRITICAL - Current implementation only works in foreground. Audio stops when screen turns off or app is backgrounded.

**Estimated Time:** 1-2 weeks

### Current Issues

**What Works:**
- ✅ Foreground playback with ExoPlayer
- ✅ Automatic audio focus handling
- ✅ Playback speed and repeat mode
- ✅ Comprehensive state management

**Critical Gaps:**
- ❌ No background playback (audio stops when screen off)
- ❌ No lock screen controls
- ❌ No notification playback controls
- ❌ No Bluetooth headset support
- ❌ Missing foreground service implementation
- ❌ Missing MediaSession integration
- ❌ Missing required permissions (FOREGROUND_SERVICE_MEDIA_PLAYBACK)

### Implementation Tasks

#### Task 7.1: Add MediaSession Dependencies
- [ ] Add `media3-session` to `gradle/libs.versions.toml`
- [ ] Add implementation dependency to `library/build.gradle.kts`

#### Task 7.2: Create MediaSessionService
- [ ] Create `AudioPlaybackService.kt` extending `MediaSessionService`
- [ ] Implement ExoPlayer integration
- [ ] Implement MediaSession lifecycle management
- [ ] Add custom command handling (chapter selection, reciter change)
- [ ] Implement automatic notification generation
- [ ] Add proper resource cleanup in `onDestroy()`

#### Task 7.3: Update Library Manifest
- [ ] Add `FOREGROUND_SERVICE` permission
- [ ] Add `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permission
- [ ] Add `POST_NOTIFICATIONS` permission
- [ ] Declare service with `foregroundServiceType="mediaPlayback"`
- [ ] Add MediaSessionService intent filter

#### Task 7.4: Refactor AudioPlayerService
- [ ] Choose refactoring approach (Option A: Facade or Option B: Merge)
- [ ] Implement MediaController communication (if Option A)
- [ ] Update dependency injection
- [ ] Test existing functionality still works

#### Task 7.5: Handle Notification Permissions (Android 13+)
- [ ] Add runtime permission request in sample app
- [ ] Handle permission denial gracefully
- [ ] Test on Android 13+ devices

#### Task 7.6: Testing
- [ ] Test: Audio continues with screen off
- [ ] Test: Lock screen controls visible and functional
- [ ] Test: Notification controls work (play/pause/stop)
- [ ] Test: Bluetooth headset controls work
- [ ] Test: App backgrounded → audio continues
- [ ] Test: App killed by system → service stops gracefully
- [ ] Test: Android 8.0 (API 26)
- [ ] Test: Android 10 (API 29)
- [ ] Test: Android 13 (API 33)
- [ ] Test: Android 14+ (API 34+)

### Success Criteria

- [ ] Audio plays with screen off
- [ ] Lock screen shows playback controls
- [ ] Notification controls work correctly
- [ ] Bluetooth headset controls functional
- [ ] No crashes on Android 10+
- [ ] Service properly declared with mediaPlayback type
- [ ] All required permissions added
- [ ] Tests pass on multiple Android versions

### References

**Based on Android best practices (2026):**
- Background playback requires MediaSessionService as foreground service
- Notifications are automatic with Media3
- Must declare `foregroundServiceType="mediaPlayback"`
- Service auto-stops after 10 minutes of inactivity

---

## Phase 8: Library Modularization (🔄 Planned)

### Overview

**Goal:** Transform the monolithic library into a modular architecture with two distinct artifacts:
1. **mushaf-core** - Headless data layer for custom UI implementations
2. **mushaf-ui** - Ready-to-use Compose UI components

**Inspiration:** Based on architectural analysis of [quran_android](https://github.com/quran/quran_android) (28 modules, 7+ years mature)

**📄 Detailed Migration Plan:** See [MODULARIZATION_MIGRATION.md](android/MODULARIZATION_MIGRATION.md) for version-by-version migration strategy (v1.0.0 → v2.0.0 non-breaking)

**Note:** This phase should be implemented after Phase 7 (Background Audio Playback) is complete

**Benefits:**
- ✅ Developers can use core without UI (custom implementations)
- ✅ Smaller dependency footprint for core-only users
- ✅ Clear separation of concerns
- ✅ Independent versioning (if needed)
- ✅ Better testability
- ✅ Easier maintenance

---

### Current Architecture (Monolithic)

```
library/
├── src/main/java/com/mushafimad/library/
│   ├── data/              # Data layer (local, remote, cache)
│   ├── domain/            # Domain models and repositories
│   ├── di/                # Dependency injection
│   └── ui/                # UI components (Compose)
└── build.gradle.kts
```

**Single Artifact:** `com.mushafimad:library:1.0.0`

**Problem:**
- UI-only users must include all data layer code
- Data-only users must include Compose dependencies
- No clear API boundaries
- Single version for all features

---

### Target Architecture (Modular)

```
android/
├── mushaf-core/              # Core library module
│   ├── src/main/java/com/mushafimad/core/
│   │   ├── data/              # Data providers, cache, networking
│   │   │   ├── audio/         # Audio services (AudioPlayerService, ReciterService)
│   │   │   ├── local/         # Realm database, DAOs
│   │   │   ├── repository/    # Repository implementations
│   │   │   ├── cache/         # Image and data caching
│   │   │   └── providers/     # Data providers (ChapterDataProvider, etc.)
│   │   ├── domain/            # Domain models and contracts
│   │   │   ├── models/        # Verse, Chapter, MushafType, ReciterInfo
│   │   │   └── repository/    # Repository interfaces
│   │   ├── di/                # Core DI modules (Hilt)
│   │   └── util/              # Core utilities
│   ├── build.gradle.kts
│   └── consumer-rules.pro     # ProGuard rules for consumers
│
├── mushaf-ui/                # UI library module
│   ├── src/main/java/com/mushafimad/ui/
│   │   ├── mushaf/            # Mushaf UI (MushafView, QuranPageView)
│   │   ├── player/            # Audio player UI (QuranPlayerView)
│   │   ├── search/            # Search UI (SearchView)
│   │   ├── theme/             # Theming (ReadingTheme, ColorScheme)
│   │   ├── components/        # Reusable UI components (VerseFasel, etc.)
│   │   └── di/                # UI DI modules
│   ├── build.gradle.kts
│   └── consumer-rules.pro
│
├── sample/                   # Sample app (unchanged)
│   └── build.gradle.kts      # Depends on mushaf-ui
│
└── build.gradle.kts          # Root build file
```

**Two Artifacts:**
- `com.mushafimad:mushaf-core:1.0.0` - Core data layer
- `com.mushafimad:mushaf-ui:1.0.0` - UI components (depends on core)

---

### Module Responsibilities

#### mushaf-core (Data Layer)

**Purpose:** Headless Quran library for custom UI implementations

**Includes:**
- ✅ Realm database and entities
- ✅ Repository pattern (interfaces + implementations)
- ✅ Domain models (Verse, Chapter, Page, Part, Quarter, MushafType)
- ✅ Audio services (AudioPlayerService, AyahTimingService, ReciterService)
- ✅ Data providers (ChapterDataProvider, ReciterDataProvider)
- ✅ Networking (if needed for audio/translations)
- ✅ Caching (ImageCache, data caching)
- ✅ Core utilities (string helpers, formatters)
- ✅ Hilt modules for DI
- ✅ Assets (Quran images, timing JSON, metadata)

**Excludes:**
- ❌ Jetpack Compose dependencies
- ❌ ViewModels (UI logic)
- ❌ Composables
- ❌ Navigation
- ❌ UI themes/theming

**Dependencies:**
```kotlin
// Core Android
implementation(libs.androidx.core.ktx)

// Database
implementation(libs.realm.kotlin)

// Audio
implementation(libs.media3.exoplayer)
implementation(libs.media3.ui)

// Dependency Injection
implementation(libs.hilt.android)
kapt(libs.hilt.compiler)

// Coroutines
implementation(libs.kotlinx.coroutines.android)

// JSON parsing (for timing data)
implementation(libs.gson) // or Moshi

// Image loading (optional, for internal caching)
implementation(libs.coil) // or make it API dependency
```

**Public API Example:**
```kotlin
// Domain models
data class Verse(...)
data class Chapter(...)
data class MushafType(...)

// Repository interfaces
interface VerseRepository {
    suspend fun getVersesForPage(pageNumber: Int): List<Verse>
    suspend fun searchVerses(query: String): List<Verse>
}

interface AudioPlayerService {
    fun play(verse: Verse)
    fun pause()
    val playbackState: StateFlow<PlaybackState>
}

// DI entry point for custom implementations
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MushafCoreEntryPoint {
    fun verseRepository(): VerseRepository
    fun chapterRepository(): ChapterRepository
    fun audioPlayerService(): AudioPlayerService
}
```

---

#### mushaf-ui (UI Layer)

**Purpose:** Ready-to-use Jetpack Compose UI components

**Includes:**
- ✅ All Compose UI components (MushafView, QuranPlayerView, SearchView)
- ✅ ViewModels (MushafViewModel, QuranPlayerViewModel, SearchViewModel)
- ✅ Reading themes (ReadingTheme, ColorScheme)
- ✅ UI utilities (Compose helpers, modifiers)
- ✅ Navigation helpers
- ✅ Hilt modules for UI DI

**Excludes:**
- ❌ Data layer implementation (uses mushaf-core)
- ❌ Database implementation
- ❌ Audio service implementation

**Dependencies:**
```kotlin
// Depends on core
api(project(":mushaf-core"))

// Jetpack Compose
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.ui.tooling.preview)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.navigation.compose)

// Dependency Injection
implementation(libs.hilt.android)
implementation(libs.hilt.navigation.compose)
kapt(libs.hilt.compiler)

// Coil for image loading
implementation(libs.coil.compose)
```

**Public API Example:**
```kotlin
// Main Composables
@Composable
fun MushafView(
    readingTheme: ReadingTheme,
    colorScheme: ColorSchemeType,
    mushafType: MushafType,
    initialPage: Int = 1,
    onVerseClicked: (Verse) -> Unit = {},
    modifier: Modifier = Modifier
)

@Composable
fun QuranPlayerView(
    viewModel: QuranPlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
)

@Composable
fun SearchView(
    onNavigateToVerse: (Verse) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
)

// Theming
enum class ReadingTheme { COMFORTABLE, SEPIA, NIGHT, WHITE }
data class ColorSchemeType(...)
```

---

### Dependency Strategy

#### Module Dependencies

```
sample (app)
    ↓
mushaf-ui
    ↓ (api dependency)
mushaf-core
```

**Key Points:**
1. **mushaf-core**: Standalone, no dependency on UI
2. **mushaf-ui**: Depends on core via `api` (transitive to consumers)
3. **sample**: Only needs to depend on `mushaf-ui`

#### Gradle Configuration

**mushaf-core/build.gradle.kts:**
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mushafimad.core"
    // ... android config
}

dependencies {
    // Core dependencies (no Compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.realm.kotlin)
    implementation(libs.media3.exoplayer)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // ... other core deps
}
```

**mushaf-ui/build.gradle.kts:**
```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mushafimad.ui"
    // ... android config
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Core module (exposed to consumers)
    api(project(":mushaf-core"))

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    // ... other UI deps
}
```

**sample/build.gradle.kts:**
```kotlin
dependencies {
    // Only need UI module (core comes transitively)
    implementation(project(":mushaf-ui"))

    // App-specific dependencies
    implementation(libs.androidx.activity.compose)
    // ...
}
```

---

### Versioning Strategy

**Inspired by Firebase BOM but simplified for 2 modules**

#### Option 1: Unified Versioning (Recommended)

**Approach:** Both modules share the same version number

**Benefits:**
- ✅ Simpler for consumers
- ✅ Guaranteed compatibility
- ✅ Clear release cadence
- ✅ No version mismatch issues

**Implementation:**
```kotlin
// gradle.properties
MUSHAF_LIBRARY_VERSION=1.0.0

// Publishing
group = "com.mushafimad"
version = MUSHAF_LIBRARY_VERSION

// Artifacts
com.mushafimad:mushaf-core:1.0.0
com.mushafimad:mushaf-ui:1.0.0  (depends on mushaf-core:1.0.0)
```

**Consumer Usage:**
```kotlin
dependencies {
    // Both always use the same version
    val mushafVersion = "1.0.0"
    implementation("com.mushafimad:mushaf-core:$mushafVersion")
    implementation("com.mushafimad:mushaf-ui:$mushafVersion")

    // Or just UI (core comes automatically)
    implementation("com.mushafimad:mushaf-ui:$mushafVersion")
}
```

---

#### Option 2: BOM (Bill of Materials)

**Approach:** Provide a BOM artifact that manages versions

**Benefits:**
- ✅ Firebase-style version management
- ✅ Single version declaration
- ✅ Flexibility for patch releases

**Implementation:**
```kotlin
// mushaf-bom/build.gradle.kts
plugins {
    id("java-platform")
}

dependencies {
    constraints {
        api("com.mushafimad:mushaf-core:1.0.0")
        api("com.mushafimad:mushaf-ui:1.0.0")
    }
}

// Publishing
group = "com.mushafimad"
artifactId = "mushaf-bom"
version = "1.0.0"
```

**Consumer Usage:**
```kotlin
dependencies {
    // Import BOM
    implementation(platform("com.mushafimad:mushaf-bom:1.0.0"))

    // Use modules without version
    implementation("com.mushafimad:mushaf-core")
    implementation("com.mushafimad:mushaf-ui")
}
```

**Recommendation:** Start with **Option 1 (Unified Versioning)** for simplicity. Consider BOM when you have 5+ modules.

---

### Lessons from Quran Android

#### ✅ What to Adopt

1. **Modular Architecture Concept**
   - Separation of concerns
   - Clear module boundaries
   - Reusable components
   - **Adaptation:** Simplified to 2 modules instead of 28

2. **Build Variants & Flavors**
   - Multiple build types (Debug, Beta, Release)
   - Flavor dimensions for different configurations
   - **Adopt:** Add build variants for different distributions

3. **Dual Analytics Pattern**
   - Optional Firebase analytics
   - No-op stub for privacy/OSS builds
   - **Adopt:** Implement analytics interface with optional implementation

4. **ProGuard/R8 Configuration**
   - Consumer ProGuard rules
   - Code shrinking and obfuscation
   - **Already Done:** consumer-rules.pro in place

5. **Testing Infrastructure**
   - Unit tests for business logic
   - Integration tests for repositories
   - UI tests for critical flows
   - **Adopt:** Comprehensive test suite

6. **Build-Logic Abstraction**
   - Centralized build configuration
   - Version catalogs (already using)
   - Custom Gradle plugins
   - **Adopt:** build-logic module for shared config

7. **Clean Architecture Principles**
   - Domain models independent of framework
   - Repository pattern
   - Dependency inversion
   - **Already Following:** Clean Architecture

8. **WorkManager for Background Tasks**
   - Audio downloads
   - Database updates
   - **Adopt:** For download manager feature

---

#### ❌ What to Skip/Avoid

1. **SQLDelight Database**
   - Keep Realm Kotlin (already implemented)
   - Better cross-platform support (iOS parity)
   - **Reason:** Realm schema v24 already shared with iOS

2. **MVP Pattern**
   - Keep MVVM with Jetpack Compose
   - Better state management
   - More idiomatic for Compose
   - **Reason:** MVVM is better for declarative UI

3. **Excessive Modularization (28 modules)**
   - Keep 2-module approach (core + ui)
   - Avoid over-engineering
   - **Reason:** Smaller project, smaller team

4. **RxJava**
   - Keep pure Coroutines + Flow
   - Simpler for new developers
   - **Reason:** No legacy code, modern codebase

5. **XML Layouts**
   - Already 100% Jetpack Compose
   - **Reason:** Modern from the start

6. **Legacy Java Code**
   - Keep 100% Kotlin
   - **Reason:** New project, no legacy baggage

7. **Dual Reactive Approaches**
   - Stick to Coroutines only
   - Avoid RxJava + Coroutines mix
   - **Reason:** Consistency and simplicity

---

### Migration Plan

#### Step-by-Step Implementation

**Phase 8.1: Preparation (1 week)**
- [ ] Create detailed module dependency diagram
- [ ] Identify all public APIs (core vs UI)
- [ ] Document breaking changes
- [ ] Set up version management strategy
- [ ] Create migration guide for existing consumers

**Phase 8.2: Create Module Structure (3 days)**
- [ ] Create `mushaf-core` module directory
- [ ] Create `mushaf-ui` module directory
- [ ] Configure `settings.gradle.kts` with new modules
- [ ] Set up build.gradle.kts for each module
- [ ] Configure ProGuard rules

**Phase 8.3: Move Core Code (1 week)**
- [ ] Move `data/` package to mushaf-core
- [ ] Move `domain/` package to mushaf-core
- [ ] Move core DI modules to mushaf-core
- [ ] Move assets (images, JSON) to mushaf-core
- [ ] Update package names: `com.mushafimad.core.*`
- [ ] Fix imports and dependencies

**Phase 8.4: Move UI Code (1 week)**
- [ ] Move `ui/` package to mushaf-ui
- [ ] Move ViewModels to mushaf-ui
- [ ] Move UI DI modules to mushaf-ui
- [ ] Update package names: `com.mushafimad.ui.*`
- [ ] Add `api(project(":mushaf-core"))` dependency
- [ ] Fix imports and dependencies

**Phase 8.5: Update Sample App (2 days)**
- [ ] Change dependency from `:library` to `:mushaf-ui`
- [ ] Update imports
- [ ] Test all features
- [ ] Verify no regressions

**Phase 8.6: Testing & Validation (1 week)**
- [ ] Run all tests (unit, integration, UI)
- [ ] Verify build times (should be faster with modularization)
- [ ] Test sample app thoroughly
- [ ] Performance testing
- [ ] Memory leak detection

**Phase 8.7: Documentation (3 days)**
- [ ] Update README with new module structure
- [ ] Create integration guides for:
  - Core-only users (custom UI)
  - UI users (ready-to-use components)
- [ ] Document public APIs with KDoc
- [ ] Create migration guide from v1.0.0 (monolithic)

**Phase 8.8: Publishing Setup (1 week)**
- [ ] Configure Maven Central or JitPack
- [ ] Set up publishing scripts
- [ ] Configure signing (GPG keys)
- [ ] Test publishing to staging repository
- [ ] Publish v2.0.0 with modular structure

---

### Build Variants (From Quran Android)

**Implement multi-variant builds for flexibility:**

#### Build Types
```kotlin
android {
    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }

        create("beta") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".beta"
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

#### Flavor Dimensions (Optional)
```kotlin
android {
    flavorDimensions += listOf("analytics")

    productFlavors {
        create("oss") {
            dimension = "analytics"
            // Open-source build: no Firebase, no analytics
        }

        create("playstore") {
            dimension = "analytics"
            // Play Store build: includes Firebase analytics
        }
    }
}

// Resulting variants:
// - ossDebug, ossRelease (no analytics)
// - playstoreDebug, playstoreRelease (with Firebase)
```

---

### Analytics Pattern (From Quran Android)

**Implement privacy-respecting, optional analytics:**

#### Core Module - Analytics Interface
```kotlin
// mushaf-core/src/main/java/com/mushafimad/core/analytics/Analytics.kt
interface Analytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(key: String, value: String)
}

// No-op implementation (default)
class NoOpAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        // Do nothing
    }
    override fun setUserProperty(key: String, value: String) {
        // Do nothing
    }
}

// DI Module
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideAnalytics(): Analytics {
        // Can be swapped via build variants
        return NoOpAnalytics()
    }
}
```

#### Optional Firebase Implementation
```kotlin
// For Play Store flavor only
class FirebaseAnalyticsImpl(
    private val firebaseAnalytics: FirebaseAnalytics
) : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun setUserProperty(key: String, value: String) {
        firebaseAnalytics.setUserProperty(key, value)
    }
}
```

---

### Testing Infrastructure (From Quran Android)

**Implement comprehensive testing:**

#### Unit Tests (mushaf-core)
```kotlin
// test/
├── data/
│   ├── repository/
│   │   └── VerseRepositoryImplTest.kt
│   └── cache/
│       └── ChaptersDataCacheTest.kt
├── domain/
│   └── models/
│       └── VerseTest.kt
└── audio/
    └── AyahTimingServiceTest.kt
```

#### UI Tests (mushaf-ui)
```kotlin
// androidTest/
├── ui/
│   ├── mushaf/
│   │   └── MushafViewTest.kt
│   ├── player/
│   │   └── QuranPlayerViewTest.kt
│   └── search/
│       └── SearchViewTest.kt
└── integration/
    └── NavigationTest.kt
```

#### Test Dependencies
```kotlin
dependencies {
    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.truth)
    testImplementation(libs.turbine) // Flow testing
    testImplementation(libs.kotlinx.coroutines.test)

    // UI testing
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

---

### Publishing Strategy

#### Maven Central Publishing

**Both modules published to Maven Central:**

```kotlin
// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.mushafimad"
            artifactId = "mushaf-core" // or "mushaf-ui"
            version = MUSHAF_LIBRARY_VERSION

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Mushaf Imad")
                description.set("High-quality Quran library for Android")
                url.set("https://github.com/YahiaRagae/mushaf-imad-android")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("YahiaRagae")
                        name.set("Yahia Ragae")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/YahiaRagae/mushaf-imad-android.git")
                    url.set("https://github.com/YahiaRagae/mushaf-imad-android")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
```

---

### Timeline & Phases

**Total Estimated Time: 4-5 weeks**

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| 8.1: Preparation | 1 week | None |
| 8.2: Module Structure | 3 days | 8.1 |
| 8.3: Move Core Code | 1 week | 8.2 |
| 8.4: Move UI Code | 1 week | 8.3 |
| 8.5: Update Sample App | 2 days | 8.4 |
| 8.6: Testing & Validation | 1 week | 8.5 |
| 8.7: Documentation | 3 days | 8.6 |
| 8.8: Publishing Setup | 1 week | 8.7 |

**Milestones:**
- ✅ Week 1: Preparation complete
- ✅ Week 2: Both modules created and code moved
- ✅ Week 3: Sample app updated, tests passing
- ✅ Week 4: Documentation complete
- ✅ Week 5: Published to Maven Central

---

### Success Criteria

**Phase 8 is complete when:**
- [ ] `mushaf-core` module is fully functional and testable
- [ ] `mushaf-ui` module is fully functional and testable
- [ ] Sample app uses `mushaf-ui` with no regressions
- [ ] Both modules have comprehensive documentation
- [ ] Both modules are published to Maven Central
- [ ] Migration guide is available for existing users
- [ ] All tests pass (unit + integration + UI)
- [ ] Performance is equivalent or better than monolithic version
- [ ] Build times are improved due to modularization

---

## Current Status

### Android Library
**Version:** Pre-release / Development
**Status:** ✅ Core Features Complete | ❌ Background Audio Missing

**What's Working:**
- [x] Page navigation (604 pages)
- [x] Image-based Mushaf rendering
- [x] Verse highlighting and selection
- [x] Fasel (verse number) decorations
- [x] Multiple reading themes and color schemes
- [x] Audio playback with 18 reciters (foreground only)
- [x] Real-time verse highlighting during audio
- [x] Reciter selection
- [x] Playback controls (play/pause, seek, speed, repeat)
- [x] Reading position persistence
- [x] RTL layout support
- [x] Sample app demonstrating integration
- [x] Search functionality (verses and chapters)

**Known Issues:**
- [ ] **CRITICAL**: Audio stops when screen turns off (no background playback)
- [ ] **CRITICAL**: No lock screen controls
- [ ] **CRITICAL**: No notification playback controls
- [ ] Audio playback requires testing on physical devices
- [ ] Need to verify all 18 reciters' audio URLs are accessible
- [ ] Performance testing needed on lower-end devices
- [ ] **Android 16 KB Alignment Warning**: Realm library compatibility issue on Android 15+

**Build Output:**
- Library: `library/build/outputs/aar/library-debug.aar`
- Sample App: `sample/build/outputs/apk/debug/sample-debug.apk`

### iOS Library
**Status:** ✅ Feature Complete (Version 1.0.4)
**Location:** `/ios` directory

**Key Features:**
- [x] Full Quran text display (604 pages) with SwiftUI
- [x] Verse-by-verse audio playback with 18 reciters
- [x] Synchronized highlighting during audio playback
- [x] Multiple reading themes (Comfortable, Calm, Night, White)
- [x] Horizontal and vertical scrolling modes
- [x] RTL (Right-to-Left) layout support
- [x] Cross-platform (iOS 17+, macOS 14+)
- [x] Offline-first architecture with bundled resources
- [x] Background audio playback
- [x] Lock screen controls

**Technology Stack:**
- Swift 6.0 + SwiftUI
- RealmSwift 10.49+ (schema version 24)
- AVFoundation for audio
- Swift Package Manager

**Common Commands:**
```bash
# Build
cd ios && swift build -v

# Run Tests
cd ios && swift test -v

# Open Example App
cd ios/Example && open Example.xcodeproj
```

**For detailed iOS documentation:** See `ios/README.md` and `ios/TROUBLESHOOTING.md`

---

## Cross-Platform Architecture

### Shared Concepts
Both iOS and Android implementations share:
- **Database Schema:** Realm schema version 24 (cross-platform compatible)
- **Resource Structure:** 604 pages, 15 lines per page, same image dimensions
- **Audio System:** 18 reciters with identical timing data format
- **UI Patterns:** Equivalent components (MushafView, QuranPlayerView)
- **Reading Themes:** Same color schemes and theme names
- **Data Models:** Equivalent domain models (Chapter, Verse, Page, Part, Quarter)

### Platform-Specific Implementations

| Feature | iOS | Android |
|---------|-----|---------|
| **Language** | Swift 6.0 | Kotlin 1.9.25 |
| **UI Framework** | SwiftUI | Jetpack Compose |
| **Database** | RealmSwift 10.49+ | Realm Kotlin 2.3.0 |
| **Audio** | AVFoundation/AVPlayer | Media3 (ExoPlayer) |
| **DI** | N/A (protocol-based) | Hilt |
| **Async** | Combine + async/await | Coroutines + Flow |
| **Min Version** | iOS 17+, macOS 14+ | Android 7.0 (API 24+) |

---

## Architecture

### Data Layer
```
data/
├── audio/
│   ├── AudioPlayerService.kt       (ExoPlayer wrapper)
│   ├── AyahTimingService.kt        (Verse timing from JSON)
│   ├── ReciterService.kt           (Reciter management)
│   └── ReciterDataProvider.kt      (18 reciters metadata)
├── local/
│   ├── entities/                   (Realm entities)
│   ├── dao/                        (Data access objects)
│   └── RealmServiceImpl.kt         (Realm database service)
├── repository/
│   ├── RealmServiceImpl.kt         (Main Realm service)
│   ├── ReadingHistoryRepositoryImpl.kt
│   └── SearchHistoryRepositoryImpl.kt
└── cache/
    └── ChaptersDataCache.kt        (In-memory chapter cache)
```

### Domain Layer
```
domain/
├── models/
│   ├── Verse.kt
│   ├── Chapter.kt
│   ├── MushafType.kt
│   ├── ReciterInfo.kt
│   └── AyahTiming.kt
└── repository/
    └── MushafRepository.kt
```

### UI Layer
```
ui/
├── mushaf/
│   ├── MushafView.kt               (Main Mushaf composable)
│   ├── MushafWithPlayerView.kt     (Mushaf + audio player)
│   ├── MushafViewModel.kt
│   ├── QuranPageView.kt            (Page rendering)
│   └── VerseRenderer.kt            (Verse interaction)
├── player/
│   ├── QuranPlayerView.kt          (Audio player UI)
│   ├── QuranPlayerViewModel.kt
│   └── ReciterPickerDialog.kt
└── theme/
    ├── MushafTheme.kt
    ├── ReadingTheme.kt
    └── ColorScheme.kt
```

### Dependency Injection
```
di/
├── MushafCoreModule.kt             (Core dependencies)
├── MushafAudioModule.kt            (Audio services)
├── MushafPreferencesModule.kt      (SharedPreferences, repos)
└── MushafUserDataModule.kt         (User data repos)
```

### Utilities
```
logging/
└── Logger.kt                       (Logging utilities)

utils/
└── (Core utility functions)
```

---

## Next Steps

### Priority 0: Critical - Background Audio Playback (Phase 7)
**Status:** 🔴 MUST DO FIRST - Blocking production release

- [ ] **Task 7.1**: Add MediaSession dependencies
- [ ] **Task 7.2**: Create MediaSessionService for background playback
- [ ] **Task 7.3**: Update library manifest with permissions
- [ ] **Task 7.4**: Refactor AudioPlayerService integration
- [ ] **Task 7.5**: Handle notification permissions (Android 13+)
- [ ] **Task 7.6**: Test on multiple Android versions

**See Phase 7 section above for detailed implementation checklist**

### Priority 1: Testing & Stabilization
- [ ] Test audio playback on physical devices
- [ ] Verify all 18 reciters' audio URLs are accessible
- [ ] Test on different Android versions (API 26+)
- [ ] Performance testing on lower-end devices
- [ ] Memory leak detection
- [ ] Test verse highlighting accuracy with different reciters
- [x] Handle network errors gracefully (basic audio focus handling implemented)
- [ ] Fix Android 16 KB alignment issue (requires Realm library update or workaround)

### Priority 2: Missing Features
- [ ] **Bookmarks System**
  - [ ] Save favorite verses
  - [ ] Bookmark management UI
  - [ ] Sync across devices (optional)

- [ ] **Translations**
  - [ ] Display verse translations
  - [ ] Multiple language support
  - [ ] Translation selection UI

- [ ] **Tafsir (Commentary)**
  - [ ] Display verse explanations
  - [ ] Multiple tafsir sources
  - [ ] Tafsir viewer UI

- [ ] **Reading History**
  - [ ] Track recently read pages
  - [ ] Reading statistics
  - [ ] Reading goals/progress

- [ ] **Verse-by-Verse Audio Playback**
  - [ ] Integrate AyahTimingService with AudioPlaybackService
  - [ ] Emit current verse events during playback
  - [ ] Update MushafView to highlight current verse
  - [ ] Add verse navigation (play from specific verse)
  - [ ] Gapless verse-to-verse playback

### Priority 3: UI/UX Enhancements
- [x] Improved loading states (basic loading indicators implemented)
- [ ] Better error messages and recovery
- [ ] Onboarding/tutorial for first-time users
- [ ] Accessibility improvements (TalkBack, font scaling)
- [ ] Landscape orientation support
- [ ] Tablet optimization
- [ ] Animations and transitions
- [ ] Haptic feedback

### Priority 4: Library Publishing
- [ ] **Prerequisite: Complete Phase 8 (Modularization)**
  - [ ] Split into mushaf-core and mushaf-ui modules
  - [ ] See Phase 8 section for detailed plan

- [ ] **Documentation**
  - [ ] API documentation (KDoc)
  - [ ] Integration guide
  - [x] Sample code snippets (sample app demonstrates all features)
  - [ ] Migration guides

- [ ] **Publishing**
  - [ ] Maven Central or JitPack setup
  - [ ] Semantic versioning
  - [ ] Changelog maintenance
  - [ ] Release process automation

- [ ] **Library Configuration**
  - [x] ProGuard rules (proguard-rules.pro and consumer-rules.pro created)
  - [ ] Resource shrinking
  - [x] Dependency optimization (using version catalog)
  - [ ] Size analysis and optimization

### Priority 5: Advanced Features
- [ ] **Offline Support**
  - [ ] Download audio for offline playback
  - [ ] Download manager
  - [ ] Storage management
  - [ ] Resume failed downloads

- [ ] **Social Features**
  - [ ] Share verses
  - [ ] Share audio clips
  - [ ] Social media integration

- [ ] **Analytics** (Privacy-respecting)
  - [ ] Reading patterns
  - [ ] Feature usage
  - [ ] Error tracking (opt-in)
  - [ ] Optional Firebase vs NoOp implementation

- [ ] **Customization**
  - [ ] Custom fonts (if allowed)
  - [ ] Line spacing adjustments
  - [ ] Custom color themes

### Priority 6: iOS Parity
- [x] Ensure feature parity between iOS and Android (core features implemented)
- [x] Shared data format/structure (Realm schema v24 compatible)
- [ ] Cross-platform testing
- [x] Unified design system (equivalent themes and color schemes)

---

## Technical Debt

### Code Quality
- [ ] **Testing Coverage**
  - [ ] Increase test coverage (currently minimal)
  - [ ] Add unit tests for ViewModels
  - [ ] Add integration tests for repositories
  - [ ] UI tests for critical flows
  - [ ] Performance benchmarking

### Refactoring Opportunities
- [x] **UI Components**
  - [x] Extract common UI components (MushafView, QuranPlayerView, ReciterPickerDialog, etc.)
  - [x] Consolidate theming logic (MushafTheme, ReadingTheme, ColorScheme)

- [ ] **Performance**
  - [ ] Optimize image loading pipeline
  - [ ] Review error handling patterns
  - [ ] Improve logging strategy

### Documentation
- [ ] **API Documentation**
  - [ ] Add KDoc to all public APIs
  - [ ] Create architecture decision records (ADRs)

- [x] **Architecture Documentation**
  - [x] Document internal architecture decisions (PLAN.md with architecture sections)
  - [x] Add code examples to documentation (sample app provides working examples)

---

## Resources & Dependencies

### Key Dependencies
- **Jetpack Compose** - Modern UI toolkit
- **Hilt** - Dependency injection
- **Media3 (ExoPlayer)** - Audio playback
- **Kotlin Coroutines & Flow** - Asynchronous programming
- **Realm Kotlin** - Local database
- **Coil** - Image loading

### Assets
- **Quran Images**: 604 pages, line-by-line images
- **Fasel Decorations**: PNG verse markers
- **Verse Timing JSON**: Timing data for 18 reciters
- **Audio URLs**: Streaming MP3 files from various sources

### External APIs/Services
- Audio streaming servers (per reciter)
- Future: Translation APIs
- Future: Tafsir APIs

---

## Notes

### Design Decisions
1. **Image-based rendering** chosen over text rendering for authentic Mushaf appearance
2. **Line-by-line images** for better performance than full-page images
3. **ExoPlayer** for robust audio playback with network streaming
4. **StateFlow** for reactive state management
5. **Hilt** for compile-time safe dependency injection

### Future Considerations
- Consider migrating to type-safe navigation (Navigation 3's `@Serializable` routes)
- Consider migrating to Kotlin Multiplatform for shared business logic
- Evaluate Compose Multiplatform for shared UI
- Consider offline-first architecture for better UX
- Plan for internationalization (i18n) early
- Consider accessibility from the start

---

## Contact & Contribution

This is a private project. For questions or contributions, please contact the project maintainer.

---

**Last Updated:** January 17, 2026
**Current Phase:** Core Features Complete (Phases 1-6: Foundation, Audio, Search, Navigation, Sample App)
**Critical Next Phase:** Phase 7 - Background Audio Playback (1-2 weeks)
**Planned Phase:** Phase 8 - Library Modularization (mushaf-core + mushaf-ui)
**Next Milestone:** Implement background audio playback for production readiness
