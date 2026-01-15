package com.mushafimad.library.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.library.MushafLibrary
import com.mushafimad.library.data.audio.AudioPlayerService
import com.mushafimad.library.data.audio.AyahTimingService
import com.mushafimad.library.data.audio.PlaybackState
import com.mushafimad.library.data.audio.ReciterService
import com.mushafimad.library.domain.models.ReciterInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Quran audio player
 * Coordinates audio playback with verse timing synchronization
 * Internal implementation - not directly exposed, used via QuranPlayerView composable
 */
@HiltViewModel
internal class QuranPlayerViewModel @Inject constructor(
    private val audioPlayerService: AudioPlayerService,
    private val ayahTimingService: AyahTimingService,
    private val reciterService: ReciterService
) : ViewModel() {

    companion object {
        val PLAYBACK_RATES = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f)
        private const val VERSE_UPDATE_INTERVAL_MS = 100L
    }

    // Configuration
    private var chapterNumber: Int = 0
    private var chapterName: String = ""
    private var currentReciterId: Int = 1

    // State flows
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackRate = MutableStateFlow(1.0f)
    val playbackRate: StateFlow<Float> = _playbackRate.asStateFlow()

    private val _isRepeatEnabled = MutableStateFlow(false)
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled.asStateFlow()

    private val _currentVerseNumber = MutableStateFlow<Int?>(null)
    val currentVerseNumber: StateFlow<Int?> = _currentVerseNumber.asStateFlow()

    private val _availableReciters = MutableStateFlow<List<ReciterInfo>>(emptyList())
    val availableReciters: StateFlow<List<ReciterInfo>> = _availableReciters.asStateFlow()

    private val _selectedReciter = MutableStateFlow<ReciterInfo?>(null)
    val selectedReciter: StateFlow<ReciterInfo?> = _selectedReciter.asStateFlow()

    private var verseTrackingJob: Job? = null

    init {
        observePlayerState()
        observeReciters()
    }

    /**
     * Observe audio player state changes
     */
    private fun observePlayerState() {
        viewModelScope.launch {
            audioPlayerService.playerState.collect { playerState ->
                _playbackState.value = playerState.playbackState
                _currentTimeMs.value = playerState.currentPositionMs
                _durationMs.value = playerState.durationMs
                _isRepeatEnabled.value = playerState.isRepeatEnabled

                // Start/stop verse tracking based on playback state
                if (playerState.isPlaying) {
                    startVerseTracking()
                } else {
                    stopVerseTracking()
                }
            }
        }
    }

    /**
     * Observe reciter service for available reciters
     */
    private fun observeReciters() {
        viewModelScope.launch {
            reciterService.availableReciters.collect { reciters ->
                _availableReciters.value = reciters
            }
        }

        viewModelScope.launch {
            reciterService.selectedReciter.collect { reciter ->
                _selectedReciter.value = reciter
                reciter?.let { currentReciterId = it.id }
            }
        }
    }

    /**
     * Configure player for a specific chapter
     * @param chapterNumber Chapter number (1-114)
     * @param chapterName Chapter name (for display)
     * @param reciterId Optional reciter ID (uses current if not specified)
     */
    fun configure(chapterNumber: Int, chapterName: String, reciterId: Int? = null) {
        if (this.chapterNumber == chapterNumber && reciterId == null) {
            return // Already configured
        }

        this.chapterNumber = chapterNumber
        this.chapterName = chapterName

        reciterId?.let {
            currentReciterId = it
            // Update selected reciter if different
            reciterService.getReciterById(it)?.let { reciter ->
                reciterService.selectReciter(reciter)
            }
        }

        MushafLibrary.logger.info("Configured player for chapter $chapterNumber with reciter $currentReciterId")
    }

    /**
     * Load and optionally start playback for configured chapter
     * @param autoPlay Whether to start playing immediately
     */
    fun loadChapter(autoPlay: Boolean = false) {
        if (chapterNumber <= 0 || currentReciterId <= 0) {
            MushafLibrary.logger.error("Cannot load: invalid configuration")
            return
        }

        audioPlayerService.loadChapter(chapterNumber, currentReciterId, autoPlay)
    }

    /**
     * Start or resume playback
     */
    fun play() {
        audioPlayerService.play()
    }

    /**
     * Pause playback
     */
    fun pause() {
        audioPlayerService.pause()
    }

    /**
     * Toggle between play and pause
     */
    fun togglePlayback() {
        if (_playbackState.value == PlaybackState.PLAYING) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Stop playback and reset
     */
    fun stop() {
        audioPlayerService.stop()
        _currentVerseNumber.value = null
        stopVerseTracking()
    }

    /**
     * Seek to specific position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        audioPlayerService.seekTo(positionMs)
        updateCurrentVerse()
    }

    /**
     * Seek to a specific verse
     * @param verseNumber Verse number within the current chapter
     * @return true if seek was successful, false if verse timing not found
     */
    fun seekToVerse(verseNumber: Int): Boolean {
        val timing = ayahTimingService.getTiming(currentReciterId, chapterNumber, verseNumber)
        if (timing == null) {
            MushafLibrary.logger.warning("No timing found for verse $verseNumber in chapter $chapterNumber")
            return false
        }

        audioPlayerService.seekTo(timing.startTime.toLong())
        _currentVerseNumber.value = verseNumber
        MushafLibrary.logger.info("Seeked to verse $verseNumber at ${timing.startTime}ms")
        return true
    }

    /**
     * Seek to next verse if available
     * @return true if successful, false if no next verse
     */
    fun seekToNextVerse(): Boolean {
        val currentVerse = _currentVerseNumber.value ?: return false
        return seekToVerse(currentVerse + 1)
    }

    /**
     * Seek to previous verse if available
     * @return true if successful, false if no previous verse
     */
    fun seekToPreviousVerse(): Boolean {
        val currentVerse = _currentVerseNumber.value ?: return false
        if (currentVerse <= 1) return false
        return seekToVerse(currentVerse - 1)
    }

    /**
     * Cycle through available playback rates
     */
    fun cyclePlaybackRate() {
        val currentIndex = PLAYBACK_RATES.indexOf(_playbackRate.value)
        val nextIndex = (currentIndex + 1) % PLAYBACK_RATES.size
        val newRate = PLAYBACK_RATES[nextIndex]

        _playbackRate.value = newRate
        audioPlayerService.setPlaybackSpeed(newRate)

        MushafLibrary.logger.info("Playback rate changed to ${newRate}x")
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        val newValue = !_isRepeatEnabled.value
        audioPlayerService.setRepeatMode(newValue)
        _isRepeatEnabled.value = newValue
        MushafLibrary.logger.info("Repeat mode: $newValue")
    }

    /**
     * Select a different reciter
     * @param reciter The reciter to select
     * @param reloadAudio Whether to reload current chapter with new reciter
     */
    fun selectReciter(reciter: ReciterInfo, reloadAudio: Boolean = true) {
        reciterService.selectReciter(reciter)
        currentReciterId = reciter.id

        if (reloadAudio && chapterNumber > 0) {
            val wasPlaying = _playbackState.value == PlaybackState.PLAYING
            stop()
            loadChapter(autoPlay = wasPlaying)
        }

        MushafLibrary.logger.info("Reciter changed to: ${reciter.getDisplayName()}")
    }

    /**
     * Start tracking current verse based on playback position
     */
    private fun startVerseTracking() {
        if (verseTrackingJob?.isActive == true) return

        verseTrackingJob = viewModelScope.launch {
            while (isActive) {
                updateCurrentVerse()
                delay(VERSE_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop tracking current verse
     */
    private fun stopVerseTracking() {
        verseTrackingJob?.cancel()
        verseTrackingJob = null
    }

    /**
     * Update current verse based on playback position
     */
    private fun updateCurrentVerse() {
        if (chapterNumber <= 0 || currentReciterId <= 0) return

        val currentTimeMs = _currentTimeMs.value.toInt()
        val verseNumber = ayahTimingService.getCurrentVerse(
            currentReciterId,
            chapterNumber,
            currentTimeMs
        )

        if (verseNumber != null && verseNumber != _currentVerseNumber.value) {
            _currentVerseNumber.value = verseNumber
            MushafLibrary.logger.debug("Current verse: $verseNumber")
        }
    }

    /**
     * Get chapter info for display
     */
    fun getChapterInfo(): ChapterInfo {
        return ChapterInfo(
            number = chapterNumber,
            name = chapterName,
            reciterName = _selectedReciter.value?.getDisplayName() ?: ""
        )
    }

    /**
     * Check if player has valid configuration
     */
    fun hasValidConfiguration(): Boolean {
        return chapterNumber > 0 && currentReciterId > 0
    }

    override fun onCleared() {
        super.onCleared()
        stopVerseTracking()
    }
}

/**
 * Chapter information for display
 */
data class ChapterInfo(
    val number: Int,
    val name: String,
    val reciterName: String
)
