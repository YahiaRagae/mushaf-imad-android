package com.mushafimad.ui.mushaf

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
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
    val mushafViewModel: MushafViewModel = koinViewModel()
    val playerViewModel: QuranPlayerViewModel = koinViewModel()

    val mushafUiState by mushafViewModel.uiState.collectAsState()
    val currentVerseNumber by playerViewModel.currentVerseNumber.collectAsState()

    // Track highlighted verse
    var highlightedVerse by remember { mutableStateOf<Verse?>(null) }

    // Update highlighted verse when audio plays
    LaunchedEffect(currentVerseNumber, mushafUiState.verses) {
        if (currentVerseNumber != null && currentVerseNumber!! > 0) {
            // Find verse by number on current page
            val verse = mushafUiState.verses.find { it.number == currentVerseNumber }
            highlightedVerse = verse
        } else {
            highlightedVerse = null
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
                onPageChanged = { page ->
                    onPageChanged?.invoke(page)
                },
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
                    onPreviousVerse = {
                        // Navigate to previous verse
                        playerViewModel.seekToPreviousVerse()
                    },
                    onNextVerse = {
                        // Navigate to next verse
                        playerViewModel.seekToNextVerse()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
