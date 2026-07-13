@file:Suppress("unused")

package com.mushafimad.app

// ---------------------------------------------------------------------------
// The four "Usage Examples" from the library README, copied VERBATIM, to check
// that they still compile against 0.2.2. Extra imports the README omits
// (Modifier / fillMaxSize / remember...) are added below - the README snippets
// themselves do not list them.
// ---------------------------------------------------------------------------

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.theme.ReadingTheme
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.ui.mushaf.MushafWithPlayerView
import com.mushafimad.ui.search.SearchView

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

@Composable
fun SearchScreenSnippet() {
    var currentPage by remember { mutableStateOf<Int?>(null) }

    if (currentPage != null) {
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
