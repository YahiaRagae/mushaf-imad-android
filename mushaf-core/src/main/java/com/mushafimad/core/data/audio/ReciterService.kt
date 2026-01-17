package com.mushafimad.core.data.audio

import android.content.Context
import android.content.SharedPreferences
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ReciterInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Service for managing Quran reciters
 * Handles loading reciter information, selection, and persistence
 * Internal implementation - not exposed in public API
 */
internal class ReciterService(
    private val context: Context,
    private val ayahTimingService: AyahTimingService,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_SELECTED_RECITER_ID = "selected_reciter_id"
        private const val DEFAULT_RECITER_ID = 1 // Ibrahim Al-Akdar
    }

    private val _availableReciters = MutableStateFlow<List<ReciterInfo>>(emptyList())
    val availableReciters: StateFlow<List<ReciterInfo>> = _availableReciters.asStateFlow()

    private val _selectedReciter = MutableStateFlow<ReciterInfo?>(null)
    val selectedReciter: StateFlow<ReciterInfo?> = _selectedReciter.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Service scope for background initialization
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Load reciters asynchronously on background thread
        serviceScope.launch {
            loadAvailableReciters()
        }
    }

    /**
     * Load available reciters from timing JSON files
     * Falls back to ReciterDataProvider if JSON loading fails
     * Runs on background thread
     */
    private suspend fun loadAvailableReciters() {
        try {
            val reciters = mutableListOf<ReciterInfo>()

            // Try to load from timing JSON files first
            val reciterIds = ayahTimingService.getAvailableReciterIds()
            var loadedFromJSON = false

            for (reciterId in reciterIds) {
                val reciterTiming = ayahTimingService.loadTimingForReciter(reciterId)
                if (reciterTiming != null) {
                    val reciterInfo = ReciterInfo(
                        id = reciterTiming.id,
                        nameArabic = reciterTiming.name,
                        nameEnglish = reciterTiming.nameEn,
                        rewaya = reciterTiming.rewaya,
                        folderUrl = reciterTiming.folderUrl
                    )
                    reciters.add(reciterInfo)
                    loadedFromJSON = true
                }
            }

            // Fallback to embedded data if JSON loading failed
            if (!loadedFromJSON) {
                MushafLibrary.logger.warning("No reciters loaded from JSON, using fallback data")
                reciters.addAll(ReciterDataProvider.allReciters)
            }

            // Sort by ID for consistent ordering
            reciters.sortBy { it.id }

            _availableReciters.value = reciters

            MushafLibrary.logger.info("Loaded ${reciters.count()} reciters")

            // Load saved reciter or use default
            val savedReciterId = prefs.getInt(KEY_SELECTED_RECITER_ID, DEFAULT_RECITER_ID)
            val savedReciter = reciters.find { it.id == savedReciterId }

            if (savedReciter != null) {
                _selectedReciter.value = savedReciter
                MushafLibrary.logger.info("Selected saved reciter: ${savedReciter.getDisplayName()} (ID: ${savedReciter.id})")
            } else if (reciters.isNotEmpty()) {
                // Use first reciter as default
                val defaultReciter = reciters.first()
                _selectedReciter.value = defaultReciter
                saveSelectedReciterId(defaultReciter.id)
                MushafLibrary.logger.info("Selected default reciter: ${defaultReciter.getDisplayName()} (ID: ${defaultReciter.id})")
            }

            _selectedReciter.value?.let { reciter ->
                MushafLibrary.logger.info("Audio base URL: ${reciter.folderUrl}")
            }

            _isLoading.value = false
        } catch (e: Exception) {
            MushafLibrary.logger.error("Failed to load reciters", e)
            _isLoading.value = false
        }
    }

    /**
     * Select a reciter and persist the choice
     * @param reciter The reciter to select
     */
    fun selectReciter(reciter: ReciterInfo) {
        _selectedReciter.value = reciter
        saveSelectedReciterId(reciter.id)
        MushafLibrary.logger.info("Selected reciter: ${reciter.getDisplayName()} (ID: ${reciter.id})")
    }

    /**
     * Get reciter by ID
     * @param reciterId The reciter ID
     * @return ReciterInfo if found, null otherwise
     */
    fun getReciterById(reciterId: Int): ReciterInfo? {
        return _availableReciters.value.find { it.id == reciterId }
    }

    /**
     * Get currently selected reciter's base URL
     * @return Base URL for audio files, or null if no reciter selected
     */
    fun getCurrentReciterBaseURL(): String? {
        return _selectedReciter.value?.folderUrl
    }

    /**
     * Get audio URL for a specific chapter with current reciter
     * @param chapterNumber Chapter number (1-114)
     * @return Full URL to MP3 file, or null if no reciter selected
     */
    fun getChapterAudioURL(chapterNumber: Int): String? {
        return _selectedReciter.value?.getAudioUrl(chapterNumber)
    }

    /**
     * Check if a reciter has timing data available
     * @param reciterId The reciter ID
     * @return true if timing data exists, false otherwise
     */
    fun hasTimingData(reciterId: Int): Boolean {
        return ayahTimingService.hasTimingForReciter(reciterId)
    }

    /**
     * Search reciters by name
     * @param query Search query
     * @param languageCode Language code ("ar" for Arabic, "en" for English)
     * @return List of matching reciters
     */
    fun searchReciters(query: String, languageCode: String = "en"): List<ReciterInfo> {
        val normalizedQuery = query.trim().lowercase()
        return _availableReciters.value.filter { reciter ->
            when (languageCode) {
                "ar" -> reciter.nameArabic.contains(normalizedQuery, ignoreCase = true)
                else -> reciter.nameEnglish.lowercase().contains(normalizedQuery)
            }
        }
    }

    /**
     * Get reciters by rewaya (recitation style)
     * @param rewaya The rewaya name (e.g., "حفص", "hafs")
     * @return List of reciters with matching rewaya
     */
    fun getRecitersByRewaya(rewaya: String): List<ReciterInfo> {
        val normalizedRewaya = rewaya.trim().lowercase()
        return _availableReciters.value.filter { reciter ->
            reciter.rewaya.lowercase().contains(normalizedRewaya)
        }
    }

    /**
     * Get all Hafs reciters
     */
    fun getHafsReciters(): List<ReciterInfo> {
        return _availableReciters.value.filter { it.isHafs }
    }

    /**
     * Save selected reciter ID to SharedPreferences
     */
    private fun saveSelectedReciterId(reciterId: Int) {
        prefs.edit().putInt(KEY_SELECTED_RECITER_ID, reciterId).apply()
    }

    /**
     * Reset to default reciter
     */
    fun resetToDefault() {
        val defaultReciter = _availableReciters.value.find { it.id == DEFAULT_RECITER_ID }
            ?: _availableReciters.value.firstOrNull()

        if (defaultReciter != null) {
            selectReciter(defaultReciter)
        }
    }
}
