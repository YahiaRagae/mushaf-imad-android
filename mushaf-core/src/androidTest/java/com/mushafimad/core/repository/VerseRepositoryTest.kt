package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.repository.VerseRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for VerseRepository (Ayah data).
 *
 * Covers: QA-6.2 (VerseRepository API) — Issue #22
 *
 * TC-6.16  getVersesForPage(1) returns verses for page 1
 * TC-6.17  getVersesForPage(604) returns verses for the last page
 * TC-6.18  getVersesForChapter(1) returns 7 verses (Al-Fatiha)
 * TC-6.19  getVerse(2, 255) returns Ayat Al-Kursi with Arabic text
 * TC-6.20  getVerse(0, 0) returns null (invalid reference)
 * TC-6.21  getSajdaVerses() returns known sajda verses
 * TC-6.22  searchVerses("بسم الله") returns matching verses
 * TC-6.23  getVersesForPage(page, HAFS_1441) differs from HAFS_1405
 */
@RunWith(AndroidJUnit4::class)
class VerseRepositoryTest {

    private lateinit var repository: VerseRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getVerseRepository()
    }

    // ──────────────────────────── TC-6.16 ────────────────────────────

    @Test
    fun getVersesForPage1_returnsNonEmptyList() = runTest {
        val verses = repository.getVersesForPage(1)
        assertThat(verses).isNotEmpty()
    }

    @Test
    fun getVersesForPage1_versesHaveCorrectChapterNumber() = runTest {
        val verses = repository.getVersesForPage(1)
        // Page 1 in HAFS_1441 belongs to Al-Fatiha (chapter 1)
        assertThat(verses.all { it.chapterNumber == 1 }).isTrue()
    }

    // ──────────────────────────── TC-6.17 ────────────────────────────

    @Test
    fun getVersesForPage604_returnsNonEmptyList() = runTest {
        val verses = repository.getVersesForPage(604)
        assertThat(verses).isNotEmpty()
    }

    // ──────────────────────────── TC-6.18 ────────────────────────────

    @Test
    fun getVersesForChapter1_returnsExactly7Verses() = runTest {
        val verses = repository.getVersesForChapter(1)
        assertThat(verses).hasSize(7)
    }

    @Test
    fun getVersesForChapter1_versesAreOrderedByNumber() = runTest {
        val verses = repository.getVersesForChapter(1)
        val numbers = verses.map { it.number }
        assertThat(numbers).isInOrder()
    }

    // ──────────────────────────── TC-6.19 ────────────────────────────

    @Test
    fun getVerse_returnsAyatAlKursiWithArabicText() = runTest {
        val verse = repository.getVerse(2, 255)

        assertThat(verse).isNotNull()
        assertThat(verse!!.chapterNumber).isEqualTo(2)
        assertThat(verse.number).isEqualTo(255)
        // Ayat Al-Kursi must contain the known opening word "اللَّهُ"
        assertThat(verse.text).contains("الله")
    }

    // ──────────────────────────── TC-6.20 ────────────────────────────

    @Test
    fun getVerse_returnsNullForInvalidChapter0Verse0() = runTest {
        assertThat(repository.getVerse(0, 0)).isNull()
    }

    @Test
    fun getVerse_returnsNullForNonExistentChapter999() = runTest {
        assertThat(repository.getVerse(999, 1)).isNull()
    }

    // ──────────────────────────── TC-6.21 ────────────────────────────

    @Test
    fun getSajdaVerses_returnsKnownSajdaVerses() = runTest {
        val sajdaVerses = repository.getSajdaVerses()

        // There are 15 sajda (prostration) verses in the Quran
        assertThat(sajdaVerses).isNotEmpty()
        assertThat(sajdaVerses.size).isEqualTo(15)
    }

    // ──────────────────────────── TC-6.22 ────────────────────────────

    @Test
    fun searchVerses_arabicBismillahQuery_returnsMatches() = runTest {
        val results = repository.searchVerses("بسم الله")

        assertThat(results).isNotEmpty()
        // Every match must contain the query text in its searchable representation
        assertThat(results.all { verse ->
            verse.text.contains("الله") || verse.searchableText.contains("الله")
        }).isTrue()
    }

    // ──────────────────────────── TC-6.23 ────────────────────────────

    @Test
    fun getVersesForPage_returnsDifferentSetsForDifferentMushafTypes() = runTest {
        // Use a page deep enough in the Quran where the two layouts diverge.
        // Page 100 is in the middle of Al-Baqarah/Al-Imran where layouts differ.
        val verses1441 = repository.getVersesForPage(100, MushafType.HAFS_1441)
        val verses1405 = repository.getVersesForPage(100, MushafType.HAFS_1405)

        assertThat(verses1441).isNotEmpty()
        assertThat(verses1405).isNotEmpty()
        // The two layouts assign different verse sets to the same page number
        assertThat(verses1441).isNotEqualTo(verses1405)
    }
}
