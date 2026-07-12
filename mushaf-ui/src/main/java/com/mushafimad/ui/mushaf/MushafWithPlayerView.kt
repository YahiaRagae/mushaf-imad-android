package com.mushafimad.ui.mushaf

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mushafimad.ui.internal.mushafViewModel
import com.mushafimad.core.MushafLibrary
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

    // Get current chapter info for player
    val currentChapter = mushafUiState.chapters.firstOrNull()
    val chapterName = currentChapter?.arabicTitle ?: ""
    val chapterNumber = currentChapter?.number ?: 1

    val verses = mushafUiState.verses
    val currentVerseNumber by playerViewModel.currentVerseNumber.collectAsState()

    // Follow the recitation: resolve the verse being recited and turn the page
    // to whichever page holds it. Without this the reader sits on the page it
    // happened to start on while the audio runs away from it, and the highlight
    // silently disappears as soon as the verse is off-page.
    //
    // The chapter is read from the player, not from the page: once the page
    // starts following along it can land on a page that opens with the tail of
    // the previous surah, and resolving the verse against that surah would send
    // the reader somewhere else entirely.
    val verseRepository = remember { MushafLibrary.getVerseRepository() }
    var recitedVerse by remember { mutableStateOf<Verse?>(null) }

    LaunchedEffect(playerViewModel, currentVerseNumber) {
        val verseNumber = currentVerseNumber
        if (verseNumber == null || verseNumber <= 0) {
            recitedVerse = null
            return@LaunchedEffect
        }

        val recitingChapter = playerViewModel.getChapterInfo().number
        if (recitingChapter <= 0) return@LaunchedEffect

        val verse = verseRepository.getVerse(recitingChapter, verseNumber) ?: return@LaunchedEffect
        recitedVerse = verse

        if (verse.pageNumber > 0 && verse.pageNumber != mushafViewModel.uiState.value.currentPage) {
            mushafViewModel.goToPage(verse.pageNumber)
        }
    }

    // Highlight the recited verse using the page's own instance, so it compares
    // equal to what the page renders. Matched on chapter AND verse number: a
    // page can hold the end of one surah and the start of the next, where two
    // different verses share a number.
    val highlightedVerse: Verse? = remember(verses, recitedVerse) {
        val recited = recitedVerse ?: return@remember null
        verses.find { it.chapterNumber == recited.chapterNumber && it.number == recited.number }
    }

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
