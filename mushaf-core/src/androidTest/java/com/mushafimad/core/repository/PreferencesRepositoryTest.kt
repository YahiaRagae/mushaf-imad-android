package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ColorScheme
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.ThemeMode
import com.mushafimad.core.domain.repository.PreferencesRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PreferencesRepository — Mushaf, audio, and theme preferences.
 *
 * Covers: QA-7.4 (Preferences Repository) — Issue #28
 *
 * TC-7.19  setMushafType → getMushafTypeFlow emits correct value
 * TC-7.20  setCurrentPage → getCurrentPageFlow emits correct value
 * TC-7.21  setFontSizeMultiplier(1.5) → flow emits 1.5
 * TC-7.22  setFontSizeMultiplier(3.0) → clamped to exactly 2.0
 * TC-7.23  setFontSizeMultiplier(0.1) → clamped to exactly 0.5
 * TC-7.24  setSelectedReciterId / getSelectedReciterId round-trip
 * TC-7.25  setPlaybackSpeed / getPlaybackSpeed round-trip
 * TC-7.26  setRepeatMode / getRepeatMode round-trip
 * TC-7.27  setLastAudioChapter / getLastAudioChapter round-trip
 * TC-7.28  setLastAudioPositionMs / getLastAudioPositionMs round-trip
 * TC-7.29  setThemeMode(DARK) → getThemeConfig().mode == DARK
 * TC-7.30  setColorScheme(SEPIA) → getThemeConfig().colorScheme == SEPIA
 * TC-7.31  setAmoledMode(true) → getThemeConfig().useAmoled == true
 * TC-7.32  getThemeConfigFlow emits updates on each change
 * TC-7.34  clearAll() resets all dirtied preference groups to defaults
 *
 * Note: TC-7.33 (persistence across process death) requires manual testing on device.
 */
@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryTest {

    private lateinit var repository: PreferencesRepository

    @Before
    fun setUp() = runTest {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getPreferencesRepository()
        repository.clearAll()
    }

    // ═══════════════════════════════════════════════════════════════
    //  Mushaf Reading Preferences
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.19 ────────────────────────────

    @Test
    fun setMushafType_flowEmitsNewValue() = runTest {
        repository.getMushafTypeFlow().test {
            awaitItem() // skip initial/default emission

            repository.setMushafType(MushafType.HAFS_1405)

            val emitted = awaitItem()
            assertThat(emitted).isEqualTo(MushafType.HAFS_1405)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-7.20 ────────────────────────────

    @Test
    fun setCurrentPage_flowEmitsNewPage() = runTest {
        repository.getCurrentPageFlow().test {
            awaitItem() // skip initial emission

            repository.setCurrentPage(300)

            val emitted = awaitItem()
            assertThat(emitted).isEqualTo(300)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-7.21 ────────────────────────────

    @Test
    fun setFontSizeMultiplier_validValue_flowEmitsExactValue() = runTest {
        repository.getFontSizeMultiplierFlow().test {
            awaitItem() // skip initial emission

            repository.setFontSizeMultiplier(1.5f)

            val emitted = awaitItem()
            assertThat(emitted).isWithin(0.01f).of(1.5f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-7.22 ────────────────────────────

    @Test
    fun setFontSizeMultiplier_aboveMax_clampedToExactMaximum() = runTest {
        repository.setFontSizeMultiplier(3.0f)

        repository.getFontSizeMultiplierFlow().test {
            val value = awaitItem()
            assertThat(value).isWithin(0.001f).of(2.0f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-7.23 ────────────────────────────

    @Test
    fun setFontSizeMultiplier_belowMin_clampedToExactMinimum() = runTest {
        repository.setFontSizeMultiplier(0.1f)

        repository.getFontSizeMultiplierFlow().test {
            val value = awaitItem()
            assertThat(value).isWithin(0.001f).of(0.5f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Audio Preferences
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.24 ────────────────────────────

    @Test
    fun setSelectedReciterId_roundTrip() = runTest {
        repository.setSelectedReciterId(5)
        assertThat(repository.getSelectedReciterId()).isEqualTo(5)
    }

    // ──────────────────────────── TC-7.25 ────────────────────────────

    @Test
    fun setPlaybackSpeed_roundTrip() = runTest {
        repository.setPlaybackSpeed(2.0f)
        assertThat(repository.getPlaybackSpeed()).isWithin(0.01f).of(2.0f)
    }

    // ──────────────────────────── TC-7.26 ────────────────────────────

    @Test
    fun setRepeatMode_roundTrip() = runTest {
        repository.setRepeatMode(true)
        assertThat(repository.getRepeatMode()).isTrue()

        repository.setRepeatMode(false)
        assertThat(repository.getRepeatMode()).isFalse()
    }

    // ──────────────────────────── TC-7.27 ────────────────────────────

    @Test
    fun setLastAudioChapter_roundTrip() = runTest {
        repository.setLastAudioChapter(5)
        assertThat(repository.getLastAudioChapter()).isEqualTo(5)
    }

    // ──────────────────────────── TC-7.28 ────────────────────────────

    @Test
    fun setLastAudioPositionMs_roundTrip() = runTest {
        repository.setLastAudioPositionMs(50_000L)
        assertThat(repository.getLastAudioPositionMs()).isEqualTo(50_000L)
    }

    // ═══════════════════════════════════════════════════════════════
    //  Theme Preferences
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.29 ────────────────────────────

    @Test
    fun setThemeMode_dark_getThemeConfigReflectsDarkMode() = runTest {
        repository.setThemeMode(ThemeMode.DARK)

        val config = repository.getThemeConfig()
        assertThat(config.mode).isEqualTo(ThemeMode.DARK)
    }

    // ──────────────────────────── TC-7.30 ────────────────────────────

    @Test
    fun setColorScheme_sepia_getThemeConfigReflectsSepiaScheme() = runTest {
        repository.setColorScheme(ColorScheme.SEPIA)

        val config = repository.getThemeConfig()
        assertThat(config.colorScheme).isEqualTo(ColorScheme.SEPIA)
    }

    // ──────────────────────────── TC-7.31 ────────────────────────────

    @Test
    fun setAmoledMode_true_getThemeConfigReflectsAmoledEnabled() = runTest {
        repository.setAmoledMode(true)

        val config = repository.getThemeConfig()
        assertThat(config.useAmoled).isTrue()
    }

    // ──────────────────────────── TC-7.32 ────────────────────────────

    @Test
    fun getThemeConfigFlow_emitsUpdatedConfigOnEachChange() = runTest {
        repository.getThemeConfigFlow().test {
            awaitItem() // skip initial emission

            repository.setThemeMode(ThemeMode.LIGHT)
            val afterLight = awaitItem()
            assertThat(afterLight.mode).isEqualTo(ThemeMode.LIGHT)
            assertThat(afterLight.colorScheme).isEqualTo(ColorScheme.DEFAULT)

            repository.setColorScheme(ColorScheme.WARM)
            val afterWarm = awaitItem()
            assertThat(afterWarm.mode).isEqualTo(ThemeMode.LIGHT)
            assertThat(afterWarm.colorScheme).isEqualTo(ColorScheme.WARM)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  General
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.34 ────────────────────────────

    @Test
    fun clearAll_resetsAllPreferenceGroupsToDefaults() = runTest {
        // Dirty all four preference groups
        repository.setMushafType(MushafType.HAFS_1405)
        repository.setCurrentPage(300)
        repository.setThemeMode(ThemeMode.DARK)
        repository.setRepeatMode(true)

        repository.clearAll()

        // Mushaf type must revert to HAFS_1441
        repository.getMushafTypeFlow().test {
            assertThat(awaitItem()).isEqualTo(MushafType.HAFS_1441)
            cancelAndIgnoreRemainingEvents()
        }

        // Current page must revert to 1
        repository.getCurrentPageFlow().test {
            assertThat(awaitItem()).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }

        // Theme mode must revert to SYSTEM
        repository.getThemeConfigFlow().test {
            assertThat(awaitItem().mode).isEqualTo(ThemeMode.SYSTEM)
            cancelAndIgnoreRemainingEvents()
        }

        // Repeat mode must revert to false
        assertThat(repository.getRepeatMode()).isFalse()
    }
}
