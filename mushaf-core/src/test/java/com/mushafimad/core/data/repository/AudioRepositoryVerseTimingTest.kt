package com.mushafimad.core.data.repository

import com.mushafimad.core.data.audio.AyahTimingService
import com.mushafimad.core.data.audio.MediaSessionManager
import com.mushafimad.core.data.audio.ReciterService
import com.mushafimad.core.domain.models.AyahTiming
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AudioRepositoryVerseTimingTest {

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var ayahTimingService: AyahTimingService
    private lateinit var reciterService: ReciterService
    private lateinit var repository: DefaultAudioRepository

    private val testReciterId = 1

    @Before
    fun setup() {
        mediaSessionManager = mockk(relaxed = true)
        ayahTimingService = mockk(relaxed = true)
        reciterService = mockk(relaxed = true)
        repository = DefaultAudioRepository(mediaSessionManager, ayahTimingService, reciterService)
    }

    // TC-8.23: getChapterTimings(reciterId, 1) — returns list of AyahTiming for Al-Fatiha
    @Test
    fun `getChapterTimings returns list of AyahTiming for Al-Fatiha`() = runTest {
        val timings = (1..7).map { AyahTiming(ayah = it, startTime = (it - 1) * 5000, endTime = it * 5000) }
        coEvery { ayahTimingService.getChapterTimings(testReciterId, 1) } returns timings

        val result = repository.getChapterTimings(testReciterId, 1)

        assertTrue(result.isNotEmpty())
        assertEquals(7, result.size)
    }

    // TC-8.24: Each AyahTiming has ayah, startTime, and endTime in milliseconds
    @Test
    fun `each AyahTiming has ayah startTime and endTime`() = runTest {
        val timings = listOf(
            AyahTiming(ayah = 1, startTime = 0, endTime = 5000),
            AyahTiming(ayah = 2, startTime = 5000, endTime = 12000)
        )
        coEvery { ayahTimingService.getChapterTimings(testReciterId, 1) } returns timings

        val result = repository.getChapterTimings(testReciterId, 1)

        result.forEach { timing ->
            assertTrue(timing.ayah > 0)
            assertTrue(timing.startTime >= 0)
            assertTrue(timing.endTime > 0)
            assertTrue(timing.endTime > timing.startTime)
        }
    }

    // TC-8.25: getAyahTiming(reciterId, 1, 1) — returns start/end time for verse 1
    @Test
    fun `getAyahTiming returns start and end time for verse 1`() = runTest {
        val timing = AyahTiming(ayah = 1, startTime = 0, endTime = 5000)
        coEvery { ayahTimingService.getTiming(testReciterId, 1, 1) } returns timing

        val result = repository.getAyahTiming(testReciterId, 1, 1)

        assertNotNull(result)
        assertEquals(1, result!!.ayah)
        assertEquals(0, result.startTime)
        assertEquals(5000, result.endTime)
    }

    // TC-8.26: getCurrentVerse(reciterId, 1, timeMs) — verify correct verse number returned
    @Test
    fun `getCurrentVerse returns correct verse number for known time`() = runTest {
        coEvery { ayahTimingService.getCurrentVerse(testReciterId, 1, 3000) } returns 1
        coEvery { ayahTimingService.getCurrentVerse(testReciterId, 1, 8000) } returns 2

        val verse1 = repository.getCurrentVerse(testReciterId, 1, 3000)
        val verse2 = repository.getCurrentVerse(testReciterId, 1, 8000)

        assertEquals(1, verse1)
        assertEquals(2, verse2)
    }

    // TC-8.27: hasTimingForReciter(reciterId) — returns true for reciters with timing data
    @Test
    fun `hasTimingForReciter returns true for reciter with timing data`() {
        every { ayahTimingService.hasTimingForReciter(testReciterId) } returns true

        val result = repository.hasTimingForReciter(testReciterId)

        assertTrue(result)
    }

    // TC-8.28: preloadTiming(reciterId) — completes without crash
    @Test
    fun `preloadTiming completes without crash`() = runTest {
        repository.preloadTiming(testReciterId)

        coVerify { ayahTimingService.preloadTiming(testReciterId) }
    }

    // TC-8.29: For reciter WITHOUT timing data — verify graceful handling
    @Test
    fun `reciter without timing data returns empty list`() = runTest {
        val invalidReciterId = 9999
        coEvery { ayahTimingService.getChapterTimings(invalidReciterId, 1) } returns emptyList()
        every { ayahTimingService.hasTimingForReciter(invalidReciterId) } returns false

        val timings = repository.getChapterTimings(invalidReciterId, 1)
        val hasTiming = repository.hasTimingForReciter(invalidReciterId)

        assertTrue(timings.isEmpty())
        assertFalse(hasTiming)
    }

    @Test
    fun `getAyahTiming returns null for reciter without timing`() = runTest {
        val invalidReciterId = 9999
        coEvery { ayahTimingService.getTiming(invalidReciterId, 1, 1) } returns null

        val result = repository.getAyahTiming(invalidReciterId, 1, 1)

        assertNull(result)
    }

    @Test
    fun `getCurrentVerse returns null for reciter without timing`() = runTest {
        val invalidReciterId = 9999
        coEvery { ayahTimingService.getCurrentVerse(invalidReciterId, 1, 3000) } returns null

        val result = repository.getCurrentVerse(invalidReciterId, 1, 3000)

        assertNull(result)
    }
}
