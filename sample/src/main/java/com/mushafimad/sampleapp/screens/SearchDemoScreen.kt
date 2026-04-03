package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.search.SearchView
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDemoScreen(navController: NavHostController) {
    val verseRepository = remember { MushafLibrary.getVerseRepository() }
    var currentPage by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    if (currentPage != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Page $currentPage") },
                    navigationIcon = {
                        IconButton(onClick = { currentPage = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to search")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.Companion.padding(paddingValues)) {
                MushafView(
                    readingTheme = ReadingTheme.COMFORTABLE,
                    colorScheme = ColorSchemeType.DEFAULT,
                    mushafType = MushafType.HAFS_1441,
                    initialPage = currentPage,
                    showNavigationControls = true,
                    showPageInfo = true,
                    modifier = Modifier.Companion.fillMaxSize()
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SearchView") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.Companion.padding(paddingValues)) {
                SearchView(
                    onVerseSelected = { verse ->
                        currentPage = verse.pageNumber
                    },
                    onChapterSelected = { chapter ->
                        coroutineScope.launch {
                            val firstVerse = verseRepository.getVerse(chapter.number, 1)
                            currentPage = firstVerse?.pageNumber ?: 1
                        }
                    },
                    onDismiss = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.Companion.fillMaxSize()
                )
            }
        }
    }
}