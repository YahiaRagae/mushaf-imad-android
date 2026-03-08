package com.mushafimad.core.data.repository

import com.mushafimad.core.data.cache.CacheStats
import com.mushafimad.core.data.cache.ChaptersDataCache
import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.domain.models.Part
import com.mushafimad.core.domain.models.Quarter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuranRepositoryTest {

    private lateinit var realmService: RealmService
    private lateinit var chaptersDataCache: ChaptersDataCache
    private lateinit var quranDataCacheService: QuranDataCacheService
    private lateinit var repository: DefaultQuranRepository

    @Before
    fun setup() {
        realmService = mockk(relaxed = true)
        chaptersDataCache = mockk(relaxed = true)
        quranDataCacheService = mockk(relaxed = true)
        repository = DefaultQuranRepository(realmService, chaptersDataCache, quranDataCacheService)
    }

    // TC-6.32: getAllParts() returns 30 Juz
    @Test
    fun `getAllParts returns 30 Juz`() = runTest {
        val parts = (1..30).map { Part(it, it, "الجزء $it", "Juz $it") }
        coEvery { realmService.fetchAllPartsAsync() } returns parts

        val result = repository.getAllParts()

        assertEquals(30, result.size)
        assertEquals(1, result.first().number)
        assertEquals(30, result.last().number)
    }

    // TC-6.33: getPart(1) returns Juz 1 with Arabic and English titles
    @Test
    fun `getPart 1 returns Juz 1 with Arabic and English titles`() = runTest {
        val juz1 = Part(1, 1, "الجزء الأول", "Juz 1")
        coEvery { realmService.getPart(1) } returns juz1

        val result = repository.getPart(1)

        assertNotNull(result)
        assertEquals(1, result!!.number)
        assertTrue(result.arabicTitle.isNotEmpty())
        assertTrue(result.englishTitle.isNotEmpty())
        assertEquals("الجزء الأول", result.arabicTitle)
        assertEquals("Juz 1", result.englishTitle)
    }

    // TC-6.34: getPart(0) returns null (boundary)
    @Test
    fun `getPart 0 returns null for boundary`() = runTest {
        coEvery { realmService.getPart(0) } returns null

        val result = repository.getPart(0)

        assertNull(result)
    }

    // TC-6.35: getPartForPage(1) returns Juz 1
    @Test
    fun `getPartForPage 1 returns Juz 1`() = runTest {
        val juz1 = Part(1, 1, "الجزء الأول", "Juz 1")
        coEvery { realmService.getPartForPage(1) } returns juz1

        val result = repository.getPartForPage(1)

        assertNotNull(result)
        assertEquals(1, result!!.number)
    }

    // TC-6.36: getAllQuarters() returns hizb quarter data
    @Test
    fun `getAllQuarters returns hizb quarter data`() = runTest {
        val quarters = listOf(
            Quarter(1, 1, 0, "ربع الحزب الأول", "Quarter 1", 1),
            Quarter(2, 1, 1, "ربع الحزب الثاني", "Quarter 2", 1)
        )
        coEvery { realmService.fetchAllQuartersAsync() } returns quarters

        val result = repository.getAllQuarters()

        assertTrue(result.isNotEmpty())
        assertNotNull(result.first().hizbNumber)
        assertNotNull(result.first().hizbFraction)
    }

    // TC-6.37: getCacheStats() returns a valid stats object
    @Test
    fun `getCacheStats returns valid stats object`() = runTest {
        val stats = CacheStats(cachedPagesCount = 5, cachedChaptersCount = 3, totalVersesCached = 100)
        coEvery { quranDataCacheService.getCacheStats() } returns stats

        val result = repository.getCacheStats()

        assertNotNull(result)
        assertEquals(5, result.cachedPagesCount)
        assertEquals(3, result.cachedChaptersCount)
        assertEquals(100, result.totalVersesCached)
    }

    // TC-6.38: clearAllCaches() clears without crash
    @Test
    fun `clearAllCaches completes without crash`() = runTest {
        repository.clearAllCaches()

        coVerify { chaptersDataCache.clearCache() }
        coVerify { quranDataCacheService.clearAllCache() }
    }
}
