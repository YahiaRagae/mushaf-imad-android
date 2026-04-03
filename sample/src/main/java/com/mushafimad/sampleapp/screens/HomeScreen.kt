package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mushafimad.sampleapp.component.DemoListItem
import com.mushafimad.sampleapp.component.SectionHeader
import com.mushafimad.sampleapp.modle.DemoItem
import com.mushafimad.sampleapp.modle.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val coreItems = listOf(
        DemoItem(
            title = "Chapters",
            subtitle = "ChapterRepository - All 114 surahs",
            icon = Icons.Default.List,
            route = Screen.ChaptersData.route
        ),
        DemoItem(
            title = "Verses",
            subtitle = "VerseRepository - Ayat data & search",
            icon = Icons.Default.Menu,
            route = Screen.VersesData.route
        ),
        DemoItem(
            title = "Reciters",
            subtitle = "AudioRepository - 18 available reciters",
            icon = Icons.Default.Person,
            route = Screen.RecitersData.route
        ),
        DemoItem(
            title = "Bookmarks",
            subtitle = "BookmarkRepository - Save & manage",
            icon = Icons.Default.Favorite,
            route = Screen.BookmarksData.route
        ),
        DemoItem(
            title = "Reading History",
            subtitle = "ReadingHistoryRepository - Stats & tracking",
            icon = Icons.Default.DateRange,
            route = Screen.ReadingHistoryData.route
        ),
        DemoItem(
            title = "Preferences",
            subtitle = "PreferencesRepository - User settings",
            icon = Icons.Default.Settings,
            route = Screen.PreferencesData.route
        )
    )

    val uiItems = listOf(
        DemoItem(
            title = "MushafView",
            subtitle = "Basic Quran page reader",
            icon = Icons.Default.Home,
            route = Screen.MushafReader.route
        ),
        DemoItem(
            title = "MushafWithPlayerView",
            subtitle = "Mushaf with integrated audio player",
            icon = Icons.Default.PlayArrow,
            route = Screen.MushafWithAudio.route
        ),
        DemoItem(
            title = "SearchView",
            subtitle = "Search verses and chapters",
            icon = Icons.Default.Search,
            route = Screen.SearchDemo.route
        ),
        DemoItem(
            title = "Theme System",
            subtitle = "ReadingTheme & ColorScheme customization",
            icon = Icons.Default.Build,
            route = Screen.ThemeCustomization.route
        ),
        DemoItem(
            title = "ReciterPickerDialog",
            subtitle = "Reciter selection dialog",
            icon = Icons.Default.AccountCircle,
            route = Screen.ReciterPickerDemo.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MushafImad Library") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Core Library section
            item {
                SectionHeader(
                    title = "Core Library",
                    subtitle = "mushaf-core: Data layer & repositories"
                )
            }
            items(coreItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }

            item { Spacer(modifier = Modifier.Companion.height(16.dp)) }

            // UI Library section
            item {
                SectionHeader(
                    title = "UI Library",
                    subtitle = "mushaf-ui: Jetpack Compose components"
                )
            }
            items(uiItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}