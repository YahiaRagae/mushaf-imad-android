package com.mushafimad.core.data.audio

import android.content.Context
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.AyahTiming
import com.mushafimad.core.domain.models.ReciterTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Service for loading and managing ayah timing data for audio synchronization
 * Internal implementation - not exposed in public API
 */
internal class AyahTimingService(
    private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Cache for loaded timing data
    private val timingCache = mutableMapOf<Int, ReciterTiming>()

    // Available reciter IDs (corresponding to read_X.json files)
    private val availableReciterIds = setOf(
        1, 5, 9, 10, 31, 32, 51, 53, 60, 62, 67, 74, 78, 106, 112, 118, 159, 256
    )

    /**
     * Load timing data for a specific reciter
     * Returns null if reciter timing file doesn't exist
     * Runs on background thread
     */
    suspend fun loadTimingForReciter(reciterId: Int): ReciterTiming? = withContext(Dispatchers.IO) {
        // Return cached data if available
        if (timingCache.containsKey(reciterId)) {
            return@withContext timingCache[reciterId]
        }

        // Check if this reciter has timing data
        if (!availableReciterIds.contains(reciterId)) {
            MushafLibrary.logger.warning("No timing data available for reciter $reciterId")
            return@withContext null
        }

        try {
            val fileName = "ayah_timing/read_$reciterId.json"
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val timing = json.decodeFromString<ReciterTiming>(jsonString)

            // Cache the loaded timing
            timingCache[reciterId] = timing

            MushafLibrary.logger.info("Loaded timing data for reciter $reciterId")
            timing
        } catch (e: Exception) {
            MushafLibrary.logger.error("Failed to load timing for reciter $reciterId", e)
            null
        }
    }

    /**
     * Get timing for a specific ayah
     * @param reciterId The reciter ID
     * @param chapterId The chapter (surah) number (1-114)
     * @param ayahNumber The ayah number within the chapter
     * @return AyahTiming if found, null otherwise
     */
    suspend fun getTiming(reciterId: Int, chapterId: Int, ayahNumber: Int): AyahTiming? {
        val timing = loadTimingForReciter(reciterId) ?: return null

        val chapter = timing.chapters.find { it.id == chapterId } ?: return null
        return chapter.ayaTiming.find { it.ayah == ayahNumber }
    }

    /**
     * Get the current verse being recited based on playback time
     * Applies -10ms correction to account for audio processing delay
     *
     * @param reciterId The reciter ID
     * @param chapterId The chapter (surah) number (1-114)
     * @param currentTimeMs Current playback position in milliseconds
     * @return The ayah number at the current time, or null if not found
     */
    suspend fun getCurrentVerse(reciterId: Int, chapterId: Int, currentTimeMs: Int): Int? {
        val timing = loadTimingForReciter(reciterId) ?: return null

        val chapter = timing.chapters.find { it.id == chapterId } ?: return null

        // Apply -10ms correction (iOS equivalent)
        val correctedTime = (currentTimeMs - 10).coerceAtLeast(0)

        // Find the ayah where correctedTime falls between start and end
        return chapter.ayaTiming.find { ayahTiming ->
            correctedTime >= ayahTiming.startTime && correctedTime <= ayahTiming.endTime
        }?.ayah
    }

    /**
     * Get all timing data for a chapter
     * @param reciterId The reciter ID
     * @param chapterId The chapter (surah) number (1-114)
     * @return List of AyahTiming for all verses in the chapter
     */
    suspend fun getChapterTimings(reciterId: Int, chapterId: Int): List<AyahTiming> {
        val timing = loadTimingForReciter(reciterId) ?: return emptyList()
        return timing.chapters.find { it.id == chapterId }?.ayaTiming ?: emptyList()
    }

    /**
     * Check if timing data is available for a reciter
     */
    fun hasTimingForReciter(reciterId: Int): Boolean {
        return availableReciterIds.contains(reciterId)
    }

    /**
     * Get list of all reciter IDs that have timing data
     */
    fun getAvailableReciterIds(): Set<Int> {
        return availableReciterIds
    }

    /**
     * Clear cached timing data (useful for memory management)
     */
    fun clearCache() {
        timingCache.clear()
        MushafLibrary.logger.info("Timing cache cleared")
    }

    /**
     * Preload timing data for a reciter (for better performance)
     */
    suspend fun preloadTiming(reciterId: Int) {
        if (!timingCache.containsKey(reciterId)) {
            loadTimingForReciter(reciterId)
        }
    }
}
