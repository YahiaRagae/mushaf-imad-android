package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.QuranRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for QuranRepository (Parts / Juz and Quarters / Hizb).
 *
 * Covers: QA-6.4 (QuranRepository API) — Issue #24
 *
 * TC-6.32  getAllParts() returns exactly 30 Juz, sorted by number
 * TC-6.33  getPart(1) returns Juz 1 with Arabic and English titles; getDisplayTitle routes correctly
 * TC-6.34  getPart(0) / getPart(-1) / getPart(31) return null (boundary cases)
 * TC-6.35  getPartForPage(1) returns Juz 1; out-of-range pages return null
 * TC-6.36  getAllQuarters() returns exactly 240 hizb quarter entries
 * TC-6.37  getCacheStats() reflects zeroed counts on a freshly cleared repository
 * TC-6.38  clearAllCaches() resets all cache counts to zero
 */
@RunWith(AndroidJUnit4::class)
class QuranRepositoryTest {

    private lateinit var repository: QuranRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getQuranRepository()
        // Guarantee deterministic cache state for every test — caches are in-memory only.
        runBlocking { repository.clearAllCaches() }
    }

    // TC-6.32

    @Test
    fun getAllParts_returnsExactly30Juz() = runTest {
        val parts = repository.getAllParts()
        assertThat(parts).hasSize(30)
    }

    @Test
    fun getAllParts_areSortedByNumber() = runTest {
        val parts = repository.getAllParts()
        val numbers = parts.map { it.number }
        assertThat(numbers).isInOrder()
        assertThat(numbers.first()).isEqualTo(1)
        assertThat(numbers.last()).isEqualTo(30)
    }

    // TC-6.33

    @Test
    fun getPart_returnsJuz1WithTitles() = runTest {
        val part = repository.getPart(1)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(1)
        assertThat(part.arabicTitle).isNotEmpty()
        assertThat(part.englishTitle).isNotEmpty()
    }

    @Test
    fun getPart_returnsJuz30ForLastPart() = runTest {
        val part = repository.getPart(30)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(30)
    }

    @Test
    fun getPart_getDisplayTitle_routesCorrectlyByLocale() = runTest {
        val part = repository.getPart(1)!!

        // Each locale must return a non-empty string.
        assertThat(part.getDisplayTitle("ar")).isNotEmpty()
        assertThat(part.getDisplayTitle("en")).isNotEmpty()
        // The two locales must return distinct strings, proving the routing is active.
        assertThat(part.getDisplayTitle("ar")).isNotEqualTo(part.getDisplayTitle("en"))
    }

    // TC-6.34

    @Test
    fun getPart_returnsNullForPart0() = runTest {
        assertThat(repository.getPart(0)).isNull()
    }

    @Test
    fun getPart_returnsNullForNegativeNumber() = runTest {
        assertThat(repository.getPart(-1)).isNull()
    }

    @Test
    fun getPart_returnsNullForPart31() = runTest {
        assertThat(repository.getPart(31)).isNull()
    }

    // TC-6.35

    @Test
    fun getPartForPage_returnsJuz1ForPage1() = runTest {
        val part = repository.getPartForPage(1)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(1)
    }

    @Test
    fun getPartForPage_returnsJuz30ForLastPages() = runTest {
        // Pages 581–604 are Juz 30 in the standard Hafs 1441 layout.
        val part = repository.getPartForPage(600)

        assertThat(part).isNotNull()
        assertThat(part!!.number).isEqualTo(30)
    }

    @Test
    fun getPartForPage_returnsNullBelowLowerBoundary() = runTest {
        assertThat(repository.getPartForPage(0)).isNull()
    }

    @Test
    fun getPartForPage_returnsNullAboveUpperBoundary() = runTest {
        assertThat(repository.getPartForPage(605)).isNull()
    }

    // TC-6.36

    @Test
    fun getAllQuarters_returnsExactly240HizbQuarters() = runTest {
        val quarters = repository.getAllQuarters()

        // 60 Hizb × 4 quarters = 240 total.
        assertThat(quarters).hasSize(240)
    }

    @Test
    fun getAllQuarters_areSortedByHizbThenFraction() = runTest {
        val quarters = repository.getAllQuarters()
        val sorted = quarters.sortedWith(compareBy({ it.hizbNumber }, { it.hizbFraction }))
        assertThat(quarters).containsExactlyElementsIn(sorted).inOrder()
    }

    @Test
    fun getAllQuarters_firstQuarterHasExpectedFields() = runTest {
        val first = repository.getAllQuarters().first()

        assertThat(first.hizbNumber).isEqualTo(1)
        assertThat(first.hizbFraction).isEqualTo(0)
        assertThat(first.arabicTitle).isNotEmpty()
        assertThat(first.englishTitle).isNotEmpty()
    }

    // TC-6.37

    @Test
    fun getCacheStats_returnsZeroCountsOnFreshRepository() = runTest {
        // @Before already called clearAllCaches(); caches are empty.
        val stats = repository.getCacheStats()

        assertThat(stats.cachedPagesCount).isEqualTo(0)
        assertThat(stats.cachedChaptersCount).isEqualTo(0)
        assertThat(stats.totalVersesCached).isEqualTo(0)
    }

    // TC-6.38

    @Test
    fun clearAllCaches_resetsAllCountsToZero() = runTest {
        // clearAllCaches is also called in @Before, but call it again here to test
        // the method independently of test setup.
        repository.clearAllCaches()

        val stats = repository.getCacheStats()
        assertThat(stats.cachedPagesCount).isEqualTo(0)
        assertThat(stats.cachedChaptersCount).isEqualTo(0)
        assertThat(stats.totalVersesCached).isEqualTo(0)
    }
}
