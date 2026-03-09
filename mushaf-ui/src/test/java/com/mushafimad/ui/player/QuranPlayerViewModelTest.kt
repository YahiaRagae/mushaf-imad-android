package com.mushafimad.ui.player

import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.domain.repository.PreferencesRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuranPlayerViewModelTest {

    private lateinit var audioRepository: AudioRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    private val playerStateFlow = MutableStateFlow(AudioPlayerState())

    private fun testReciter(id: Int = 1) = ReciterInfo(
        id = id, nameArabic = "قارئ $id",
        nameEnglish = "Reciter $id",
        rewaya = "حفص عن عاصم",
        folderUrl = "https://example.com/reciter$id/"
    )

    private fun createViewModel(): QuranPlayerViewModel {
        return QuranPlayerViewModel(audioRepository, preferencesRepository)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)

        every { audioRepository.getPlayerStateFlow() } returns playerStateFlow
        coEvery { audioRepository.getAllReciters() } returns listOf(testReciter(1), testReciter(2))
        every { audioRepository.getSelectedReciterFlow() } returns flowOf(testReciter(1))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== QA-3.1: QuranPlayerView Basic & States =====

    // TC-3.1: Initial playback state is IDLE
    @Test
    fun `initial playback state is IDLE`() = runTest {
        val vm = createViewModel()
        assertEquals(PlaybackState.IDLE, vm.playbackState.value)
    }

    // TC-3.2: Initial current time is 0
    @Test
    fun `initial current time is 0`() = runTest {
        val vm = createViewModel()
        assertEquals(0L, vm.currentTimeMs.value)
    }

    // TC-3.3: Initial duration is 0
    @Test
    fun `initial duration is 0`() = runTest {
        val vm = createViewModel()
        assertEquals(0L, vm.durationMs.value)
    }

    // TC-3.4: Initial playback rate is 1.0
    @Test
    fun `initial playback rate is 1_0`() = runTest {
        val vm = createViewModel()
        assertEquals(1.0f, vm.playbackRate.value)
    }

    // TC-3.5: Initial repeat is disabled
    @Test
    fun `initial repeat mode is disabled`() = runTest {
        val vm = createViewModel()
        assertFalse(vm.isRepeatEnabled.value)
    }

    // TC-3.6: Initial verse number is null
    @Test
    fun `initial current verse number is null`() = runTest {
        val vm = createViewModel()
        assertNull(vm.currentVerseNumber.value)
    }

    // TC-3.7: configure sets chapter info
    @Test
    fun `configure sets chapter number and name`() = runTest {
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah")

        val info = vm.getChapterInfo()
        assertEquals(1, info.number)
        assertEquals("Al-Fatihah", info.name)
    }

    // TC-3.8: configure with same chapter is no-op
    @Test
    fun `configure with same chapter does not reconfigure`() = runTest {
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", 1)
        vm.configure(1, "Al-Fatihah")

        val info = vm.getChapterInfo()
        assertEquals(1, info.number)
    }

    // TC-3.9: configure with explicit reciterId
    @Test
    fun `configure with explicit reciterId updates reciter`() = runTest {
        coEvery { audioRepository.getReciterById(2) } returns testReciter(2)
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 2)

        assertEquals(testReciter(2), vm.selectedReciter.value)
    }

    // TC-3.10: play delegates to audioRepository
    @Test
    fun `play calls audioRepository play`() = runTest {
        val vm = createViewModel()
        vm.play()
        verify { audioRepository.play() }
    }

    // TC-3.11: pause delegates to audioRepository
    @Test
    fun `pause calls audioRepository pause`() = runTest {
        val vm = createViewModel()
        vm.pause()
        verify { audioRepository.pause() }
    }

    // TC-3.12: togglePlayback from IDLE calls play
    @Test
    fun `togglePlayback when IDLE calls play`() = runTest {
        val vm = createViewModel()
        vm.togglePlayback()
        verify { audioRepository.play() }
    }

    // TC-3.13: togglePlayback from PLAYING calls pause
    @Test
    fun `togglePlayback when PLAYING calls pause`() = runTest {
        val vm = createViewModel()
        playerStateFlow.value = AudioPlayerState(playbackState = PlaybackState.PLAYING)
        vm.togglePlayback()
        verify { audioRepository.pause() }
    }

    // TC-3.14: stop resets verse and delegates
    @Test
    fun `stop calls audioRepository stop and resets verse`() = runTest {
        val vm = createViewModel()
        vm.stop()
        verify { audioRepository.stop() }
        assertNull(vm.currentVerseNumber.value)
    }

    // TC-3.15: loadChapter with unconfigured chapter does nothing
    @Test
    fun `loadChapter without configure does not call audioRepository`() = runTest {
        val vm = createViewModel()
        vm.loadChapter(autoPlay = true)
        verify(exactly = 0) { audioRepository.loadChapter(any(), any(), any()) }
    }

    // TC-3.16: loadChapter with configured chapter delegates
    @Test
    fun `loadChapter after configure calls audioRepository loadChapter`() = runTest {
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.loadChapter(autoPlay = true)
        verify { audioRepository.loadChapter(1, 1, true) }
    }

    // TC-3.17: hasValidConfiguration returns false initially
    @Test
    fun `hasValidConfiguration returns false before configure`() = runTest {
        val vm = createViewModel()
        assertFalse(vm.hasValidConfiguration())
    }

    // TC-3.18: hasValidConfiguration returns true after configure
    @Test
    fun `hasValidConfiguration returns true after configure with reciter`() = runTest {
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        assertTrue(vm.hasValidConfiguration())
    }

    // TC-3.19: selectReciter updates state and saves
    @Test
    fun `selectReciter updates selected reciter and saves`() = runTest {
        val reciter = testReciter(2)
        val vm = createViewModel()
        vm.selectReciter(reciter, reloadAudio = false)

        assertEquals(reciter, vm.selectedReciter.value)
        verify { audioRepository.saveSelectedReciter(reciter) }
    }

    // TC-3.20: selectReciter with reloadAudio stops and reloads
    @Test
    fun `selectReciter with reloadAudio reloads chapter`() = runTest {
        val reciter = testReciter(2)
        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.selectReciter(reciter, reloadAudio = true)

        verify { audioRepository.stop() }
        verify { audioRepository.loadChapter(1, 2, any()) }
    }

    // TC-3.21: toggleRepeat flips state
    @Test
    fun `toggleRepeat flips repeat enabled state`() = runTest {
        val vm = createViewModel()
        assertFalse(vm.isRepeatEnabled.value)
        vm.toggleRepeat()
        assertTrue(vm.isRepeatEnabled.value)
        vm.toggleRepeat()
        assertFalse(vm.isRepeatEnabled.value)
    }

    // TC-3.22: toggleRepeat calls audioRepository
    @Test
    fun `toggleRepeat calls audioRepository setRepeatMode`() = runTest {
        val vm = createViewModel()
        vm.toggleRepeat()
        verify { audioRepository.setRepeatMode(true) }
    }

    // TC-3.23: PLAYBACK_RATES contains expected values
    @Test
    fun `PLAYBACK_RATES contains standard rates`() {
        val rates = QuranPlayerViewModel.PLAYBACK_RATES
        assertTrue(rates.contains(0.75f))
        assertTrue(rates.contains(1.0f))
        assertTrue(rates.contains(1.5f))
        assertTrue(rates.contains(2.0f))
    }

    // TC-3.24: ChapterInfo data class
    @Test
    fun `ChapterInfo holds correct values`() {
        val info = ChapterInfo(number = 1, name = "Al-Fatihah", reciterName = "Reciter 1")
        assertEquals(1, info.number)
        assertEquals("Al-Fatihah", info.name)
        assertEquals("Reciter 1", info.reciterName)
    }

    // TC-3.25: saveAudioPosition persists chapter and position
    @Test
    fun `saveAudioPosition persists to preferences`() = runTest {
        val vm = createViewModel()
        vm.configure(5, "Al-Ma'idah", reciterId = 1)
        vm.saveAudioPosition()

        coVerify { preferencesRepository.setLastAudioChapter(5) }
    }

    // TC-3.26: restoreAudioPosition returns false when no saved position
    @Test
    fun `restoreAudioPosition returns false when no saved data`() = runTest {
        coEvery { preferencesRepository.getLastAudioChapter() } returns null

        val vm = createViewModel()
        val restored = vm.restoreAudioPosition()

        assertFalse(restored)
    }

    // TC-3.27: restoreAudioPosition returns true when saved position exists
    @Test
    fun `restoreAudioPosition returns true when saved chapter exists`() = runTest {
        coEvery { preferencesRepository.getLastAudioChapter() } returns 5
        coEvery { preferencesRepository.getLastAudioVerse() } returns 10
        coEvery { preferencesRepository.getLastAudioPositionMs() } returns 50000L

        val vm = createViewModel()
        val restored = vm.restoreAudioPosition()

        assertTrue(restored)
    }

    // TC-3.28: getLastPlayedChapter returns pair when available
    @Test
    fun `getLastPlayedChapter returns pair when data exists`() = runTest {
        coEvery { preferencesRepository.getLastAudioChapter() } returns 3
        coEvery { preferencesRepository.getLastAudioPositionMs() } returns 12345L

        val vm = createViewModel()
        val result = vm.getLastPlayedChapter()

        assertNotNull(result)
        assertEquals(3, result!!.first)
        assertEquals(12345L, result.second)
    }

    // TC-3.29: getLastPlayedChapter returns null when no data
    @Test
    fun `getLastPlayedChapter returns null when no data`() = runTest {
        coEvery { preferencesRepository.getLastAudioChapter() } returns null

        val vm = createViewModel()
        val result = vm.getLastPlayedChapter()

        assertNull(result)
    }

    // ===== QA-3.2: QuranPlayerView Seeking & Progress =====

    // TC-3.30: seekTo delegates to audioRepository
    @Test
    fun `seekTo delegates position to audioRepository`() = runTest {
        val vm = createViewModel()
        vm.seekTo(30000L)
        verify { audioRepository.seekTo(30000L) }
    }

    // TC-3.31: seekToVerse finds timing and seeks
    @Test
    fun `seekToVerse seeks to correct position from timing`() = runTest {
        val timing = AyahTiming(ayah = 5, startTime = 15000, endTime = 20000)
        coEvery { audioRepository.getAyahTiming(any(), any(), 5) } returns timing

        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.seekToVerse(5)

        verify { audioRepository.seekTo(15000L) }
        assertEquals(5, vm.currentVerseNumber.value)
    }

    // TC-3.32: seekToVerse with no timing does not seek
    @Test
    fun `seekToVerse with no timing does not call seekTo`() = runTest {
        coEvery { audioRepository.getAyahTiming(any(), any(), 99) } returns null

        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.seekToVerse(99)

        verify(exactly = 0) { audioRepository.seekTo(any()) }
    }

    // TC-3.33: seekToNextVerse increments verse
    @Test
    fun `seekToNextVerse seeks to next verse number`() = runTest {
        val timing5 = AyahTiming(ayah = 5, startTime = 15000, endTime = 20000)
        val timing6 = AyahTiming(ayah = 6, startTime = 20000, endTime = 25000)
        coEvery { audioRepository.getAyahTiming(any(), any(), 5) } returns timing5
        coEvery { audioRepository.getAyahTiming(any(), any(), 6) } returns timing6

        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.seekToVerse(5)
        vm.seekToNextVerse()

        verify { audioRepository.seekTo(20000L) }
    }

    // TC-3.34: seekToNextVerse with null current verse does nothing
    @Test
    fun `seekToNextVerse with null current verse is no-op`() = runTest {
        val vm = createViewModel()
        vm.seekToNextVerse()
        verify(exactly = 0) { audioRepository.seekTo(any()) }
    }

    // TC-3.35: seekToPreviousVerse decrements verse
    @Test
    fun `seekToPreviousVerse seeks to previous verse number`() = runTest {
        val timing5 = AyahTiming(ayah = 5, startTime = 15000, endTime = 20000)
        val timing4 = AyahTiming(ayah = 4, startTime = 10000, endTime = 15000)
        coEvery { audioRepository.getAyahTiming(any(), any(), 5) } returns timing5
        coEvery { audioRepository.getAyahTiming(any(), any(), 4) } returns timing4

        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.seekToVerse(5)
        vm.seekToPreviousVerse()

        verify { audioRepository.seekTo(10000L) }
    }

    // TC-3.36: seekToPreviousVerse at verse 1 does nothing
    @Test
    fun `seekToPreviousVerse at verse 1 is no-op`() = runTest {
        val timing1 = AyahTiming(ayah = 1, startTime = 0, endTime = 5000)
        coEvery { audioRepository.getAyahTiming(any(), any(), 1) } returns timing1

        val vm = createViewModel()
        vm.configure(1, "Al-Fatihah", reciterId = 1)
        vm.seekToVerse(1)
        clearMocks(audioRepository, answers = false, verificationMarks = true)

        vm.seekToPreviousVerse()
        verify(exactly = 0) { audioRepository.seekTo(any()) }
    }

    // TC-3.37: cyclePlaybackRate cycles through rates
    @Test
    fun `cyclePlaybackRate cycles to next rate`() = runTest {
        val vm = createViewModel()
        assertEquals(1.0f, vm.playbackRate.value)

        vm.cyclePlaybackRate()
        assertEquals(1.25f, vm.playbackRate.value)
        verify { audioRepository.setPlaybackSpeed(1.25f) }
    }

    // TC-3.38: cyclePlaybackRate wraps around
    @Test
    fun `cyclePlaybackRate wraps around after last rate`() = runTest {
        val vm = createViewModel()
        val rateCount = QuranPlayerViewModel.PLAYBACK_RATES.size
        for (i in 0 until rateCount) {
            vm.cyclePlaybackRate()
        }
        assertEquals(0.75f, vm.playbackRate.value)
    }

    // TC-3.39: observePlayerState updates from flow
    @Test
    fun `playback state updates from player state flow`() = runTest {
        val vm = createViewModel()
        playerStateFlow.value = AudioPlayerState(
            playbackState = PlaybackState.PLAYING,
            currentPositionMs = 5000L,
            durationMs = 300000L
        )

        assertEquals(PlaybackState.PLAYING, vm.playbackState.value)
        assertEquals(5000L, vm.currentTimeMs.value)
        assertEquals(300000L, vm.durationMs.value)
    }

    // TC-3.40: AudioPlayerState progress percentage
    @Test
    fun `AudioPlayerState progressPercentage is calculated correctly`() {
        val state = AudioPlayerState(
            currentPositionMs = 50000L,
            durationMs = 100000L
        )
        assertEquals(50f, state.progressPercentage, 0.01f)
    }

    // TC-3.41: AudioPlayerState progress percentage with zero duration
    @Test
    fun `AudioPlayerState progressPercentage is 0 when duration is 0`() {
        val state = AudioPlayerState(
            currentPositionMs = 5000L,
            durationMs = 0L
        )
        assertEquals(0f, state.progressPercentage, 0.01f)
    }

    // TC-3.42: AudioPlayerState remaining time
    @Test
    fun `AudioPlayerState remainingTimeMs is correctly calculated`() {
        val state = AudioPlayerState(
            currentPositionMs = 30000L,
            durationMs = 100000L
        )
        assertEquals(70000L, state.remainingTimeMs)
    }

    // TC-3.43: AudioPlayerState remaining time never negative
    @Test
    fun `AudioPlayerState remainingTimeMs is never negative`() {
        val state = AudioPlayerState(
            currentPositionMs = 110000L,
            durationMs = 100000L
        )
        assertEquals(0L, state.remainingTimeMs)
    }

    // TC-3.44: AudioPlayerState isPlaying
    @Test
    fun `AudioPlayerState isPlaying returns true only when PLAYING`() {
        val playing = AudioPlayerState(playbackState = PlaybackState.PLAYING)
        val paused = AudioPlayerState(playbackState = PlaybackState.PAUSED)
        val idle = AudioPlayerState(playbackState = PlaybackState.IDLE)

        assertTrue(playing.isPlaying)
        assertFalse(paused.isPlaying)
        assertFalse(idle.isPlaying)
    }

    // TC-3.45: PlaybackState enum values
    @Test
    fun `PlaybackState has all expected states`() {
        val states = PlaybackState.values()
        assertTrue(states.contains(PlaybackState.IDLE))
        assertTrue(states.contains(PlaybackState.LOADING))
        assertTrue(states.contains(PlaybackState.PLAYING))
        assertTrue(states.contains(PlaybackState.PAUSED))
        assertTrue(states.contains(PlaybackState.STOPPED))
        assertTrue(states.contains(PlaybackState.ERROR))
    }
}
