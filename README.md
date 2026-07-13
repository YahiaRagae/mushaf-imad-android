# Mushaf Imad - Android Library

A Quran reader library for Android providing high-quality Mushaf page display with audio recitation support.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Version](https://img.shields.io/badge/Version-0.2.2-blue.svg)](https://github.com/YahiaRagae/mushaf-imad-android/releases/tag/0.2.2)
[![JitPack](https://jitpack.io/v/YahiaRagae/mushaf-imad-android.svg)](https://jitpack.io/#YahiaRagae/mushaf-imad-android)
[![Status](https://img.shields.io/badge/Status-Stable-green.svg)](https://github.com/YahiaRagae/mushaf-imad-android)

> ✅ **Version 0.2.1:** Supports 16 KB memory pages (required by Google Play for apps targeting Android 15+), and fixes a long list of bugs found during QA — including a startup crash and the loss of all saved user data on every launch. The public API is unchanged, so v0.1 code compiles as-is.

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
- **Kotlin:** 2.0.21
- **Jetpack Compose:** BOM 2024.12.01
- **Android Gradle Plugin:** 8.7.3 (Gradle 8.13)

---

## Quick Start

### 1. Add Dependencies

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency in your app's `build.gradle.kts`:

**Option A: Full library (UI + Data) — recommended**
```kotlin
dependencies {
    implementation("com.github.YahiaRagae.mushaf-imad-android:mushaf-ui:0.2.2")
}
```

**Option B: Data layer only (custom UI)**
```kotlin
dependencies {
    implementation("com.github.YahiaRagae.mushaf-imad-android:mushaf-core:0.2.2")
}
```

> `mushaf-ui` includes `mushaf-core` transitively — no need to add both.

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

    <!--
        WAKE_LOCK is declared by the library itself and merged in automatically -
        you do not need to add it.
    -->

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
                // Handle chapter selection - look up the first page for this chapter
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
    WARM,
    COOL,
    SEPIA
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

Read from the bundled verse-timing data, so this is exactly what
`AudioRepository.getAllReciters()` returns and what the reciter picker shows.

| ID | Reciter | Rewaya |
|----|---------|--------|
| 1 | Ibrahim Al-Akdar (إبراهيم الأخضر) | حفص عن عاصم |
| 5 | Ahmad Al-Ajmy (أحمد بن علي العجمي) | حفص عن عاصم |
| 9 | Ahmad Nauina (أحمد نعينع) | حفص عن عاصم |
| 10 | Akram Alalaqmi (أكرم العلاقمي) | حفص عن عاصم |
| 31 | Saud Al-Shuraim (سعود الشريم) | حفص عن عاصم |
| 32 | Sahl Yassin (سهل ياسين) | حفص عن عاصم |
| 51 | Abdulbasit Abdulsamad (عبدالباسط عبدالصمد) | المصحف المجود |
| 53 | Abdulbasit Abdulsamad (عبدالباسط عبدالصمد) | حفص عن عاصم |
| 60 | Abdullah Basfer (عبدالله بصفر) | حفص عن عاصم |
| 62 | Abdullah Al-Johany (عبدالله عواد الجهني) | حفص عن عاصم |
| 67 | Abdulmohsen Al-Qasim (عبدالمحسن القاسم) | حفص عن عاصم |
| 74 | Ali Alhuthaifi (علي بن عبدالرحمن الحذيفي) | حفص عن عاصم |
| 78 | Emad Hafez (عماد زهير حافظ) | حفص عن عاصم |
| 106 | Mohammad Al-Tablaway (محمد الطبلاوي) | حفص عن عاصم |
| 112 | Mohammed Siddiq Al-Minshawi (محمد صديق المنشاوي) | حفص عن عاصم |
| 118 | Mahmoud Khalil Al-Hussary (محمود خليل الحصري) | حفص عن عاصم |
| 159 | Khalid Almohana (خالد المهنا) | حفص عن عاصم |
| 256 | Ahmad Shaheen (أحمد خليل شاهين) | حفص عن عاصم |

### Audio Controls

- ▶️ Play/Pause/Stop
- ⏭️ Next/Previous verse
- 🎚️ Playback speed (0.75x - 3.0x)
- 🔁 Repeat mode
- 🎯 Seek to specific verse
- ✨ Real-time verse highlighting

---

## Architecture

The library follows Clean Architecture principles with modular design:

### Module Structure

```
mushaf-core/                    # Headless data layer (no Compose)
├── data/
│   ├── audio/
│   │   ├── AudioPlaybackService.kt    # Background playback (MediaSessionService)
│   │   ├── AyahTimingService.kt       # Verse timing for audio sync
│   │   ├── MediaSessionManager.kt     # MediaSession controller
│   │   ├── ReciterDataProvider.kt     # Reciter metadata
│   │   └── ReciterService.kt         # Reciter management
│   ├── local/
│   │   ├── entities/                  # Realm entities
│   │   └── dao/                       # Data access objects
│   ├── repository/                    # Repository implementations (Default*)
│   └── cache/                         # Caching services
├── domain/
│   ├── models/                        # Public domain models
│   └── repository/                    # Public repository interfaces
├── di/
│   └── CoreModule.kt                 # Koin singletons
└── internal/
    └── MushafInitProvider.kt          # ContentProvider auto-init

mushaf-ui/                      # Jetpack Compose UI (depends on mushaf-core)
├── mushaf/
│   ├── MushafView.kt                 # Main Mushaf composable
│   ├── MushafWithPlayerView.kt       # Mushaf + integrated audio player
│   ├── MushafViewModel.kt            # Mushaf state management
│   ├── QuranPageView.kt              # Page rendering
│   ├── QuranLineImageView.kt         # Line image rendering
│   └── VerseFasel.kt                 # Verse number decorations
├── player/
│   ├── QuranPlayerView.kt            # Audio player composable
│   ├── QuranPlayerViewModel.kt       # Player state management
│   └── ReciterPickerDialog.kt        # Reciter selection dialog (public API)
├── search/
│   ├── SearchView.kt                 # Search composable
│   └── SearchViewModel.kt            # Search state management
├── bookmarks/
│   └── BookmarksViewModel.kt         # Bookmark state management
├── history/
│   └── ReadingHistoryViewModel.kt    # Reading history state management
├── settings/
│   └── SettingsViewModel.kt          # Settings state management
├── theme/
│   ├── Color.kt                      # Color definitions
│   ├── Theme.kt                      # Theme composables
│   ├── ThemeViewModel.kt             # Theme state management
│   └── Typography.kt                 # Font configuration
├── di/
│   └── UiModule.kt                   # Koin ViewModels
└── internal/
    └── MushafUiInitProvider.kt        # ContentProvider for UI module
```

### Key Benefits
- **mushaf-core**: Headless library for custom UI implementations
- **mushaf-ui**: Pre-built Compose components (depends on mushaf-core)
- **Clean separation**: Data layer completely independent from UI
- **Flexible integration**: Use core only or full UI components
- **Clean Architecture**: Koin manages repository lifecycle (no manual singletons)
- **Zero boilerplate**: Repositories are simple classes with constructor injection
- **Testable**: Easy to inject fakes/mocks via Koin modules

---

## Technology Stack

- **UI:** Jetpack Compose with Material 3
- **Database:** Realm Kotlin 3.0.0 (schema version 24, 16 KB page aligned)
- **Audio:** Media3 (ExoPlayer) 1.5.0
- **DI:** Koin 3.5.6 (lightweight runtime DI, no code generation)
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Navigation Compose 2.8.5
- **Image Loading:** Coil 2.7.0
- **Build:** Gradle 8.7.3 with Version Catalog
- **Init:** ContentProvider auto-initialization

---

## Sample App

A sample app is included in the `sample/` module demonstrating all library features:

```bash
./gradlew :sample:installDebug
```

---

## Building from Source

```bash
# Build both library modules
./gradlew assembleDebug -x lint

# Run tests
./gradlew testDebugUnitTest
```

---

## Project Status

**Version:** 0.2.2
**Status:** Published on [JitPack](https://jitpack.io/#YahiaRagae/mushaf-imad-android)

---

## What's new in 0.2.2

Two crashes that only ever hit **consumers of the published library** — never the bundled
sample app, which is why they survived every internal check. Both were found by building a
real third-party app against `0.2.1` from JitPack.

- **The app died the instant audio started.** `AudioPlaybackService` sets `WAKE_MODE_NETWORK`
  on the player, which needs `WAKE_LOCK` — but the library never declared it, so the host
  crashed with `SecurityException: ... android.permission.WAKE_LOCK`. It went unnoticed
  because the bundled sample declared the permission itself. The library now declares it and
  it is merged into the host automatically; **you do not need to add it**.
- **Reading history always threw.** `recordReadingSession()` silently dropped its `verseNumber`
  and `mushafType` before writing, so every row was stored with an empty mushaf type, and
  reading one back did `MushafType.valueOf("")` and threw. This was latent until 0.2.1 — which
  started recording sessions automatically — at which point any app displaying reading history
  crashed on its first visit. Rows written by 0.2.1 no longer crash on upgrade.

Also: corrected the reciter list in this README (it named people the library does not actually
ship), and the Gradle/AGP versions.

## What's new in 0.2.1

The public API is unchanged — v0.1 code compiles as-is.

### 16 KB page size ([#73](https://github.com/YahiaRagae/mushaf-imad-android/issues/73))

Android 15+ devices can use 16 KB memory pages, and Google Play requires apps targeting Android 15+
to support them. Three native libraries were still built for 4 KB pages, which forced the app into
page-size compatibility mode and warned the user on every launch. All three are now aligned:
Realm 3.0.0, DataStore 1.1.7, graphics-path 1.1.0.

Realm 3.0.0 requires Kotlin ≥ 2.0.20, so the library is now built with **Kotlin 2.0.21**. The bundled
`quran.realm` is unchanged and still shared with the iOS library.

### Fixed

- **Crash on the second launch.** The database was being deleted and recopied from assets on every
  start, by two racing service instances (`RLM_ERR_MISMATCHED_CONFIG`).
- **All user data was wiped on every launch.** Bookmarks, reading history and the last read position
  lived in the file that was being deleted. They now live in a separate `userdata.realm` that is never
  deleted.
- **Zero-configuration setup crashed host apps that use Koin themselves.** The library called
  `startKoin` on the global Koin context; it now runs an isolated one.
- **Search.** Stale results could overwrite newer ones while typing, and the search box emptied itself
  after tapping a result while the results stayed on screen.
- **The reading position was never actually saved.**
- **The reader never followed the recitation.** The page did not turn, and the highlight vanished as
  soon as the recited verse was off-page — which also made the next/previous verse buttons appear dead.
- **Seeking to the start did not return to the chapter opening.** The basmala is recited but is not a
  numbered verse.
- **Paging the reader restarted the chapter from its first verse.**
- **Audio kept playing after the app was swiped out of recents.**
- **`getSajdaVerses()` always returned an empty list.**
- **Page turning** is now animated, with neighbouring pages preloaded
  ([#70](https://github.com/YahiaRagae/mushaf-imad-android/issues/70)).
- **Player state** comes from listener events instead of a 100 ms polling loop that ran forever.

### Added

- **Reading statistics now work** ([#75](https://github.com/YahiaRagae/mushaf-imad-android/issues/75)).
  Nothing ever recorded a reading session, so reading time, streak and chapters-read were empty for
  every consumer. The reader now times real dwell per page.

---

### Completed Features
- Page navigation (604 pages) with image-based Mushaf rendering
- Verse highlighting, selection, and fasel decorations
- Multiple reading themes and color schemes
- Search functionality (verses and chapters)
- Reading position and history persistence
- Preferences management (mushaf type, font size, theme, audio settings)
- Background audio playback with lock screen and notification controls
- 18 reciters with real-time verse highlighting
- Playback controls (play/pause, seek, speed, repeat)
- Reciter selection dialog (public API)
- Core data access (chapters, verses, pages, juz, hizb)
- Data export/import (JSON)
- Zero-configuration auto-initialization via ContentProvider
- JitPack publishing

---

## QA Testing

The library is currently undergoing QA validation. Test cases are tracked as [GitHub Issues](https://github.com/YahiaRagae/mushaf-imad-android/issues?q=label%3AQA).

| # | Area | Module |
|---|------|--------|
| QA-1 | Library Initialization & Repository Access | `mushaf-core` |
| QA-2 | MushafView Composable Integration | `mushaf-ui` |
| QA-3 | QuranPlayerView Composable Integration | `mushaf-ui` |
| QA-4 | SearchView Composable Integration | `mushaf-ui` |
| QA-5 | ReciterPickerDialog Composable Integration | `mushaf-ui` |
| QA-6 | Core Data Repositories (Chapters, Verses, Pages, Quran) | `mushaf-core` |
| QA-7 | ReadingHistoryRepository & PreferencesRepository API | `mushaf-core` |
| QA-8 | AudioRepository API & Background Playback | `mushaf-core` |

---

## Repository

**GitHub:** [https://github.com/YahiaRagae/mushaf-imad-android](https://github.com/YahiaRagae/mushaf-imad-android)

---

## Credits

Developed with care for the Muslim community.

**Based on:**
- iOS MushafImad library by [Ibrahim Qraiqe (ibo2001)](https://github.com/ibo2001/MushafImad)
- Quran Android by [Quran.com](https://github.com/quran/quran_android)

**Acknowledgments:**
- Quran text and metadata
- Audio recitations from various reciters
- Open source libraries: Jetpack Compose, Realm, ExoPlayer, Koin

---

**Last Updated:** July 2026
**Current Version:** 0.2.2
**Status:** Stable - Production Ready
**Published:** [JitPack](https://jitpack.io/#YahiaRagae/mushaf-imad-android)
