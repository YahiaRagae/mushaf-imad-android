package com.mushafimad.library.domain.repository

import com.mushafimad.library.data.audio.AudioPlayerState
import com.mushafimad.library.domain.models.AyahTiming
import com.mushafimad.library.domain.models.ReciterInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Quran audio playback and reciter management
 * Public API - exposed to library consumers
 */
interface AudioRepository {

    /**
     * Get all available reciters
     */
    fun getAllReciters(): List<ReciterInfo>

    /**
     * Get reciter by ID
     * @param reciterId The reciter ID
     * @return ReciterInfo if found, null otherwise
     */
    fun getReciterById(reciterId: Int): ReciterInfo?

    /**
     * Search reciters by name
     * @param query Search query
     * @param languageCode Language for search ("ar" for Arabic, "en" for English)
     * @return List of matching reciters
     */
    fun searchReciters(query: String, languageCode: String = "en"): List<ReciterInfo>

    /**
     * Get all Hafs reciters
     */
    fun getHafsReciters(): List<ReciterInfo>

    /**
     * Get default reciter
     */
    fun getDefaultReciter(): ReciterInfo

    /**
     * Observe audio player state
     */
    fun getPlayerStateFlow(): Flow<AudioPlayerState>

    /**
     * Load and optionally play a chapter
     * @param chapterNumber The chapter (surah) number (1-114)
     * @param reciterId The reciter ID
     * @param autoPlay Whether to start playback immediately
     */
    fun loadChapter(chapterNumber: Int, reciterId: Int, autoPlay: Boolean = false)

    /**
     * Start or resume playback
     */
    fun play()

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Stop playback
     */
    fun stop()

    /**
     * Seek to specific position
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Long)

    /**
     * Set playback speed
     * @param speed Playback speed (0.5 = half speed, 1.0 = normal, 2.0 = double speed)
     */
    fun setPlaybackSpeed(speed: Float)

    /**
     * Get current playback position in milliseconds
     */
    fun getCurrentPosition(): Long

    /**
     * Get total duration in milliseconds
     */
    fun getDuration(): Long

    /**
     * Check if player is currently playing
     */
    fun isPlaying(): Boolean

    /**
     * Get timing for a specific ayah
     * @param reciterId The reciter ID
     * @param chapterNumber The chapter (surah) number (1-114)
     * @param ayahNumber The ayah number within the chapter
     * @return AyahTiming if found, null otherwise
     */
    fun getAyahTiming(reciterId: Int, chapterNumber: Int, ayahNumber: Int): AyahTiming?

    /**
     * Get the current verse being recited based on playback position
     * @param reciterId The reciter ID
     * @param chapterNumber The chapter (surah) number (1-114)
     * @param currentTimeMs Current playback position in milliseconds
     * @return The ayah number at the current time, or null if not found
     */
    fun getCurrentVerse(reciterId: Int, chapterNumber: Int, currentTimeMs: Int): Int?

    /**
     * Get all timing data for a chapter
     * @param reciterId The reciter ID
     * @param chapterNumber The chapter (surah) number (1-114)
     * @return List of AyahTiming for all verses in the chapter
     */
    fun getChapterTimings(reciterId: Int, chapterNumber: Int): List<AyahTiming>

    /**
     * Check if timing data is available for a reciter
     * @param reciterId The reciter ID
     */
    fun hasTimingForReciter(reciterId: Int): Boolean

    /**
     * Preload timing data for better performance
     * @param reciterId The reciter ID
     */
    suspend fun preloadTiming(reciterId: Int)

    /**
     * Release player resources
     */
    fun release()
}
