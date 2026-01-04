package com.mushafimad.library.domain.models

/**
 * Information about a Quran reciter
 * Public API - exposed to library consumers
 */
data class ReciterInfo(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val rewaya: String,           // Recitation style (e.g., "حفص عن عاصم")
    val folderUrl: String         // Base URL for audio files
) {
    /**
     * Get reciter display name based on language
     */
    fun getDisplayName(languageCode: String = "en"): String {
        return if (languageCode == "ar") nameArabic else nameEnglish
    }

    /**
     * Get audio URL for a specific chapter (surah)
     */
    fun getAudioUrl(chapterNumber: Int): String {
        // Format: https://server.../001.mp3, https://server.../002.mp3, etc.
        val paddedChapter = chapterNumber.toString().padStart(3, '0')
        return "$folderUrl$paddedChapter.mp3"
    }

    /**
     * Check if this reciter uses Hafs recitation
     */
    val isHafs: Boolean
        get() = rewaya.contains("حفص", ignoreCase = true) ||
                rewaya.contains("hafs", ignoreCase = true)

    /**
     * Check if this reciter uses Warsh recitation
     */
    val isWarsh: Boolean
        get() = rewaya.contains("ورش", ignoreCase = true) ||
                rewaya.contains("warsh", ignoreCase = true)
}
