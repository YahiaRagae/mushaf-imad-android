package com.mushafimad.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.domain.repository.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel for Quran audio player
 * Coordinates audio playback with verse timing synchronization
 * Internal implementation - not directly exposed, used via QuranPlayerView composable
 *
 * Dependencies are injected via Koin DI
 */
internal class QuranPlayerViewModel(
    private val audioRepository: AudioRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        val PLAYBACK_RATES = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f,2.25f,2.5f,2.75f,3.0f)
        private const val VERSE_UPDATE_INTERVAL_MS = 100L
    }

    // Configuration
    private var chapterNumber: Int = 0
    private var chapterName: String = ""
    private var currentReciterId: Int = 0  // Will be set by loadReciters() or configure()

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
        loadReciters()
    }

    /**
     * Observe audio player state changes
     */
    private fun observePlayerState() {
        viewModelScope.launch {
            audioRepository.getPlayerStateFlow().collect { playerState ->
                // Only update StateFlows when values actually change.
                // StateFlow skips equal values, so this prevents cascading recompositions
                // in composables that don't depend on position (e.g. PlayerControls) from
                // firing on every ~100ms position tick.
                if (_playbackState.value != playerState.playbackState)
                    _playbackState.value = playerState.playbackState
                if (_currentTimeMs.value != playerState.currentPositionMs)
                    _currentTimeMs.value = playerState.currentPositionMs
                if (_durationMs.value != playerState.durationMs)
                    _durationMs.value = playerState.durationMs
                if (_isRepeatEnabled.value != playerState.isRepeatEnabled)
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
     * Load available reciters and observe the selected reciter from ReciterService
     * Waits for ReciterService to finish initializing before collecting
     */
    private fun loadReciters() {
        viewModelScope.launch {
            // Wait for ReciterService to finish initializing
            val reciters = audioRepository.getAllReciters()
            _availableReciters.value = reciters
            MushafLibrary.logger.info("Loaded ${reciters.size} reciters")

            // NOW start observing selected reciter (after initialization is complete)
            // This ensures we get the final saved reciter, not intermediate values
            audioRepository.getSelectedReciterFlow().collect { reciter ->
                if (reciter != null) {
                    _selectedReciter.value = reciter

                    // Only update currentReciterId if it's not already set (0 means not set)
                    if (currentReciterId == 0) {
                        currentReciterId = reciter.id
                        MushafLibrary.logger.info("Initial reciter set to: ${reciter.nameArabic} (ID: ${reciter.id})")
                    }
                }
            }
        }
    }

    /**
     * Configure player for a specific chapter
     * @param chapterNumber Chapter number (1-114)
     * @param chapterName Chapter name (for display)
     * @param reciterId Optional reciter ID (uses current/default if not specified)
     */
    fun configure(chapterNumber: Int, chapterName: String, reciterId: Int? = null) {
        if (this.chapterNumber == chapterNumber && reciterId == null) {
            return // Already configured
        }

        this.chapterNumber = chapterNumber
        this.chapterName = chapterName

        if (reciterId != null) {
            currentReciterId = reciterId
            // Update selected reciter if different
            viewModelScope.launch {
                audioRepository.getReciterById(reciterId)?.let { reciter ->
                    _selectedReciter.value = reciter
                }
            }
            MushafLibrary.logger.info("Configured player for chapter $chapterNumber with reciter $currentReciterId")
        } else {
            // If no reciter specified, loadReciters() will set the default reciter
            MushafLibrary.logger.info("Configured player for chapter $chapterNumber (waiting for reciter to load)")
        }
    }

    /**
     * Load and optionally start playback for configured chapter
     * @param autoPlay Whether to start playing immediately
     */
    fun loadChapter(autoPlay: Boolean = false) {
        if (chapterNumber <= 0) {
            MushafLibrary.logger.error("Cannot load: chapter not configured")
            return
        }

        if (currentReciterId <= 0) {
            MushafLibrary.logger.error("Cannot load: reciter not set (currentReciterId=$currentReciterId)")
            return
        }

        MushafLibrary.logger.info("Loading chapter $chapterNumber with reciter $currentReciterId")
        audioRepository.loadChapter(chapterNumber, currentReciterId, autoPlay)
    }

    /**
     * Start or resume playback
     */
    fun play() {
        audioRepository.play()
    }

    /**
     * Pause playback
     */
    fun pause() {
        audioRepository.pause()
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
        audioRepository.stop()
        _currentVerseNumber.value = null
        stopVerseTracking()
    }

    /**
     * Seek to specific position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        audioRepository.seekTo(positionMs)
        viewModelScope.launch {
            updateCurrentVerse()
        }
    }

    /**
     * Seek to a specific verse
     * @param verseNumber Verse number within the current chapter
     * @return true if seek was successful, false if verse timing not found
     */
    fun seekToVerse(verseNumber: Int) {
        viewModelScope.launch {
            val timing = audioRepository.getAyahTiming(currentReciterId, chapterNumber, verseNumber)
            if (timing == null) {
                MushafLibrary.logger.warning("No timing found for verse $verseNumber in chapter $chapterNumber")
                return@launch
            }

            audioRepository.seekTo(timing.startTime.toLong())
            _currentVerseNumber.value = verseNumber
            MushafLibrary.logger.info("Seeked to verse $verseNumber at ${timing.startTime}ms")
        }
    }

    /**
     * Seek to next verse if available
     */
    fun seekToNextVerse() {
        val currentVerse = _currentVerseNumber.value ?: return
        seekToVerse(currentVerse + 1)
    }

    /**
     * Seek to previous verse if available
     */
    fun seekToPreviousVerse() {
        val currentVerse = _currentVerseNumber.value ?: return
        if (currentVerse <= 1) return
        seekToVerse(currentVerse - 1)
    }

    /**
     * Cycle through available playback rates
     */
    fun cyclePlaybackRate() {
        val currentIndex = PLAYBACK_RATES.indexOf(_playbackRate.value)
        val nextIndex = (currentIndex + 1) % PLAYBACK_RATES.size
        val newRate = PLAYBACK_RATES[nextIndex]

        _playbackRate.value = newRate
        audioRepository.setPlaybackSpeed(newRate)

        MushafLibrary.logger.info("Playback rate changed to ${newRate}x")
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        val newValue = !_isRepeatEnabled.value
        audioRepository.setRepeatMode(newValue)
        _isRepeatEnabled.value = newValue
        MushafLibrary.logger.info("Repeat mode: $newValue")
    }

    /**
     * Select a different reciter
     * @param reciter The reciter to select
     * @param reloadAudio Whether to reload current chapter with new reciter
     */
    fun selectReciter(reciter: ReciterInfo, reloadAudio: Boolean = true) {
        _selectedReciter.value = reciter
        currentReciterId = reciter.id

        // Persist the selection
        audioRepository.saveSelectedReciter(reciter)

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
    private suspend fun updateCurrentVerse() {
        if (chapterNumber <= 0 || currentReciterId <= 0) return

        val currentTimeMs = _currentTimeMs.value.toInt()
        val verseNumber = audioRepository.getCurrentVerse(
            currentReciterId,
            chapterNumber,
            currentTimeMs
        )

        verseNumber?.let { verse ->
            if (verse != _currentVerseNumber.value) {
                _currentVerseNumber.value = verse
                MushafLibrary.logger.debug("Current verse: $verse")
            }
            return
        }

        // No verse covers this position. Most reciters' timing data marks the
        // opening basmala as ayah 0, in which case getCurrentVerse already
        // returned it above. Where it is missing instead, anything before the
        // first verse is the chapter opening - report it as such, otherwise
        // seeking back to the start would leave the previous verse reported and
        // the reader parked wherever it happened to be.
        audioRepository.getAyahTiming(currentReciterId, chapterNumber, FIRST_VERSE)
            ?.takeIf { currentTimeMs < it.startTime && _currentVerseNumber.value != CHAPTER_OPENING }
            ?.let {
                _currentVerseNumber.value = CHAPTER_OPENING
                MushafLibrary.logger.debug("Chapter opening (basmala)")
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

    /**
     * Save current audio playback position
     */
    fun saveAudioPosition() {
        viewModelScope.launch {
            try {
                if (chapterNumber > 0) {
                    preferencesRepository.setLastAudioChapter(chapterNumber)
                    preferencesRepository.setLastAudioVerse(_currentVerseNumber.value)
                    preferencesRepository.setLastAudioPositionMs(_currentTimeMs.value)
                    MushafLibrary.logger.debug("Saved audio position: chapter=$chapterNumber, verse=${_currentVerseNumber.value}, position=${_currentTimeMs.value}ms")
                }
            } catch (e: Exception) {
                MushafLibrary.logger.error("Failed to save audio position", e)
            }
        }
    }

    /**
     * Restore last audio playback position
     * @return true if position was restored, false otherwise
     */
    suspend fun restoreAudioPosition(): Boolean {
        return try {
            val lastChapter = preferencesRepository.getLastAudioChapter()
            val lastVerse = preferencesRepository.getLastAudioVerse()
            val lastPosition = preferencesRepository.getLastAudioPositionMs()

            if (lastChapter != null && lastChapter > 0) {
                MushafLibrary.logger.info("Restoring audio position: chapter=$lastChapter, verse=$lastVerse, position=${lastPosition}ms")
                // Return true to indicate we have a saved position
                true
            } else {
                false
            }
        } catch (e: Exception) {
            MushafLibrary.logger.error("Failed to restore audio position", e)
            false
        }
    }

    /**
     * Get last played chapter info
     */
    suspend fun getLastPlayedChapter(): Pair<Int, Long>? {
        return try {
            val lastChapter = preferencesRepository.getLastAudioChapter()
            val lastPosition = preferencesRepository.getLastAudioPositionMs()
            if (lastChapter != null && lastChapter > 0) {
                Pair(lastChapter, lastPosition)
            } else {
                null
            }
        } catch (e: Exception) {
            MushafLibrary.logger.error("Failed to get last played chapter", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopVerseTracking()
        saveAudioPosition()
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

/**
 * The first numbered verse of a chapter.
 */
internal const val FIRST_VERSE = 1

/**
 * The opening basmala. It is recited before the first verse but is not itself a
 * verse of the chapter; reciter timing data represents it as ayah 0.
 */
internal const val CHAPTER_OPENING = 0
