package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.QuranRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for QuranRepository (Parts / Juz and Quarters / Hizb).
 *
 * Covers: QA-6.4 (QuranRepository API) — Issue #24
 *
 * TC-6.32  getAllParts() returns 30 Juz
 * TC-6.33  getPart(1) returns Juz 1 with Arabic and English titles
 * TC-6.34  getPart(0) returns null (below boundary)
 * TC-6.35  getPartForPage(1) returns Juz 1
 * TC-6.36  getAllQuarters() returns hizb quarter data
 * TC-6.37  getCacheStats() returns a valid stats object
 * TC-6.38  clearAllCaches() completes without crash
 */
@RunWith(AndroidJUnit4::class)
class QuranRepositoryTest {

    private lateinit var repository: QuranRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getQuranRepository()
    }

    // ──────────────────────────── TC-6.32 ────────────────────────────

    @Test
    fun getAllParts_returnsExactly30Juz() = runTest {
        val parts = repository.getAllParts()
        assertThat(parts).hasSize(30)
    }

    // ──────────────────────────── TC-6.33 ────────────────────────────

    @Test
    fun getPart_returnsJuz1WithTitles() = runTest {
        val part = repository.getPart(1)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(1)
        assertThat(part.arabicTitle).isNotEmpty()
        assertThat(part.englishTitle).isNotEmpty()
    }

    @Test
    fun getPart_getDisplayTitle_returnsArabicForArLocale() = runTest {
        val part = repository.getPart(1)!!
        assertThat(part.getDisplayTitle("ar")).isEqualTo(part.arabicTitle)
    }

    @Test
    fun getPart_getDisplayTitle_returnsEnglishForEnLocale() = runTest {
        val part = repository.getPart(1)!!
        assertThat(part.getDisplayTitle("en")).isEqualTo(part.englishTitle)
    }

    // ──────────────────────────── TC-6.34 ────────────────────────────

    @Test
    fun getPart_returnsNullForPart0() = runTest {
        assertThat(repository.getPart(0)).isNull()
    }

    @Test
    fun getPart_returnsNullForPart31() = runTest {
        assertThat(repository.getPart(31)).isNull()
    }

    // ──────────────────────────── TC-6.35 ────────────────────────────

    @Test
    fun getPartForPage_returnsJuz1ForPage1() = runTest {
        val part = repository.getPartForPage(1)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(1)
    }

    @Test
    fun getPartForPage_returnsJuz30ForLastPages() = runTest {
        // Pages 581–604 are Juz 30 in standard Quran layouts
        val part = repository.getPartForPage(600)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(30)
    }

    // ──────────────────────────── TC-6.36 ────────────────────────────

    @Test
    fun getAllQuarters_returnsNonEmptyHizbData() = runTest {
        val quarters = repository.getAllQuarters()

        // 60 Hizb × 4 quarters = 240 quarters total
        assertThat(quarters).isNotEmpty()
        assertThat(quarters.size).isAtLeast(240)
    }

    // ──────────────────────────── TC-6.37 ────────────────────────────

    @Test
    fun getCacheStats_returnsValidStatsObject() = runTest {
        val stats = repository.getCacheStats()
        assertThat(stats).isNotNull()
    }

    // ──────────────────────────── TC-6.38 ────────────────────────────

    @Test
    fun clearAllCaches_completesWithoutCrash() = runTest {
        repository.clearAllCaches()
        // If we reach this line the call completed without throwing
        assertThat(true).isTrue()
    }
}
