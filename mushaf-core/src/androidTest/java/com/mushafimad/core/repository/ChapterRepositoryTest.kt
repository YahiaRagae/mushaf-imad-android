package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.ChapterRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ChapterRepository (Surah data).
 *
 * Covers: QA-6.1 (ChapterRepository API) — Issue #21
 *
 * TC-6.1   getAllChapters() returns exactly 114 chapters
 * TC-6.2   getChapter(1) returns Al-Fatiha (7 verses, Meccan)
 * TC-6.3   getChapter(114) returns An-Nas
 * TC-6.4   getChapter(0) returns null (below boundary)
 * TC-6.5   getChapter(115) returns null (above boundary)
 * TC-6.6   getChapterForPage(1) returns Al-Fatiha
 * TC-6.7   getChaptersOnPage() returns multiple chapters for a multi-surah page
 * TC-6.8   searchChapters("بقرة") finds Al-Baqarah
 * TC-6.9   searchChapters("baqarah") English search finds Al-Baqarah
 * TC-6.10  searchChapters("nonexistent") returns empty list
 * TC-6.11  getAllChaptersFlow() emits a list of 114 chapters
 * TC-6.12  getChaptersByPart() returns chapters grouped by Juz
 * TC-6.13  getChaptersByType() returns Meccan and Medinan groups
 * TC-6.14  Chapter.getDisplayTitle("ar") returns Arabic title
 * TC-6.15  Chapter.getDisplayTitle("en") returns English title
 */
@RunWith(AndroidJUnit4::class)
class ChapterRepositoryTest {

    private lateinit var repository: ChapterRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getChapterRepository()
    }

    // ──────────────────────────── TC-6.1 ────────────────────────────

    @Test
    fun getAllChapters_returnsExactly114Chapters() = runTest {
        val chapters = repository.getAllChapters()
        assertThat(chapters).hasSize(114)
    }

    // ──────────────────────────── TC-6.2 ────────────────────────────

    @Test
    fun getChapter1_returnsAlFatihaWith7Verses() = runTest {
        val chapter = repository.getChapter(1)

        assertThat(chapter).isNotNull()
        assertThat(chapter!!.number).isEqualTo(1)
        assertThat(chapter.versesCount).isEqualTo(7)
        assertThat(chapter.isMeccan).isTrue()
        // Arabic title must be non-empty and contain expected text
        assertThat(chapter.arabicTitle).isNotEmpty()
    }

    // ──────────────────────────── TC-6.3 ────────────────────────────

    @Test
    fun getChapter114_returnsAnNas() = runTest {
        val chapter = repository.getChapter(114)

        assertThat(chapter).isNotNull()
        assertThat(chapter!!.number).isEqualTo(114)
        // An-Nas has 6 verses and is Meccan
        assertThat(chapter.versesCount).isEqualTo(6)
    }

    // ──────────────────────────── TC-6.4 ────────────────────────────

    @Test
    fun getChapter_returnsNullForChapter0() = runTest {
        assertThat(repository.getChapter(0)).isNull()
    }

    // ──────────────────────────── TC-6.5 ────────────────────────────

    @Test
    fun getChapter_returnsNullForChapter115() = runTest {
        assertThat(repository.getChapter(115)).isNull()
    }

    // ──────────────────────────── TC-6.6 ────────────────────────────

    @Test
    fun getChapterForPage1_returnsAlFatiha() = runTest {
        val chapter = repository.getChapterForPage(1)

        assertThat(chapter).isNotNull()
        assertThat(chapter!!.number).isEqualTo(1)
    }

    // ──────────────────────────── TC-6.7 ────────────────────────────

    @Test
    fun getChaptersOnPage_returnsMultipleChaptersForMultiSurahPage() = runTest {
        // Scan a range of pages to find one that spans at least two surahs.
        // This exists in every Quran layout between pages 2 and 20.
        var multiChapterPage = -1
        for (page in 2..30) {
            if (repository.getChaptersOnPage(page).size >= 2) {
                multiChapterPage = page
                break
            }
        }

        assertThat(multiChapterPage).isGreaterThan(0)

        val chapters = repository.getChaptersOnPage(multiChapterPage)
        assertThat(chapters.size).isAtLeast(2)
    }

    // ──────────────────────────── TC-6.8 ────────────────────────────

    @Test
    fun searchChapters_arabicQuery_findsAlBaqarah() = runTest {
        val results = repository.searchChapters("بقرة")

        assertThat(results).isNotEmpty()
        assertThat(results.any { it.number == 2 }).isTrue()
    }

    // ──────────────────────────── TC-6.9 ────────────────────────────

    @Test
    fun searchChapters_englishQuery_findsAlBaqarah() = runTest {
        val results = repository.searchChapters("baqarah")

        assertThat(results).isNotEmpty()
        assertThat(results.any { it.number == 2 }).isTrue()
    }

    // ──────────────────────────── TC-6.10 ────────────────────────────

    @Test
    fun searchChapters_nonExistentQuery_returnsEmptyList() = runTest {
        val results = repository.searchChapters("zzz_nonexistent_zzz")
        assertThat(results).isEmpty()
    }

    // ──────────────────────────── TC-6.11 ────────────────────────────

    @Test
    fun getAllChaptersFlow_emits114Chapters() = runTest {
        repository.getAllChaptersFlow().test {
            val chapters = awaitItem()
            assertThat(chapters).hasSize(114)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-6.12 ────────────────────────────

    @Test
    fun getChaptersByPart_returnsAllChaptersGroupedByJuz() = runTest {
        val grouped = repository.getChaptersByPart()

        assertThat(grouped).isNotEmpty()
        // All 114 chapters must appear across the groups
        val totalChapters = grouped.sumOf { it.chapters.size }
        assertThat(totalChapters).isEqualTo(114)
    }

    // ──────────────────────────── TC-6.13 ────────────────────────────

    @Test
    fun getChaptersByType_returnsMeccanAndMedinanGroups() = runTest {
        val grouped = repository.getChaptersByType()

        assertThat(grouped).isNotEmpty()
        assertThat(grouped.any { it.isMeccan }).isTrue()
        assertThat(grouped.any { !it.isMeccan }).isTrue()

        // All 114 chapters must appear across both groups
        val totalChapters = grouped.sumOf { it.chapters.size }
        assertThat(totalChapters).isEqualTo(114)
    }

    // ──────────────────────────── TC-6.14 + TC-6.15 ────────────────────────────

    @Test
    fun chapter_getDisplayTitle_returnsArabicTitleForArLocale() = runTest {
        val chapter = repository.getChapter(1)!!
        assertThat(chapter.getDisplayTitle("ar")).isEqualTo(chapter.arabicTitle)
    }

    @Test
    fun chapter_getDisplayTitle_returnsEnglishTitleForEnLocale() = runTest {
        val chapter = repository.getChapter(1)!!
        assertThat(chapter.getDisplayTitle("en")).isEqualTo(chapter.englishTitle)
    }
}
