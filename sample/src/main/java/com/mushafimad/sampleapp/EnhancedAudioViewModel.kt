package com.mushafimad.sampleapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import com.mushafimad.core.domain.repository.ReciterPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Enhanced UI state for advanced audio playback features
 */
data class EnhancedAudioUiState(
    val reciters: List<ReciterInfo> = emptyList(),
    val selectedReciter: ReciterInfo? = null,
    val playerState: AudioPlayerState = AudioPlayerState(),
    val currentVerse: Int? = null,
    val chapterTimings: List<AyahTiming> = emptyList(),
    val playbackSpeed: Float = 1.0f,
    val availablePlaybackSpeeds: List<Float> = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f),
    val isScrubbing: Boolean = false,
    val scrubbingProgress: Float = 0f,
    val shouldResumeAfterScrubbing: Boolean = false
)

/**
 * Enhanced ViewModel with scrubbing, verse navigation, and playback rate cycling
 * Demonstrates all audio playback features
 */
class EnhancedAudioViewModel(
    private val audioRepository: AudioRepository = MushafLibrary.getAudioRepository(),
    private val reciterPreferencesRepository: ReciterPreferencesRepository = MushafLibrary.getReciterPreferencesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhancedAudioUiState())
    val uiState: StateFlow<EnhancedAudioUiState> = _uiState.asStateFlow()

    init {
        loadReciters()
        observePlayerState()
        observePreferences()
    }

    private fun loadReciters() {
        val reciters = audioRepository.getAllReciters()
        _uiState.value = _uiState.value.copy(reciters = reciters)
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            audioRepository.getPlayerStateFlow().collect { playerState ->
                _uiState.value = _uiState.value.copy(playerState = playerState)

                // Update current verse based on playback position
                if (!_uiState.value.isScrubbing) {
                    updateCurrentVerse(playerState)
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                reciterPreferencesRepository.getSelectedReciterIdFlow(),
                reciterPreferencesRepository.getPlaybackSpeedFlow(),
                reciterPreferencesRepository.getRepeatModeFlow()
            ) { reciterId, speed, repeat ->
                Triple(reciterId, speed, repeat)
            }.collect { (reciterId, speed, repeat) ->
                // Load selected reciter
                val reciter = audioRepository.getReciterById(reciterId)
                    ?: audioRepository.getDefaultReciter()

                _uiState.value = _uiState.value.copy(
                    selectedReciter = reciter,
                    playbackSpeed = speed
                )

                // Apply preferences to player
                audioRepository.setPlaybackSpeed(speed)
                audioRepository.setRepeatMode(repeat)
            }
        }
    }

    private fun updateCurrentVerse(playerState: AudioPlayerState) {
        val reciterId = playerState.currentReciterId ?: return
        val chapterNumber = playerState.currentChapter ?: return

        if (playerState.playbackState == PlaybackState.PLAYING) {
            viewModelScope.launch {
                val currentVerse = audioRepository.getCurrentVerse(
                    reciterId = reciterId,
                    chapterNumber = chapterNumber,
                    currentTimeMs = playerState.currentPositionMs.toInt()
                )
                _uiState.value = _uiState.value.copy(currentVerse = currentVerse)
            }
        }
    }

    /**
     * Select a reciter and persist the choice
     */
    fun selectReciter(reciter: ReciterInfo) {
        viewModelScope.launch {
            reciterPreferencesRepository.setSelectedReciterId(reciter.id)
        }
    }

    /**
     * Load and play a chapter
     */
    fun loadChapter(chapterNumber: Int, autoPlay: Boolean = true) {
        viewModelScope.launch {
            val reciter = _uiState.value.selectedReciter ?: audioRepository.getDefaultReciter()

            // Load chapter timings if available
            if (audioRepository.hasTimingForReciter(reciter.id)) {
                val timings = audioRepository.getChapterTimings(reciter.id, chapterNumber)
                _uiState.value = _uiState.value.copy(chapterTimings = timings)
            }

            // Load and play audio
            audioRepository.loadChapter(chapterNumber, reciter.id, autoPlay)
        }
    }

    /**
     * Play or resume playback
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
     * Stop playback
     */
    fun stop() {
        audioRepository.stop()
        _uiState.value = _uiState.value.copy(
            currentVerse = null,
            chapterTimings = emptyList()
        )
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (audioRepository.isPlaying()) {
            pause()
        } else {
            play()
        }
    }

    /**
     * Cycle through playback speeds
     */
    fun cyclePlaybackSpeed() {
        val speeds = _uiState.value.availablePlaybackSpeeds
        val currentSpeed = _uiState.value.playbackSpeed
        val currentIndex = speeds.indexOf(currentSpeed).coerceAtLeast(0)
        val nextIndex = (currentIndex + 1) % speeds.size
        val newSpeed = speeds[nextIndex]

        viewModelScope.launch {
            reciterPreferencesRepository.setPlaybackSpeed(newSpeed)
        }
    }

    /**
     * Set specific playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            reciterPreferencesRepository.setPlaybackSpeed(speed)
        }
    }

    /**
     * Toggle repeat mode
     */
    fun toggleRepeat() {
        viewModelScope.launch {
            val currentRepeat = reciterPreferencesRepository.getRepeatMode()
            reciterPreferencesRepository.setRepeatMode(!currentRepeat)
        }
    }

    /**
     * Seek to specific position
     */
    fun seekTo(positionMs: Long) {
        audioRepository.seekTo(positionMs)
    }

    /**
     * Seek to specific verse
     */
    fun seekToVerse(verseNumber: Int) {
        viewModelScope.launch {
            val reciter = _uiState.value.selectedReciter ?: return@launch
            val chapterNumber = _uiState.value.playerState.currentChapter ?: return@launch

            val timing = audioRepository.getAyahTiming(reciter.id, chapterNumber, verseNumber)
            timing?.let {
                audioRepository.seekTo(it.startTime.toLong())
                _uiState.value = _uiState.value.copy(currentVerse = verseNumber)
            }
        }
    }

    /**
     * Seek to next verse
     */
    fun seekToNextVerse() {
        val currentVerse = _uiState.value.currentVerse ?: return
        val timings = _uiState.value.chapterTimings

        if (timings.isNotEmpty()) {
            val sortedVerses = timings.map { it.ayah }.sorted()
            val currentIndex = sortedVerses.indexOf(currentVerse)
            if (currentIndex >= 0 && currentIndex < sortedVerses.size - 1) {
                val nextVerse = sortedVerses[currentIndex + 1]
                seekToVerse(nextVerse)
            }
        }
    }

    /**
     * Seek to previous verse
     */
    fun seekToPreviousVerse() {
        val currentVerse = _uiState.value.currentVerse ?: return
        val timings = _uiState.value.chapterTimings

        if (timings.isNotEmpty()) {
            val sortedVerses = timings.map { it.ayah }.sorted()
            val currentIndex = sortedVerses.indexOf(currentVerse)
            if (currentIndex > 0) {
                val previousVerse = sortedVerses[currentIndex - 1]
                seekToVerse(previousVerse)
            }
        }
    }

    /**
     * Begin scrubbing (pause if playing)
     */
    fun beginScrubbing() {
        val isPlaying = audioRepository.isPlaying()
        if (isPlaying) {
            pause()
        }

        _uiState.value = _uiState.value.copy(
            isScrubbing = true,
            shouldResumeAfterScrubbing = isPlaying,
            scrubbingProgress = _uiState.value.playerState.progressPercentage / 100f
        )
    }

    /**
     * Update scrubbing progress (0.0 - 1.0)
     */
    fun updateScrubbing(progress: Float) {
        if (!_uiState.value.isScrubbing) return

        val clampedProgress = progress.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(scrubbingProgress = clampedProgress)

        // Preview the position
        val targetPosition = (clampedProgress * _uiState.value.playerState.durationMs).toLong()

        // Update current verse preview
        val reciterId = _uiState.value.playerState.currentReciterId ?: return
        val chapterNumber = _uiState.value.playerState.currentChapter ?: return
        viewModelScope.launch {
            val previewVerse = audioRepository.getCurrentVerse(
                reciterId = reciterId,
                chapterNumber = chapterNumber,
                currentTimeMs = targetPosition.toInt()
            )
            _uiState.value = _uiState.value.copy(currentVerse = previewVerse)
        }
    }

    /**
     * End scrubbing (seek to position and resume if needed)
     */
    fun endScrubbing() {
        if (!_uiState.value.isScrubbing) return

        val progress = _uiState.value.scrubbingProgress
        val targetPosition = (progress * _uiState.value.playerState.durationMs).toLong()
        val shouldResume = _uiState.value.shouldResumeAfterScrubbing

        audioRepository.seekTo(targetPosition)

        _uiState.value = _uiState.value.copy(
            isScrubbing = false,
            scrubbingProgress = 0f,
            shouldResumeAfterScrubbing = false
        )

        if (shouldResume) {
            play()
        }
    }

    /**
     * Preload timing data for current reciter
     */
    fun preloadTimingData() {
        viewModelScope.launch {
            val reciter = _uiState.value.selectedReciter ?: return@launch
            audioRepository.preloadTiming(reciter.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRepository.release()
    }
}
