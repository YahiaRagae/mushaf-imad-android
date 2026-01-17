package com.mushafimad.sampleapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.audio.AudioPlayerState
import com.mushafimad.core.data.audio.PlaybackState
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterInfo
import com.mushafimad.core.domain.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for audio playback
 */
data class AudioUiState(
    val reciters: List<ReciterInfo> = emptyList(),
    val selectedReciter: ReciterInfo? = null,
    val playerState: AudioPlayerState = AudioPlayerState(),
    val currentVerse: Int? = null,
    val chapterTimings: List<AyahTiming> = emptyList(),
    val playbackSpeed: Float = 1.0f
)

/**
 * ViewModel demonstrating audio playback and reciter management
 */
class AudioViewModel(
    private val audioRepository: AudioRepository = MushafLibrary.getAudioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        loadReciters()
        observePlayerState()
    }

    private fun loadReciters() {
        val reciters = audioRepository.getAllReciters()
        val defaultReciter = audioRepository.getDefaultReciter()
        _uiState.value = _uiState.value.copy(
            reciters = reciters,
            selectedReciter = defaultReciter
        )
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            audioRepository.getPlayerStateFlow().collect { playerState ->
                _uiState.value = _uiState.value.copy(playerState = playerState)

                // Update current verse based on playback position
                updateCurrentVerse(playerState)
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
     * Select a reciter
     */
    fun selectReciter(reciter: ReciterInfo) {
        _uiState.value = _uiState.value.copy(selectedReciter = reciter)
    }

    /**
     * Search reciters by name
     */
    fun searchReciters(query: String, languageCode: String = "en"): List<ReciterInfo> {
        return audioRepository.searchReciters(query, languageCode)
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
            }
        }
    }

    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        audioRepository.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    /**
     * Get only Hafs reciters
     */
    fun getHafsReciters(): List<ReciterInfo> {
        return audioRepository.getHafsReciters()
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

    /**
     * Get progress percentage (0-100)
     */
    fun getProgressPercentage(): Float {
        val state = _uiState.value.playerState
        if (state.durationMs <= 0) return 0f
        return (state.currentPositionMs.toFloat() / state.durationMs.toFloat()) * 100f
    }

    override fun onCleared() {
        super.onCleared()
        audioRepository.release()
    }
}
