package com.mushafimad.ui.mushaf

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushafimad.ui.internal.mushafViewModel
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.player.QuranPlayerView
import com.mushafimad.ui.player.QuranPlayerViewModel
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme

/**
 * Integrated view combining Mushaf page display with audio player
 * Automatically highlights verses during audio playback
 * Public API - exposed to library consumers
 *
 * @param readingTheme The reading theme (background/text colors)
 * @param colorScheme The color scheme for UI elements
 * @param mushafType The Mushaf layout type
 * @param initialPage Initial page to display
 * @param showNavigationControls Show next/previous page buttons
 * @param showPageInfo Show page/juz information
 * @param showAudioPlayer Show audio player controls
 * @param onVerseSelected Callback when a verse is selected
 * @param onPageChanged Callback when page changes
 * @param modifier Optional modifier
 */
@Composable
fun MushafWithPlayerView(
    readingTheme: ReadingTheme = ReadingTheme.COMFORTABLE,
    colorScheme: ColorSchemeType = ColorSchemeType.DEFAULT,
    mushafType: MushafType = MushafType.HAFS_1441,
    initialPage: Int? = null,
    showNavigationControls: Boolean = true,
    showPageInfo: Boolean = true,
    showAudioPlayer: Boolean = true,
    onVerseSelected: ((Verse) -> Unit)? = null,
    onPageChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val mushafViewModel: MushafViewModel = mushafViewModel()
    val playerViewModel: QuranPlayerViewModel = mushafViewModel()

    val mushafUiState by mushafViewModel.uiState.collectAsState()

    val onPreviousVerse = remember(playerViewModel) { { playerViewModel.seekToPreviousVerse() } }
    val onNextVerse = remember(playerViewModel) { { playerViewModel.seekToNextVerse() } }
    val onPageChangedStable: ((Int) -> Unit)? = remember(onPageChanged) {
        onPageChanged?.let { cb -> { page: Int -> cb(page) } }
    }

      var highlightedVerse by remember { mutableStateOf<Verse?>(null) }

    val verses = mushafUiState.verses
    LaunchedEffect(playerViewModel, verses) {
        playerViewModel.currentVerseNumber.collect { verseNumber ->
            highlightedVerse = if (verseNumber != null && verseNumber > 0) {
                verses.find { it.number == verseNumber }
            } else {
                null
            }
        }
    }

    // Get current chapter info for player
    val currentChapter = mushafUiState.chapters.firstOrNull()
    val chapterName = currentChapter?.arabicTitle ?: ""
    val chapterNumber = currentChapter?.number ?: 1

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Mushaf view with verse highlighting
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MushafView(
                readingTheme = readingTheme,
                colorScheme = colorScheme,
                mushafType = mushafType,
                initialPage = initialPage,
                highlightedVerse = highlightedVerse,
                showNavigationControls = showNavigationControls,
                showPageInfo = showPageInfo,
                onVerseSelected = onVerseSelected,
                onPageChanged = onPageChangedStable,
                viewModel = mushafViewModel
            )
        }

        // Audio player (if enabled)
        if (showAudioPlayer) {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                QuranPlayerView(
                    chapterNumber = chapterNumber,
                    chapterName = chapterName,
                    autoPlay = false,
                    onPreviousVerse = onPreviousVerse,
                    onNextVerse = onNextVerse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
