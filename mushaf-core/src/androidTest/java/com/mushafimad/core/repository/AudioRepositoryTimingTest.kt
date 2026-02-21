/**
 * Instrumented tests for the verse timing API exposed via [AudioRepository].
 *
 * Test cases covered:
 *   TC-8.23 - getChapterTimings() returns AyahTiming list for Al-Fatiha (chapter 1)
 *   TC-8.24 - Each AyahTiming has ayah > 0, startTime >= 0, and endTime > startTime
 *   TC-8.25 - getAyahTiming() returns exact start/end time for verse 1 of chapter 1
 *   TC-8.26 - getCurrentVerse() resolves correct verse number for two known playback positions
 *   TC-8.27 - hasTimingForReciter() returns true for a reciter that has timing data
 *   TC-8.28 - preloadTiming() completes without throwing
 *   TC-8.29 - Reciter without timing data: all four APIs return gracefully (false/empty/null)
 */
package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Reciter ID 1 (Ibrahim Al-Akdar) has verified timing data in assets/ayah_timing/read_1.json.
 * Reciter ID 2 is not in the availableReciterIds set and therefore has no timing data.
 *
 * Chapter 1 (Al-Fatiha) has exactly 7 verses.
 * Verse 1: startTime = 0 ms, endTime = 13189 ms
 * Verse 2: startTime = 13189 ms, endTime = 19867 ms
 *
 * getCurrentVerse applies a -10 ms correction:
 *   correctedTime = max(currentTimeMs - 10, 0)
 *   Returns the ayah where correctedTime ∈ [startTime, endTime]
 */
@RunWith(AndroidJUnit4::class)
class AudioRepositoryTimingTest {

    private lateinit var repository: AudioRepository

    // -----------------------------------------------------------------------
    // Reciter constants
    // -----------------------------------------------------------------------
    private val reciterWithTiming = 1    // Ibrahim Al-Akdar — present in assets
    private val reciterWithoutTiming = 2 // Not in availableReciterIds set

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getAudioRepository()
    }

    // -----------------------------------------------------------------------
    // TC-8.23: getChapterTimings returns a non-empty list for Al-Fatiha
    // -----------------------------------------------------------------------

    // TC-8.23
    @Test
    fun getChapterTimings_forReciterWithTiming_returnsAyahTimingListForAlFatiha() = runTest {
        val timings = repository.getChapterTimings(reciterWithTiming, 1)

        assertThat(timings).isNotEmpty()
        assertThat(timings).hasSize(7)
    }

    // -----------------------------------------------------------------------
    // TC-8.24: AyahTiming fields are valid
    // -----------------------------------------------------------------------

    // TC-8.24
    @Test
    fun getChapterTimings_eachAyahTiming_hasValidFields() = runTest {
        val timings = repository.getChapterTimings(reciterWithTiming, 1)

        for (timing in timings) {
            assertThat(timing.ayah).isGreaterThan(0)
            assertThat(timing.startTime).isAtLeast(0)
            assertThat(timing.endTime).isGreaterThan(timing.startTime)
        }
    }

    // -----------------------------------------------------------------------
    // TC-8.25: getAyahTiming returns exact timing for verse 1
    // -----------------------------------------------------------------------

    // TC-8.25
    @Test
    fun getAyahTiming_verse1OfChapter1_returnsExactStartAndEndTime() = runTest {
        val timing = repository.getAyahTiming(reciterWithTiming, 1, 1)

        assertThat(timing).isNotNull()
        assertThat(timing!!.ayah).isEqualTo(1)
        assertThat(timing.startTime).isEqualTo(0)
        assertThat(timing.endTime).isEqualTo(13189)
    }

    // -----------------------------------------------------------------------
    // TC-8.26: getCurrentVerse resolves verse number for known playback positions
    // -----------------------------------------------------------------------

    // TC-8.26
    @Test
    fun getCurrentVerse_withKnownPositions_returnsCorrectVerseNumbers() = runTest {
        // timeMs=1000 → correctedTime=990 → in [0, 13189] → verse 1
        val verse1 = repository.getCurrentVerse(reciterWithTiming, 1, 1000)
        assertThat(verse1).isEqualTo(1)

        // timeMs=15000 → correctedTime=14990 → in [13189, 19867] → verse 2
        val verse2 = repository.getCurrentVerse(reciterWithTiming, 1, 15000)
        assertThat(verse2).isEqualTo(2)
    }

    // -----------------------------------------------------------------------
    // TC-8.27: hasTimingForReciter returns true for reciter with timing
    // -----------------------------------------------------------------------

    // TC-8.27
    @Test
    fun hasTimingForReciter_reciterWithTimingData_returnsTrue() {
        val result = repository.hasTimingForReciter(reciterWithTiming)

        assertThat(result).isTrue()
    }

    // -----------------------------------------------------------------------
    // TC-8.28: preloadTiming completes without throwing
    // -----------------------------------------------------------------------

    // TC-8.28
    @Test
    fun preloadTiming_forReciterWithTiming_completesWithoutException() = runTest {
        repository.preloadTiming(reciterWithTiming)
        // No assertion required — the test passes if no exception is thrown
    }

    // -----------------------------------------------------------------------
    // TC-8.29: Reciter without timing data — all four APIs return gracefully
    // -----------------------------------------------------------------------

    // TC-8.29
    @Test
    fun reciterWithoutTimingData_allFourApis_returnGracefully() = runTest {
        assertThat(repository.hasTimingForReciter(reciterWithoutTiming)).isFalse()

        val timings = repository.getChapterTimings(reciterWithoutTiming, 1)
        assertThat(timings).isEmpty()

        val ayahTiming = repository.getAyahTiming(reciterWithoutTiming, 1, 1)
        assertThat(ayahTiming).isNull()

        val currentVerse = repository.getCurrentVerse(reciterWithoutTiming, 1, 1000)
        assertThat(currentVerse).isNull()
    }
}
