package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.repository.PageRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PageRepository (page metadata and caching).
 *
 * Covers: QA-6.3 (PageRepository API) — Issue #23
 *
 * TC-6.24  getPage(1) returns page with correct data
 * TC-6.25  getPage(604) returns the last page
 * TC-6.26  getTotalPages() returns 604
 * TC-6.27  getPageHeaderInfo(1) returns Juz/Surah info
 * TC-6.28  getPageHeaderInfo(1, HAFS_1405) works with alternate layout
 * TC-6.29  cachePage(1) then isPageCached(1) returns true
 * TC-6.30  clearPageCache(1) then isPageCached(1) returns false
 * TC-6.31  clearAllPageCache() completes without crash
 */
@RunWith(AndroidJUnit4::class)
class PageRepositoryTest {

    private lateinit var repository: PageRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getPageRepository()
    }

    @After
    fun tearDown() = runTest {
        // Leave the cache in a clean state after tests that modify it
        repository.clearAllPageCache()
    }

    // ──────────────────────────── TC-6.24 ────────────────────────────

    @Test
    fun getPage1_returnsPageWithCorrectNumber() = runTest {
        val page = repository.getPage(1)

        assertThat(page).isNotNull()
        assertThat(page!!.number).isEqualTo(1)
    }

    // ──────────────────────────── TC-6.25 ────────────────────────────

    @Test
    fun getPage604_returnsLastPage() = runTest {
        val page = repository.getPage(604)

        assertThat(page).isNotNull()
        assertThat(page!!.number).isEqualTo(604)
    }

    @Test
    fun getPage_returnsNullBeyondLastPage() = runTest {
        assertThat(repository.getPage(605)).isNull()
    }

    // ──────────────────────────── TC-6.26 ────────────────────────────

    @Test
    fun getTotalPages_returns604() = runTest {
        assertThat(repository.getTotalPages()).isEqualTo(604)
    }

    // ──────────────────────────── TC-6.27 ────────────────────────────

    @Test
    fun getPageHeaderInfo_page1_containsJuzAndSurahData() = runTest {
        val info = repository.getPageHeaderInfo(1)

        assertThat(info).isNotNull()
        // Page 1 is in Juz 1
        assertThat(info!!.partNumber).isEqualTo(1)
        // Page 1 has chapter info for Al-Fatiha
        assertThat(info.chapters).isNotEmpty()
        assertThat(info.chapters.any { it.number == 1 }).isTrue()
    }

    // ──────────────────────────── TC-6.28 ────────────────────────────

    @Test
    fun getPageHeaderInfo_page1_worksWithHafs1405Layout() = runTest {
        val info = repository.getPageHeaderInfo(1, MushafType.HAFS_1405)

        assertThat(info).isNotNull()
        assertThat(info!!.partNumber).isEqualTo(1)
    }

    // ──────────────────────────── TC-6.29 ────────────────────────────

    @Test
    fun cachePage_thenIsPageCached_returnsTrue() = runTest {
        repository.clearPageCache(1)   // ensure clean state first
        assertThat(repository.isPageCached(1)).isFalse()

        repository.cachePage(1)

        assertThat(repository.isPageCached(1)).isTrue()
    }

    // ──────────────────────────── TC-6.30 ────────────────────────────

    @Test
    fun clearPageCache_thenIsPageCached_returnsFalse() = runTest {
        repository.cachePage(2)
        assertThat(repository.isPageCached(2)).isTrue()

        repository.clearPageCache(2)

        assertThat(repository.isPageCached(2)).isFalse()
    }

    // ──────────────────────────── TC-6.31 ────────────────────────────

    @Test
    fun clearAllPageCache_completesWithoutCrash() = runTest {
        // Cache a few pages first
        repository.cachePage(1)
        repository.cachePage(2)

        repository.clearAllPageCache()

        // Both pages must no longer be cached
        assertThat(repository.isPageCached(1)).isFalse()
        assertThat(repository.isPageCached(2)).isFalse()
    }
}
