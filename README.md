# Mushaf Imad - Android Library

A Quran reader library for Android providing high-quality Mushaf page display with audio recitation support.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)](https://kotlinlang.org)
[![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)](https://github.com/YahiaRagae/mushaf-imad-android)
[![Status](https://img.shields.io/badge/Status-Stable-green.svg)](https://github.com/YahiaRagae/mushaf-imad-android)

> ✅ **Version 1.0.0:** The library is now feature-complete with background audio playback, modular architecture, and full production readiness.

## Features

- 📖 Full Quran text display (604 pages)
- 🎵 Audio playback with 18 reciters (background + foreground)
- 🔒 Lock screen controls and media notifications
- ✨ Real-time verse highlighting during audio
- 🎨 Multiple reading themes (Comfortable, Calm, Night, White)
- 🔍 Search functionality (verses and chapters)
- 📱 RTL (Right-to-Left) layout support
- 💾 Offline-first architecture
- 🏗️ Modular architecture (mushaf-core + mushaf-ui)
- 🎯 Clean Architecture with Koin DI
- 🎨 Jetpack Compose UI
- 🚀 Zero-configuration setup (auto-initialization)

---

## Requirements

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Kotlin:** 1.9.25
- **Jetpack Compose:** BOM 2024.12.01
- **Gradle:** 8.7.3

---

## Quick Start

### 1. Add Dependencies

The library is split into two modules for flexibility:

**Option A: Full library (UI + Data)**
```kotlin
// In your app's build.gradle.kts
dependencies {
    // UI module (includes mushaf-core transitively)
    implementation(project(":mushaf-ui"))
}
```

**Option B: Data layer only (custom UI)**
```kotlin
dependencies {
    // Core module only (for custom UI implementations)
    implementation(project(":mushaf-core"))
}
```

### 2. Zero-Configuration Setup

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Library auto-initializes via ContentProvider!
        // No manual setup required.

        // Optional: custom logger/analytics
        // MushafLibrary.setLogger(CustomLogger())
        // MushafLibrary.setAnalytics(CustomAnalytics())
    }
}
```

✨ The library uses **ContentProvider** for automatic initialization - no `@HiltAndroidApp` or manual setup required!

### 3. Update AndroidManifest.xml

```xml
<manifest>
    <!-- Required permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- For background audio (Android 9+) -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

    <!-- For notifications (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".MyApplication"
        android:supportsRtl="true">
        <!-- Your activities -->
    </application>
</manifest>
```

---

## Usage Examples

### Basic Mushaf Reader

```kotlin
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.theme.ReadingTheme
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.core.domain.models.MushafType

@Composable
fun MyMushafScreen() {
    MushafView(
        readingTheme = ReadingTheme.COMFORTABLE,
        colorScheme = ColorSchemeType.DEFAULT,
        mushafType = MushafType.HAFS_1441,
        initialPage = 1,
        showNavigationControls = true,
        showPageInfo = true,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Mushaf with Audio Player

```kotlin
import com.mushafimad.ui.mushaf.MushafWithPlayerView

@Composable
fun MushafWithAudioScreen() {
    MushafWithPlayerView(
        readingTheme = ReadingTheme.COMFORTABLE,
        colorScheme = ColorSchemeType.DEFAULT,
        mushafType = MushafType.HAFS_1441,
        initialPage = 1,
        showNavigationControls = true,
        showPageInfo = true,
        showAudioPlayer = true,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Search Functionality

```kotlin
import com.mushafimad.ui.search.SearchView

@Composable
fun SearchScreen() {
    var currentPage by remember { mutableStateOf<Int?>(null) }

    if (currentPage != null) {
        // Navigate to selected page
        MushafView(
            initialPage = currentPage,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        SearchView(
            onVerseSelected = { verse ->
                currentPage = verse.pageNumber
            },
            onChapterSelected = { chapter ->
                // Handle chapter selection
                currentPage = chapter.startPage
            },
            onDismiss = {
                // Handle dismiss
            }
        )
    }
}
```

### Theme Customization

```kotlin
@Composable
fun ThemedMushafScreen() {
    var selectedTheme by remember { mutableStateOf(ReadingTheme.COMFORTABLE) }
    var selectedColorScheme by remember { mutableStateOf(ColorSchemeType.DEFAULT) }

    MushafView(
        readingTheme = selectedTheme,
        colorScheme = selectedColorScheme,
        mushafType = MushafType.HAFS_1441,
        initialPage = 1,
        modifier = Modifier.fillMaxSize()
    )
}
```

---

## Available Components

### Reading Themes

```kotlin
enum class ReadingTheme {
    COMFORTABLE,  // Light green (#E4EFD9)
    CALM,         // Light blue (#E0F1EA)
    NIGHT,        // Dark theme (#2F352F)
    WHITE         // Pure white (#FFFFFF)
}
```

### Color Schemes

```kotlin
enum class ColorSchemeType {
    DEFAULT,
    GREEN,
    BLUE,
    PURPLE,
    ORANGE
}
```

### Mushaf Types

```kotlin
enum class MushafType {
    HAFS_1441,  // Modern layout (default)
    HAFS_1405   // Traditional layout
}
```

---

## Audio Features

### Available Reciters (18 total)

- Ibrahim Al-Akdar (إبراهيم الأخضر)
- Ahmad Al-Ajmy (أحمد بن علي العجمي)
- Mahmoud Khalil Al-Hussary (محمود خليل الحصري)
- Ali Abdur-Rahman al-Huthaify (علي بن عبدالرحمن الحذيفي)
- Saud Al-Shuraim (سعود الشريم)
- Abdul Rahman Al-Sudais (عبدالرحمن السديس)
- Bandar Baleela (بندر بليلة)
- Yasser Al-Dosari (ياسر الدوسري)
- Fares Abbad (فارس عباد)
- Maher Al Mueaqly (ماهر المعيقلي)
- Abdullah Basfar (عبدالله بصفر)
- Nasser Al Qatami (ناصر القطامي)
- Muhammad Ayyub (محمد أيوب)
- Omar Al-Qazabri (عمر القزابري) - Warsh recitation
- Mishari Rashid al-Afasy (مشاري العفاسي)
- Mohammad al Tablaway (محمد جبريل)
- Abdul Basit Abdus Samad (عبدالباسط عبدالصمد)
- Hani Ar-Rifai (هاني الرفاعي)

### Audio Controls

- ▶️ Play/Pause/Stop
- ⏭️ Next/Previous verse
- 🎚️ Playback speed (0.5x - 2.0x)
- 🔁 Repeat mode
- 🎯 Seek to specific verse
- ✨ Real-time verse highlighting

---

## Architecture

The library follows Clean Architecture principles with modular design:

### Module Structure

```
mushaf-core/                    # Headless data layer
├── data/                       # Data layer implementation
│   ├── audio/                  # Audio playback (Media3 ExoPlayer)
│   │   ├── AudioPlaybackService.kt    # Background playback service
│   │   ├── MediaSessionManager.kt     # MediaSession controller
│   │   └── ReciterDataProvider.kt     # Reciter information
│   ├── local/                  # Local database (Realm)
│   │   ├── entities/           # Realm entities
│   │   └── dao/                # Data access objects
│   ├── repository/             # Repository implementations
│   └── cache/                  # Caching services
│
├── domain/                     # Domain layer (public API)
│   ├── models/                 # Domain models
│   └── repository/             # Repository interfaces
│
├── di/                         # Dependency injection (Koin)
│   └── CoreModule.kt           # Koin module for repositories
└── internal/
    └── MushafInitProvider.kt   # ContentProvider for auto-init

mushaf-ui/                      # UI components (Jetpack Compose)
├── mushaf/                     # Mushaf reader components
│   ├── MushafView.kt          # Main Mushaf composable
│   ├── MushafViewModel.kt     # Mushaf state management
│   └── QuranPageView.kt       # Page rendering
├── player/                     # Audio player components
│   ├── QuranPlayerView.kt     # Player UI composable
│   └── QuranPlayerViewModel.kt # Player state management
├── search/                     # Search components
│   ├── SearchView.kt          # Search UI composable
│   └── SearchViewModel.kt     # Search state management
├── theme/                      # Theming
│   ├── ReadingTheme.kt        # Reading themes
│   └── ColorScheme.kt         # Color schemes
├── di/                         # UI DI (Koin)
│   └── UiModule.kt            # Koin module for ViewModels
└── internal/
    └── MushafUiInitProvider.kt # ContentProvider for UI module
```

### Key Benefits
- **mushaf-core**: Headless library for custom UI implementations
- **mushaf-ui**: Pre-built Compose components (depends on mushaf-core)
- **Clean separation**: Data layer completely independent from UI
- **Flexible integration**: Use core only or full UI components

---

## Technology Stack

- **UI:** Jetpack Compose with Material 3
- **Database:** Realm Kotlin 2.3.0 (schema version 24)
- **Audio:** Media3 (ExoPlayer) 1.5.0
- **DI:** Koin 3.5.6
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Navigation Compose 2.8.5
- **Image Loading:** Coil 2.7.0
- **Build:** Gradle 8.7.3 with Version Catalog
- **Init:** ContentProvider auto-initialization

---

## Sample App

Run the sample app to see all features in action:

```bash
./gradlew :sample:installDebug
```

The sample app demonstrates:

### Quick Start
- Chapters List
- Read the Mushaf

### Features
- Search (verses and chapters)
- Theme Customization

### Audio
- Mushaf with Audio Player
- Reciter Selection
- Playback Controls

### Navigation
- Category-based home screen
- Proper navigation stack with back button support

---

## Building the Library

### Build AARs

```bash
# Build mushaf-core module
./gradlew :mushaf-core:assembleDebug

# Build mushaf-ui module
./gradlew :mushaf-ui:assembleDebug

# Build both modules
./gradlew assembleDebug -x lint
```

Outputs:
- `mushaf-core/build/outputs/aar/mushaf-core-debug.aar`
- `mushaf-ui/build/outputs/aar/mushaf-ui-debug.aar`

### Run Tests

```bash
# Test mushaf-core
./gradlew :mushaf-core:testDebugUnitTest

# Test mushaf-ui
./gradlew :mushaf-ui:testDebugUnitTest
```

### Build Sample App

```bash
./gradlew :sample:assembleDebug
```

Output: `sample/build/outputs/apk/debug/sample-debug.apk`

---

## Project Status

**Version:** 1.0.0 (Stable)
**Status:** ✅ Production Ready

### ✅ Core Features
- ✅ Page navigation (604 pages)
- ✅ Image-based Mushaf rendering
- ✅ Verse highlighting and selection
- ✅ Fasel (verse number) decorations
- ✅ Multiple reading themes and color schemes
- ✅ Search functionality (verses and chapters)
- ✅ Reading position persistence
- ✅ RTL layout support

### ✅ Audio Features
- ✅ Background audio playback (MediaSessionService)
- ✅ Lock screen controls
- ✅ Notification playback controls
- ✅ 18 reciters with high-quality audio
- ✅ Real-time verse highlighting during audio
- ✅ Reciter selection
- ✅ Playback controls (play/pause, seek, speed, repeat)
- ✅ Bluetooth headset controls
- ✅ Android Auto integration ready

### ✅ Architecture
- ✅ Modular design (mushaf-core + mushaf-ui)
- ✅ Clean Architecture with Koin DI
- ✅ ContentProvider auto-initialization (zero-config)
- ✅ Jetpack Compose UI
- ✅ Sample app demonstrating all features

### Known Limitations
- Audio playback tested on Android 7.0+ devices
- Android 16 KB alignment warning (Realm library compatibility)
- Some deprecation warnings for Material icons (non-blocking)

---

## Roadmap

### ✅ Phase 7: Background Audio Playback (COMPLETED)
- ✅ Implemented MediaSessionService for background playback
- ✅ Added lock screen playback controls
- ✅ Added notification with playback controls
- ✅ Support for Bluetooth headset controls
- ✅ Added required Android permissions
- ✅ Tested on Android 7.0+ devices

### ✅ Phase 8: Library Modularization (COMPLETED)
- ✅ Split into `mushaf-core` (data layer) and `mushaf-ui` (UI components)
- ✅ Enabled developers to use data layer with custom UI
- ✅ Clean migration with package renaming
- ✅ Version 1.0.0 released

### ✅ Phase 9: Dependency Injection Migration (COMPLETED)
- ✅ Removed Hilt dependency (no framework requirement)
- ✅ Implemented Koin for lightweight DI
- ✅ ContentProvider auto-initialization (zero-config)
- ✅ Dual ContentProvider pattern (core + UI modules)
- ✅ Cleaned up unused code and comments
- ✅ Removed experimental code with invalid package names

### Priority 1: Code Quality & Linting (v1.1.0)
- Add ktlint for automated code formatting
- Configure pre-commit hooks
- Fix remaining deprecation warnings
- Improve code documentation

### Priority 2: Testing & Stabilization (v1.1.0)
- Test audio playback on more physical devices
- Verify all 18 reciters' audio URLs
- Test on different Android versions (API 24-35)
- Performance optimization
- Memory leak detection
- Fix Android 16 KB alignment warning
- Fix Material icon deprecation warnings

### Priority 3: Missing Features (v1.2.0 - v1.5.0)
- Bookmarks system
- Translations support
- Tafsir (commentary) integration
- Reading history
- Verse-by-verse audio playback
- Download manager for offline audio

### Priority 4: Library Publishing (v2.0.0)
- API documentation (KDoc)
- Maven Central or JitPack publishing
- Comprehensive integration guide
- Migration guide from v1.x
- Release process automation

See **PLAN.md** for detailed roadmap and task breakdowns.

---

## Contributing

This is a private project. For questions or contributions, please contact the project maintainer.

---

## Repository

**GitHub:** [https://github.com/YahiaRagae/mushaf-imad-android](https://github.com/YahiaRagae/mushaf-imad-android)

---

## License

This project is private. Contact the maintainer for licensing information.

---

## Credits

Developed with care for the Muslim community.

**Based on:**
- iOS MushafImad library by [Ibrahim Qraiqe (ibo2001)](https://github.com/ibo2001/MushafImad)

**Acknowledgments:**
- Quran text and metadata
- Audio recitations from various reciters
- Open source libraries: Jetpack Compose, Realm, ExoPlayer, Koin

---

**Last Updated:** January 17, 2026
**Current Version:** v1.0.0
**Status:** Stable - Production Ready
**Next Milestone:** v1.1.0 (Testing & Stabilization)
