package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for AudioRepository — reciter management APIs.
 *
 * Covers: QA-8.1 (Reciter Management API) — Issue #17
 *
 * TC-8.1   getAllReciters() returns exactly 18 reciters
 * TC-8.2   getReciterById(1) returns a valid ReciterInfo
 * TC-8.3   getReciterById(999) returns null
 * TC-8.4   getDefaultReciter() returns a valid ReciterInfo
 * TC-8.5   searchReciters("مشاري") Arabic name search works
 * TC-8.6   searchReciters("mishary") English name search works
 * TC-8.7   searchReciters("nonexistent") returns empty list
 * TC-8.8   getHafsReciters() returns only Hafs-recitation reciters
 * TC-8.9   saveSelectedReciter() → getSelectedReciterFlow() emits the saved reciter
 * TC-8.10  ReciterInfo.getAudioUrl(1) returns a URL ending in "001.mp3"
 * TC-8.11  ReciterInfo.isHafs and isWarsh return correct values
 *
 * Audio playback (TC-8.12 and beyond) requires an active ExoPlayer session
 * and is covered separately under QA-8.2 (instrumented device tests).
 */
@RunWith(AndroidJUnit4::class)
class AudioRepositoryRecitersTest {

    private lateinit var repository: AudioRepository

    @Before
    fun setUp() {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getAudioRepository()
    }

    // ──────────────────────────── TC-8.1 ────────────────────────────

    @Test
    fun getAllReciters_returnsExactly18Reciters() = runTest {
        val reciters = repository.getAllReciters()
        assertThat(reciters).hasSize(18)
    }

    @Test
    fun getAllReciters_eachReciterHasNonEmptyNames() = runTest {
        val reciters = repository.getAllReciters()
        reciters.forEach { reciter ->
            assertThat(reciter.nameArabic).isNotEmpty()
            assertThat(reciter.nameEnglish).isNotEmpty()
        }
    }

    // ──────────────────────────── TC-8.2 ────────────────────────────

    @Test
    fun getReciterById_validId_returnsReciterInfo() = runTest {
        val reciter = repository.getReciterById(1)

        assertThat(reciter).isNotNull()
        assertThat(reciter!!.id).isEqualTo(1)
    }

    // ──────────────────────────── TC-8.3 ────────────────────────────

    @Test
    fun getReciterById_nonExistentId_returnsNull() = runTest {
        assertThat(repository.getReciterById(999)).isNull()
    }

    // ──────────────────────────── TC-8.4 ────────────────────────────

    @Test
    fun getDefaultReciter_returnsValidReciterInfo() = runTest {
        val defaultReciter = repository.getDefaultReciter()

        assertThat(defaultReciter).isNotNull()
        assertThat(defaultReciter.nameArabic).isNotEmpty()
        assertThat(defaultReciter.nameEnglish).isNotEmpty()
        assertThat(defaultReciter.folderUrl).isNotEmpty()
    }

    // ──────────────────────────── TC-8.5 ────────────────────────────

    @Test
    fun searchReciters_arabicNameQuery_returnsMatchingReciters() = runTest {
        val results = repository.searchReciters("مشاري")

        assertThat(results).isNotEmpty()
        assertThat(results.any { it.nameArabic.contains("مشاري") }).isTrue()
    }

    // ──────────────────────────── TC-8.6 ────────────────────────────

    @Test
    fun searchReciters_englishNameQuery_returnsMatchingReciters() = runTest {
        val results = repository.searchReciters("mishary")

        assertThat(results).isNotEmpty()
        assertThat(results.any { it.nameEnglish.contains("mishary", ignoreCase = true) }).isTrue()
    }

    // ──────────────────────────── TC-8.7 ────────────────────────────

    @Test
    fun searchReciters_nonExistentQuery_returnsEmptyList() = runTest {
        val results = repository.searchReciters("zzz_nonexistent_zzz")
        assertThat(results).isEmpty()
    }

    // ──────────────────────────── TC-8.8 ────────────────────────────

    @Test
    fun getHafsReciters_returnsOnlyHafsReciters() = runTest {
        val hafsReciters = repository.getHafsReciters()

        assertThat(hafsReciters).isNotEmpty()
        hafsReciters.forEach { reciter ->
            assertThat(reciter.isHafs).isTrue()
        }
    }

    @Test
    fun getHafsReciters_countIsLessThanOrEqualToTotal() = runTest {
        val all = repository.getAllReciters()
        val hafs = repository.getHafsReciters()
        assertThat(hafs.size).isAtMost(all.size)
    }

    // ──────────────────────────── TC-8.9 ────────────────────────────

    @Test
    fun saveSelectedReciter_thenObserveFlow_emitsSavedReciter() = runTest {
        val reciters = repository.getAllReciters()
        val targetReciter = reciters[2] // pick any non-default reciter

        repository.getSelectedReciterFlow().test {
            awaitItem() // skip initial/default emission

            repository.saveSelectedReciter(targetReciter)

            val emitted = awaitItem()
            assertThat(emitted).isNotNull()
            assertThat(emitted!!.id).isEqualTo(targetReciter.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-8.10 ────────────────────────────

    @Test
    fun reciterInfo_getAudioUrl_chapter1_returnsUrlEndingIn001mp3() = runTest {
        val reciter = repository.getDefaultReciter()
        val url = reciter.getAudioUrl(1)

        assertThat(url).endsWith("001.mp3")
        assertThat(url).startsWith("http")
    }

    @Test
    fun reciterInfo_getAudioUrl_chapter114_returnsUrlEndingIn114mp3() = runTest {
        val reciter = repository.getDefaultReciter()
        val url = reciter.getAudioUrl(114)

        assertThat(url).endsWith("114.mp3")
    }

    // ──────────────────────────── TC-8.11 ────────────────────────────

    @Test
    fun reciterInfo_isHafs_trueWhenRewayaContainsHafs() = runTest {
        // At least one Hafs reciter must exist in the catalog
        val hafsReciter = repository.getAllReciters().firstOrNull { it.isHafs }
        assertThat(hafsReciter).isNotNull()
    }

    @Test
    fun reciterInfo_isWarsh_falseForHafsReciter() = runTest {
        // A Hafs reciter must not also be classified as Warsh
        val hafsReciter = repository.getHafsReciters().first()
        assertThat(hafsReciter.isWarsh).isFalse()
    }

    @Test
    fun reciterInfo_getDisplayName_returnsArabicNameForArLocale() = runTest {
        val reciter = repository.getDefaultReciter()
        assertThat(reciter.getDisplayName("ar")).isEqualTo(reciter.nameArabic)
    }

    @Test
    fun reciterInfo_getDisplayName_returnsEnglishNameForEnLocale() = runTest {
        val reciter = repository.getDefaultReciter()
        assertThat(reciter.getDisplayName("en")).isEqualTo(reciter.nameEnglish)
    }
}
