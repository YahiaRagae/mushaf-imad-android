package com.mushafimad.core.data.repository

import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.PreferencesRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryTest {

    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    // ===== QA-7.4: Preferences Repository =====

    // TC-7.40: getMushafTypeFlow returns default HAFS_1441
    @Test
    fun `getMushafTypeFlow returns default HAFS_1441`() = runTest {
        every { repository.getMushafTypeFlow() } returns flowOf(MushafType.HAFS_1441)

        val result = repository.getMushafTypeFlow().first()
        assertEquals(MushafType.HAFS_1441, result)
    }

    // TC-7.41: setMushafType stores type
    @Test
    fun `setMushafType stores mushaf type`() = runTest {
        repository.setMushafType(MushafType.HAFS_1405)
        coVerify { repository.setMushafType(MushafType.HAFS_1405) }
    }

    // TC-7.42: getCurrentPageFlow returns default page 1
    @Test
    fun `getCurrentPageFlow returns default page 1`() = runTest {
        every { repository.getCurrentPageFlow() } returns flowOf(1)

        val result = repository.getCurrentPageFlow().first()
        assertEquals(1, result)
    }

    // TC-7.43: setCurrentPage stores page
    @Test
    fun `setCurrentPage stores page number`() = runTest {
        repository.setCurrentPage(300)
        coVerify { repository.setCurrentPage(300) }
    }

    // TC-7.44: getLastReadChapterFlow returns null initially
    @Test
    fun `getLastReadChapterFlow returns null when not set`() = runTest {
        every { repository.getLastReadChapterFlow() } returns flowOf(null)

        val result = repository.getLastReadChapterFlow().first()
        assertNull(result)
    }

    // TC-7.45: setLastReadChapter stores chapter
    @Test
    fun `setLastReadChapter stores chapter number`() = runTest {
        repository.setLastReadChapter(36)
        coVerify { repository.setLastReadChapter(36) }
    }

    // TC-7.46: getLastReadVerseFlow returns null initially
    @Test
    fun `getLastReadVerseFlow returns null when not set`() = runTest {
        every { repository.getLastReadVerseFlow() } returns flowOf(null)

        val result = repository.getLastReadVerseFlow().first()
        assertNull(result)
    }

    // TC-7.47: setLastReadVerse stores chapter and verse
    @Test
    fun `setLastReadVerse stores chapter and verse pair`() = runTest {
        repository.setLastReadVerse(2, 255)
        coVerify { repository.setLastReadVerse(2, 255) }
    }

    // TC-7.48: getFontSizeMultiplierFlow returns default 1.0
    @Test
    fun `getFontSizeMultiplierFlow returns default 1_0`() = runTest {
        every { repository.getFontSizeMultiplierFlow() } returns flowOf(1.0f)

        val result = repository.getFontSizeMultiplierFlow().first()
        assertEquals(1.0f, result)
    }

    // TC-7.49: setFontSizeMultiplier stores value
    @Test
    fun `setFontSizeMultiplier stores multiplier`() = runTest {
        repository.setFontSizeMultiplier(1.5f)
        coVerify { repository.setFontSizeMultiplier(1.5f) }
    }

    // TC-7.50: getShowTranslationFlow returns false by default
    @Test
    fun `getShowTranslationFlow returns false by default`() = runTest {
        every { repository.getShowTranslationFlow() } returns flowOf(false)

        val result = repository.getShowTranslationFlow().first()
        assertFalse(result)
    }

    // TC-7.51: setShowTranslation stores value
    @Test
    fun `setShowTranslation stores boolean value`() = runTest {
        repository.setShowTranslation(true)
        coVerify { repository.setShowTranslation(true) }
    }

    // TC-7.52: getSelectedReciterIdFlow returns default 1
    @Test
    fun `getSelectedReciterIdFlow returns default reciter id`() = runTest {
        every { repository.getSelectedReciterIdFlow() } returns flowOf(1)

        val result = repository.getSelectedReciterIdFlow().first()
        assertEquals(1, result)
    }

    // TC-7.53: setSelectedReciterId stores reciter
    @Test
    fun `setSelectedReciterId stores reciter id`() = runTest {
        repository.setSelectedReciterId(5)
        coVerify { repository.setSelectedReciterId(5) }
    }

    // TC-7.54: getPlaybackSpeedFlow returns default 1.0
    @Test
    fun `getPlaybackSpeedFlow returns default 1_0`() = runTest {
        every { repository.getPlaybackSpeedFlow() } returns flowOf(1.0f)

        val result = repository.getPlaybackSpeedFlow().first()
        assertEquals(1.0f, result)
    }

    // TC-7.55: setPlaybackSpeed stores speed
    @Test
    fun `setPlaybackSpeed stores playback speed`() = runTest {
        repository.setPlaybackSpeed(1.5f)
        coVerify { repository.setPlaybackSpeed(1.5f) }
    }

    // TC-7.56: getRepeatModeFlow returns false by default
    @Test
    fun `getRepeatModeFlow returns false by default`() = runTest {
        every { repository.getRepeatModeFlow() } returns flowOf(false)

        val result = repository.getRepeatModeFlow().first()
        assertFalse(result)
    }

    // TC-7.57: setRepeatMode stores value
    @Test
    fun `setRepeatMode stores repeat mode`() = runTest {
        repository.setRepeatMode(true)
        coVerify { repository.setRepeatMode(true) }
    }

    // TC-7.58: getLastAudioChapter returns null initially
    @Test
    fun `getLastAudioChapter returns null when not set`() = runTest {
        coEvery { repository.getLastAudioChapter() } returns null

        val result = repository.getLastAudioChapter()
        assertNull(result)
    }

    // TC-7.59: setLastAudioChapter stores chapter
    @Test
    fun `setLastAudioChapter stores chapter number`() = runTest {
        repository.setLastAudioChapter(18)
        coVerify { repository.setLastAudioChapter(18) }
    }

    // TC-7.60: setLastAudioChapter with null clears value
    @Test
    fun `setLastAudioChapter with null clears stored value`() = runTest {
        repository.setLastAudioChapter(null)
        coVerify { repository.setLastAudioChapter(null) }
    }

    // TC-7.61: getLastAudioVerse returns null initially
    @Test
    fun `getLastAudioVerse returns null when not set`() = runTest {
        coEvery { repository.getLastAudioVerse() } returns null

        val result = repository.getLastAudioVerse()
        assertNull(result)
    }

    // TC-7.62: setLastAudioVerse stores verse
    @Test
    fun `setLastAudioVerse stores verse number`() = runTest {
        repository.setLastAudioVerse(10)
        coVerify { repository.setLastAudioVerse(10) }
    }

    // TC-7.63: getLastAudioPositionMs returns 0 initially
    @Test
    fun `getLastAudioPositionMs returns 0 when not set`() = runTest {
        coEvery { repository.getLastAudioPositionMs() } returns 0L

        val result = repository.getLastAudioPositionMs()
        assertEquals(0L, result)
    }

    // TC-7.64: setLastAudioPositionMs stores position
    @Test
    fun `setLastAudioPositionMs stores position`() = runTest {
        repository.setLastAudioPositionMs(150000L)
        coVerify { repository.setLastAudioPositionMs(150000L) }
    }

    // TC-7.65: getThemeConfigFlow returns default config
    @Test
    fun `getThemeConfigFlow returns default ThemeConfig`() = runTest {
        val defaultConfig = ThemeConfig()
        every { repository.getThemeConfigFlow() } returns flowOf(defaultConfig)

        val result = repository.getThemeConfigFlow().first()
        assertEquals(ThemeMode.SYSTEM, result.mode)
        assertEquals(ColorScheme.DEFAULT, result.colorScheme)
        assertFalse(result.useAmoled)
    }

    // TC-7.66: setThemeMode stores mode
    @Test
    fun `setThemeMode stores theme mode`() = runTest {
        repository.setThemeMode(ThemeMode.DARK)
        coVerify { repository.setThemeMode(ThemeMode.DARK) }
    }

    // TC-7.67: setColorScheme stores scheme
    @Test
    fun `setColorScheme stores color scheme`() = runTest {
        repository.setColorScheme(ColorScheme.SEPIA)
        coVerify { repository.setColorScheme(ColorScheme.SEPIA) }
    }

    // TC-7.68: setAmoledMode stores flag
    @Test
    fun `setAmoledMode stores amoled flag`() = runTest {
        repository.setAmoledMode(true)
        coVerify { repository.setAmoledMode(true) }
    }

    // TC-7.69: updateThemeConfig stores complete config
    @Test
    fun `updateThemeConfig stores complete theme configuration`() = runTest {
        val config = ThemeConfig(
            mode = ThemeMode.DARK,
            colorScheme = ColorScheme.WARM,
            useAmoled = true
        )
        repository.updateThemeConfig(config)
        coVerify { repository.updateThemeConfig(config) }
    }

    // TC-7.70: clearAll clears all preferences
    @Test
    fun `clearAll invokes clear on repository`() = runTest {
        repository.clearAll()
        coVerify { repository.clearAll() }
    }

    // TC-7.71: ThemeMode enum values
    @Test
    fun `ThemeMode has LIGHT DARK and SYSTEM values`() {
        val values = ThemeMode.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(ThemeMode.LIGHT))
        assertTrue(values.contains(ThemeMode.DARK))
        assertTrue(values.contains(ThemeMode.SYSTEM))
    }

    // TC-7.72: ColorScheme enum values
    @Test
    fun `ColorScheme has DEFAULT WARM COOL and SEPIA values`() {
        val values = ColorScheme.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(ColorScheme.DEFAULT))
        assertTrue(values.contains(ColorScheme.WARM))
        assertTrue(values.contains(ColorScheme.COOL))
        assertTrue(values.contains(ColorScheme.SEPIA))
    }

    // TC-7.73: ThemeConfig defaults
    @Test
    fun `ThemeConfig default values are correct`() {
        val config = ThemeConfig()
        assertEquals(ThemeMode.SYSTEM, config.mode)
        assertEquals(ColorScheme.DEFAULT, config.colorScheme)
        assertFalse(config.useAmoled)
    }

    // TC-7.74: ThemeConfig copy
    @Test
    fun `ThemeConfig copy preserves unmodified fields`() {
        val original = ThemeConfig(mode = ThemeMode.LIGHT)
        val modified = original.copy(useAmoled = true)

        assertEquals(ThemeMode.LIGHT, modified.mode)
        assertEquals(ColorScheme.DEFAULT, modified.colorScheme)
        assertTrue(modified.useAmoled)
    }

    // TC-7.75: getSelectedReciterId returns default
    @Test
    fun `getSelectedReciterId returns stored value`() = runTest {
        coEvery { repository.getSelectedReciterId() } returns 3

        val result = repository.getSelectedReciterId()
        assertEquals(3, result)
    }

    // TC-7.76: getPlaybackSpeed returns stored value
    @Test
    fun `getPlaybackSpeed returns stored value`() = runTest {
        coEvery { repository.getPlaybackSpeed() } returns 1.5f

        val result = repository.getPlaybackSpeed()
        assertEquals(1.5f, result)
    }

    // TC-7.77: getRepeatMode returns stored value
    @Test
    fun `getRepeatMode returns stored value`() = runTest {
        coEvery { repository.getRepeatMode() } returns true

        val result = repository.getRepeatMode()
        assertTrue(result)
    }

    // TC-7.78: getThemeConfig returns stored config
    @Test
    fun `getThemeConfig returns stored configuration`() = runTest {
        val config = ThemeConfig(ThemeMode.DARK, ColorScheme.COOL, true)
        coEvery { repository.getThemeConfig() } returns config

        val result = repository.getThemeConfig()
        assertEquals(ThemeMode.DARK, result.mode)
        assertEquals(ColorScheme.COOL, result.colorScheme)
        assertTrue(result.useAmoled)
    }

    // TC-7.79: getLastAudioChapterFlow returns null initially
    @Test
    fun `getLastAudioChapterFlow returns null initially`() = runTest {
        every { repository.getLastAudioChapterFlow() } returns flowOf(null)

        val result = repository.getLastAudioChapterFlow().first()
        assertNull(result)
    }

    // TC-7.80: getLastAudioPositionMsFlow returns 0 initially
    @Test
    fun `getLastAudioPositionMsFlow returns 0 initially`() = runTest {
        every { repository.getLastAudioPositionMsFlow() } returns flowOf(0L)

        val result = repository.getLastAudioPositionMsFlow().first()
        assertEquals(0L, result)
    }
}
