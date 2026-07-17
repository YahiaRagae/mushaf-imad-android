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
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.ui.player.FIRST_VERSE
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
    pageSwipeEnabled: Boolean = true,
    onVerseSelected: ((Verse) -> Unit)? = null,
    onVerseLongPress: ((Verse) -> Unit)? = null,
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

    // Which surah the player is on. It normally tracks the surah the reader is
    // looking at, so pressing play recites what you can see. While a recitation
    // is actually running, though, it stays put: the reader follows the audio
    // across pages, and letting those page changes feed back into the player
    // would reconfigure it and restart the chapter from its first verse.
    val playbackState by playerViewModel.playbackState.collectAsState()
    val pageChapter = mushafUiState.chapters.firstOrNull()

    var chapterNumber by remember { mutableStateOf(pageChapter?.number ?: 1) }
    var chapterName by remember { mutableStateOf(pageChapter?.arabicTitle ?: "") }

    LaunchedEffect(pageChapter?.number, playbackState) {
        val reciting =
            playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.LOADING
        pageChapter
            ?.takeUnless { reciting }
            ?.let {
                chapterNumber = it.number
                chapterName = it.arabicTitle
            }
    }

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
        val verseNumber = currentVerseNumber ?: run {
            recitedVerse = null
            return@LaunchedEffect
        }

        val recitingChapter = playerViewModel.getChapterInfo().number.takeIf { it > 0 }
            ?: return@LaunchedEffect

        // Verse 0 is the opening basmala: it is recited but is not a verse of
        // the chapter. Take the reader to where the chapter starts, and
        // highlight nothing until the first verse is actually reached.
        val isChapterOpening = verseNumber < FIRST_VERSE
        val target = verseNumber.coerceAtLeast(FIRST_VERSE)

        verseRepository.getVerse(recitingChapter, target)?.let { verse ->
            recitedVerse = verse.takeUnless { isChapterOpening }

            if (verse.pageNumber > 0 && verse.pageNumber != mushafViewModel.uiState.value.currentPage) {
                mushafViewModel.goToPage(verse.pageNumber)
            }
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
                pageSwipeEnabled = pageSwipeEnabled,
                onVerseSelected = onVerseSelected,
                onVerseLongPress = onVerseLongPress,
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
