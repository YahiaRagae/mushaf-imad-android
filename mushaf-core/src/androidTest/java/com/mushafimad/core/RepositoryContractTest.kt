package com.mushafimad.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Contract tests for the public repository APIs against the real bundled
 * database. The cases mirror the QA bounty test plan (QA-6.x data
 * repositories, QA-8.1 reciters): counts, boundaries, and null handling for
 * out-of-range input.
 */
@RunWith(AndroidJUnit4::class)
class RepositoryContractTest {

    // ---- ChapterRepository (QA-6.1) ----

    @Test
    fun chapters_countAndBoundaries() = runBlocking {
        val repo = MushafLibrary.getChapterRepository()

        assertThat(repo.getAllChapters()).hasSize(114)

        val fatiha = repo.getChapter(1)
        assertThat(fatiha).isNotNull()
        assertThat(fatiha!!.isMeccan).isTrue()
        assertThat(fatiha.versesCount).isEqualTo(7)

        val nas = repo.getChapter(114)
        assertThat(nas).isNotNull()
        assertThat(nas!!.versesCount).isEqualTo(6)

        assertThat(repo.getChapter(0)).isNull()
        assertThat(repo.getChapter(115)).isNull()
        assertThat(repo.getChapter(-1)).isNull()
    }

    @Test
    fun chapters_searchFindsResults() = runBlocking {
        val repo = MushafLibrary.getChapterRepository()
        assertThat(repo.searchChapters("الفاتحة")).isNotEmpty()
    }

    // ---- VerseRepository (QA-6.2) ----

    @Test
    fun verses_pageBoundaries() = runBlocking {
        val repo = MushafLibrary.getVerseRepository()

        assertThat(repo.getVersesForPage(1)).isNotEmpty()
        assertThat(repo.getVersesForPage(604)).isNotEmpty()
        assertThat(repo.getVersesForPage(0)).isEmpty()
        assertThat(repo.getVersesForPage(605)).isEmpty()
    }

    @Test
    fun verses_lookupAndBounds() = runBlocking {
        val repo = MushafLibrary.getVerseRepository()

        val lastVerse = repo.getVerse(114, 6)
        assertThat(lastVerse).isNotNull()

        assertThat(repo.getVerse(2, 999)).isNull()
        assertThat(repo.getVerse(0, 1)).isNull()
        assertThat(repo.getVerse(115, 1)).isNull()

        assertThat(repo.getVersesForChapter(1)).hasSize(7)
        assertThat(repo.getVersesForChapter(0)).isEmpty()
    }

    @Test
    fun verses_sajdaAndSearch() = runBlocking {
        val repo = MushafLibrary.getVerseRepository()

        assertThat(repo.getSajdaVerses()).hasSize(15)
        assertThat(repo.searchVerses("الرحمن")).isNotEmpty()
        assertThat(repo.searchVerses("xyzzy-not-in-the-quran")).isEmpty()
    }

    // ---- PageRepository (QA-6.3) ----

    @Test
    fun pages_totalAndBoundaries() = runBlocking {
        val repo = MushafLibrary.getPageRepository()

        assertThat(repo.getTotalPages()).isEqualTo(604)
        assertThat(repo.getPage(1)).isNotNull()
        assertThat(repo.getPage(604)).isNotNull()
        assertThat(repo.getPage(0)).isNull()
        assertThat(repo.getPage(605)).isNull()
    }

    // ---- QuranRepository (QA-6.4) ----

    @Test
    fun quran_partsAndQuarters() = runBlocking {
        val repo = MushafLibrary.getQuranRepository()

        assertThat(repo.getAllParts()).hasSize(30)
        assertThat(repo.getPart(1)).isNotNull()
        assertThat(repo.getPart(31)).isNull()
    }

    // ---- AudioRepository reciters (QA-8.1) ----

    @Test
    fun reciters_countAndLookup() = runBlocking {
        val repo = MushafLibrary.getAudioRepository()

        val reciters = repo.getAllReciters()
        assertThat(reciters).hasSize(18)
        // IDs must stay stable: they key the bundled timing files
        assertThat(reciters.map { it.id }).containsExactly(
            1, 5, 9, 10, 31, 32, 51, 53, 60, 62, 67, 74, 78, 106, 112, 118, 159, 256
        )

        assertThat(repo.getReciterById(1)).isNotNull()
        assertThat(repo.getReciterById(99999)).isNull()
        assertThat(repo.getDefaultReciter()).isNotNull()
    }
}
