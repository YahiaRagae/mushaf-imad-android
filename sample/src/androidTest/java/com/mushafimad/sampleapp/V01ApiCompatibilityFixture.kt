package com.mushafimad.sampleapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.mushaf.MushafWithPlayerView
import com.mushafimad.ui.player.QuranPlayerView
import com.mushafimad.ui.player.ReciterPickerDialog
import com.mushafimad.ui.search.SearchView
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme

/**
 * FROZEN public-API compatibility fixture.
 *
 * This file reproduces, verbatim, how a v0.1 consumer calls the library. It is
 * never executed - the guarantee is that it must keep COMPILING. If any public
 * signature changes shape (a parameter is renamed, reordered, retyped, made
 * required, or removed), this file stops compiling and CI fails.
 *
 * That is the real "the test app still works with no changes" oracle: the
 * sample app itself is free to evolve its demos, while this frozen snapshot of
 * v0.1 usage keeps the API contract honest. It complements
 * binary-compatibility-validator (which guards the .api dumps) by proving
 * SOURCE compatibility from a consumer's point of view.
 *
 * DO NOT "modernize" this file. Only add to it when a new public API ships.
 */
@Suppress("unused", "UNUSED_PARAMETER")
private object V01ApiCompatibilityFixture {

    // ---- Repository accessors: all 10, exactly as v0.1 exposed them ----
    fun repositoryAccessors() {
        MushafLibrary.getQuranRepository()
        MushafLibrary.getChapterRepository()
        MushafLibrary.getPageRepository()
        MushafLibrary.getVerseRepository()
        MushafLibrary.getBookmarkRepository()
        MushafLibrary.getReadingHistoryRepository()
        MushafLibrary.getSearchHistoryRepository()
        MushafLibrary.getAudioRepository()
        MushafLibrary.getPreferencesRepository()
        MushafLibrary.getDataExportRepository()

        MushafLibrary.isInitialized()
    }

    // ---- MushafView: the exact v0.1 call shape (initialPage as Int) ----
    @Composable
    fun mushafView() {
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

    // ---- MushafView with every optional parameter a v0.1 consumer could pass ----
    @Composable
    fun mushafViewFull(highlighted: Verse?) {
        MushafView(
            readingTheme = ReadingTheme.NIGHT,
            colorScheme = ColorSchemeType.SEPIA,
            mushafType = MushafType.HAFS_1405,
            initialPage = null,
            highlightedVerse = highlighted,
            showNavigationControls = false,
            showPageInfo = false,
            onVerseSelected = { verse: Verse -> verse.pageNumber },
            onPageChanged = { page: Int -> page },
            modifier = Modifier
        )
    }

    // ---- MushafView with no arguments at all (every param must stay defaulted) ----
    @Composable
    fun mushafViewDefaults() {
        MushafView()
    }

    @Composable
    fun mushafWithPlayerView() {
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
    fun quranPlayerView() {
        QuranPlayerView(
            chapterNumber = 1,
            chapterName = "الفاتحة",
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun quranPlayerViewFull() {
        QuranPlayerView(
            chapterNumber = 2,
            chapterName = "البقرة",
            reciterId = 1,
            autoPlay = false,
            onPreviousVerse = { },
            onNextVerse = { },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun searchView() {
        SearchView(
            onVerseSelected = { verse: Verse -> verse.pageNumber },
            onChapterSelected = { chapter: Chapter -> chapter.number },
            onDismiss = { },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun reciterPickerDialog(reciters: List<ReciterInfo>, selected: ReciterInfo?) {
        ReciterPickerDialog(
            reciters = reciters,
            selectedReciter = selected,
            onReciterSelected = { reciter: ReciterInfo -> reciter.id },
            onDismiss = { }
        )
    }

    // ---- Enums a v0.1 consumer switched on ----
    fun enums() {
        val themes: List<ReadingTheme> = listOf(
            ReadingTheme.COMFORTABLE,
            ReadingTheme.CALM,
            ReadingTheme.NIGHT,
            ReadingTheme.WHITE
        )
        val schemes: List<ColorSchemeType> = listOf(
            ColorSchemeType.DEFAULT,
            ColorSchemeType.WARM,
            ColorSchemeType.COOL,
            ColorSchemeType.SEPIA
        )
        val types: List<MushafType> = listOf(MushafType.HAFS_1441, MushafType.HAFS_1405)
        check(themes.isNotEmpty() && schemes.isNotEmpty() && types.isNotEmpty())
    }
}
