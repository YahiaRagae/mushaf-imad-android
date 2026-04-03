package com.mushafimad.sampleapp.component

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mushafimad.sampleapp.modle.Screen
import com.mushafimad.sampleapp.screens.BookmarksDataScreen
import com.mushafimad.sampleapp.screens.ChaptersDataScreen
import com.mushafimad.sampleapp.screens.HomeScreen
import com.mushafimad.sampleapp.screens.MushafReaderScreen
import com.mushafimad.sampleapp.screens.MushafWithAudioScreen
import com.mushafimad.sampleapp.screens.PreferencesDataScreen
import com.mushafimad.sampleapp.screens.ReadingHistoryDataScreen
import com.mushafimad.sampleapp.screens.ReciterPickerDemoScreen
import com.mushafimad.sampleapp.screens.RecitersDataScreen
import com.mushafimad.sampleapp.screens.SearchDemoScreen
import com.mushafimad.sampleapp.screens.ThemeCustomizationScreen
import com.mushafimad.sampleapp.screens.VersesDataScreen

// ============================================================================
// MARK: - Home Screen (Two Sections: Core & UI)
// ============================================================================


@Composable
fun SampleAppContent() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Core Library demos
        composable(Screen.ChaptersData.route) {
            ChaptersDataScreen(navController = navController)
        }
        composable(Screen.VersesData.route) {
            VersesDataScreen(navController = navController)
        }
        composable(Screen.RecitersData.route) {
            RecitersDataScreen(navController = navController)
        }
        composable(Screen.BookmarksData.route) {
            BookmarksDataScreen(navController = navController)
        }
        composable(Screen.ReadingHistoryData.route) {
            ReadingHistoryDataScreen(navController = navController)
        }
        composable(Screen.PreferencesData.route) {
            PreferencesDataScreen(navController = navController)
        }

        // UI Library demos
        composable(Screen.MushafReader.route) {
            MushafReaderScreen(navController = navController)
        }
        composable(Screen.MushafWithAudio.route) {
            MushafWithAudioScreen(navController = navController)
        }
        composable(Screen.SearchDemo.route) {
            SearchDemoScreen(navController = navController)
        }
        composable(Screen.ThemeCustomization.route) {
            ThemeCustomizationScreen(navController = navController)
        }
        composable(Screen.ReciterPickerDemo.route) {
            ReciterPickerDemoScreen(navController = navController)
        }
    }
}