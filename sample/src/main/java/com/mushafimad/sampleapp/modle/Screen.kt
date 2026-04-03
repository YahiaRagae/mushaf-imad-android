package com.mushafimad.sampleapp.modle

// Navigation routes
sealed class Screen(val route: String) {
    object Home : Screen("home")

    // Core Library demos
    object ChaptersData : Screen("core_chapters")
    object VersesData : Screen("core_verses")
    object RecitersData : Screen("core_reciters")
    object BookmarksData : Screen("core_bookmarks")
    object ReadingHistoryData : Screen("core_history")
    object PreferencesData : Screen("core_preferences")

    // UI Library demos
    object MushafReader : Screen("ui_mushaf")
    object MushafWithAudio : Screen("ui_mushaf_audio")
    object SearchDemo : Screen("ui_search")
    object ThemeCustomization : Screen("ui_theme")
    object ReciterPickerDemo : Screen("ui_reciter_picker")
}