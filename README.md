# Mushaf Imad - Android Library

A Quran reader library for Android providing high-quality Mushaf page display with audio recitation support.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)](https://kotlinlang.org)

## Features

- 📖 Full Quran text display (604 pages)
- 🎵 Audio playback with 18 reciters
- ✨ Real-time verse highlighting during audio
- 🎨 Multiple reading themes (Comfortable, Calm, Night, White)
- 🔍 Search functionality (verses and chapters)
- 📱 RTL (Right-to-Left) layout support
- 💾 Offline-first architecture
- 🎯 Clean Architecture with Hilt DI
- 🎨 Jetpack Compose UI

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

```kotlin
// In your app's build.gradle.kts
dependencies {
    implementation(project(":library"))

    // Required dependencies
    implementation("com.google.dagger:hilt-android:2.54")
    kapt("com.google.dagger:hilt-android-compiler:2.54")
}
```

### 2. Initialize in Application

```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Library initializes automatically via Hilt
    }
}
```

### 3. Update AndroidManifest.xml

```xml
<application
    android:name=".MyApplication"
    android:supportsRtl="true">

    <!-- Add internet permission for audio streaming -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</application>
```

---

## Usage Examples

### Basic Mushaf Reader

```kotlin
import com.mushafimad.library.ui.mushaf.MushafView
import com.mushafimad.library.ui.theme.ReadingTheme
import com.mushafimad.library.ui.theme.ColorSchemeType
import com.mushafimad.library.domain.models.MushafType

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
import com.mushafimad.library.ui.mushaf.MushafWithPlayerView

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
import com.mushafimad.library.ui.search.SearchView

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

The library follows Clean Architecture principles with clear separation of concerns:

```
library/
├── data/                       # Data layer
│   ├── audio/                  # Audio playback (ExoPlayer)
│   ├── local/                  # Local database (Realm)
│   │   ├── entities/           # Realm entities
│   │   └── dao/                # Data access objects
│   ├── repository/             # Repository implementations
│   └── cache/                  # Caching services
│
├── domain/                     # Domain layer
│   ├── models/                 # Domain models (public API)
│   └── repository/             # Repository interfaces
│
├── ui/                         # UI layer
│   ├── mushaf/                 # Mushaf reader components
│   ├── player/                 # Audio player components
│   ├── search/                 # Search components
│   └── theme/                  # Theming
│
└── di/                         # Dependency injection
    ├── MushafCoreModule.kt
    ├── MushafAudioModule.kt
    └── MushafPreferencesModule.kt
```

---

## Technology Stack

- **UI:** Jetpack Compose with Material 3
- **Database:** Realm Kotlin 2.3.0 (schema version 24)
- **Audio:** Media3 (ExoPlayer) 1.5.0
- **DI:** Hilt 2.54
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Navigation Compose 2.8.5
- **Image Loading:** Coil 2.7.0
- **Build:** Gradle 8.7.3 with Version Catalog

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

### Build AAR

```bash
./gradlew :library:assembleDebug
```

Output: `library/build/outputs/aar/library-debug.aar`

### Run Tests

```bash
./gradlew :library:testDebugUnitTest
```

### Build Sample App

```bash
./gradlew :sample:assembleDebug
```

Output: `sample/build/outputs/apk/debug/sample-debug.apk`

---

## Project Status

**Version:** Pre-release / Development
**Status:** ✅ Feature Complete

### What's Working
- ✅ Page navigation (604 pages)
- ✅ Image-based Mushaf rendering
- ✅ Verse highlighting and selection
- ✅ Fasel (verse number) decorations
- ✅ Multiple reading themes and color schemes
- ✅ Audio playback with 18 reciters
- ✅ Real-time verse highlighting during audio
- ✅ Reciter selection
- ✅ Playback controls (play/pause, seek, speed, repeat)
- ✅ Search functionality (verses and chapters)
- ✅ Reading position persistence
- ✅ RTL layout support
- ✅ Sample app demonstrating all features

### Known Issues
- Audio playback requires testing on physical devices
- Performance testing needed on lower-end devices

---

## Roadmap

### Priority 1: Testing & Stabilization
- Test audio playback on physical devices
- Verify all 18 reciters' audio URLs
- Test on different Android versions (API 24-35)
- Performance optimization
- Memory leak detection

### Priority 2: Missing Features
- Bookmarks system
- Translations support
- Tafsir (commentary) integration
- Reading history

### Priority 3: Library Publishing
- API documentation (KDoc)
- Maven Central or JitPack setup
- Integration guide
- Release process automation

See **PLAN.md** for detailed roadmap.

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
- Open source libraries: Jetpack Compose, Realm, ExoPlayer, Hilt

---

**Last Updated:** January 15, 2026
**Current Phase:** Core Features Complete
