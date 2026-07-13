# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build individual library modules
./gradlew :mushaf-core:assembleDebug
./gradlew :mushaf-ui:assembleDebug

# Run the app against the library from source (the dev loop)
./gradlew :app:installSourceDebug

# Run tests
./gradlew testDebugUnitTest                      # All tests
./gradlew :mushaf-core:testDebugUnitTest         # Core module only
./gradlew :mushaf-ui:testDebugUnitTest           # UI module only

# Build release
./gradlew assembleRelease
```

> Don't run a blanket `./gradlew assembleDebug` / `assembleRelease` at the
> root while iterating: `:app` has two product flavours (see below) and the
> `published` one needs a mavenLocal publish or network access to JitPack.
> Target `:app:assembleSourceDebug` (or `installSourceDebug`) instead.

## Architecture

This is a **Quran reader Android library** with a three-module Clean Architecture design:

```
mushaf-core/     → Headless data layer (NO Compose dependencies)
    ├── domain/  → Public API: models, repository interfaces
    ├── data/    → Implementations: repositories, DAOs, services
    └── di/      → Koin module (CoreModule.kt)

mushaf-ui/       → Jetpack Compose UI (depends on mushaf-core)
    ├── mushaf/  → Main reader: MushafView, MushafViewModel
    ├── player/  → Audio: QuranPlayerView, QuranPlayerViewModel
    ├── search/  → SearchView, SearchViewModel
    └── di/      → Koin ViewModels (UiModule.kt)

app/             → Quran reader app, built in two flavours (see below)
```

**Dependency flow:** `app → mushaf-ui → mushaf-core`

### `:app` product flavours

`:app` builds the exact same app code against two different spellings of the
library, so any gap between "works in this repo" and "works for a real
consumer" shows up as a build/test failure here instead of as a bug report:

- **`source`** — depends on `project(":mushaf-ui")`. This is the day-to-day
  dev loop: edit `mushaf-core`/`mushaf-ui` and `:app:installSourceDebug`
  picks the change up immediately, no publish step.
- **`published`** — depends on the library by Maven coordinate
  (`com.github.YahiaRagae.mushaf-imad-android:mushaf-ui:<VERSION_NAME>`),
  resolved from **mavenLocal** before a release and from **JitPack** after
  one. This is the gate that proves what we actually ship works: run
  `./gradlew :mushaf-core:publishToMavenLocal :mushaf-ui:publishToMavenLocal`
  then `:app:assemblePublishedDebug`/`installPublishedDebug` to build the app
  the way a third-party consumer would, against exactly what would be
  published. Override the version with `-PmushafVersion=<v>` to build
  against any already-released version instead of this checkout's own.

### Key Patterns

- **Repository Pattern:** Interface in `domain/repository/`, implementation in `data/repository/` with `Default` prefix
- **DI:** Koin (runtime, no code generation) - register singletons in `CoreModule.kt`, ViewModels in `UiModule.kt`
- **Auto-initialization:** ContentProvider (`MushafInitProvider`) starts Koin before `Application.onCreate()` - zero configuration required
- **State Management:** Kotlin Flow throughout, `StateFlow<T>` for UI state
- **Database:** Realm Kotlin with entities in `data/local/entities/`

### Adding a New Feature

1. Domain model → `mushaf-core/domain/models/`
2. Repository interface → `mushaf-core/domain/repository/`
3. Repository implementation → `mushaf-core/data/repository/Default*.kt`
4. Register in Koin → `mushaf-core/di/CoreModule.kt`
5. ViewModel → `mushaf-ui/` with Koin injection
6. Composable → access ViewModel via `koinViewModel()`

## Key Technologies

| Component | Library |
|-----------|---------|
| UI | Jetpack Compose (BOM 2024.12.01) |
| Database | Realm Kotlin 1.16.0 |
| Audio | Media3/ExoPlayer 1.5.0 |
| DI | Koin 3.5.6 |
| Async | Kotlin Coroutines + Flow |

## Important Files

- `gradle/libs.versions.toml` - Centralized dependency versions
- `mushaf-core/di/CoreModule.kt` - All repository/service singletons
- `mushaf-ui/di/UiModule.kt` - All ViewModel definitions
- `mushaf-core/internal/MushafInitProvider.kt` - Auto-initialization
- `mushaf-core/data/audio/AudioPlaybackService.kt` - Background playback (MediaSessionService)

## Notes

- **Min SDK 24**, Target SDK 35, Kotlin 1.9.25, Java 17
- Pre-populated Realm database at `assets/quran.realm` (schema version 24)
- 18 reciters defined in `ReciterService.kt`
- Image assets: `assets/quran-images/[page]/[line].png`
