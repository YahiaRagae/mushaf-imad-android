package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.MushafType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user preferences and settings
 * Public API - exposed to library consumers
 */
interface PreferencesRepository {

    /**
     * Get the selected Mushaf type as a Flow
     */
    fun getMushafTypeFlow(): Flow<MushafType>

    /**
     * Set the selected Mushaf type
     */
    suspend fun setMushafType(mushafType: MushafType)

    /**
     * Get the current page number as a Flow
     */
    fun getCurrentPageFlow(): Flow<Int>

    /**
     * Set the current page number
     */
    suspend fun setCurrentPage(pageNumber: Int)

    /**
     * Get the last read chapter number as a Flow
     */
    fun getLastReadChapterFlow(): Flow<Int?>

    /**
     * Set the last read chapter number
     */
    suspend fun setLastReadChapter(chapterNumber: Int)

    /**
     * Get the last read verse as a Flow
     */
    fun getLastReadVerseFlow(): Flow<Pair<Int, Int>?>  // (chapterNumber, verseNumber)

    /**
     * Set the last read verse
     */
    suspend fun setLastReadVerse(chapterNumber: Int, verseNumber: Int)

    /**
     * Get the font size multiplier as a Flow
     */
    fun getFontSizeMultiplierFlow(): Flow<Float>

    /**
     * Set the font size multiplier (0.5 to 2.0)
     */
    suspend fun setFontSizeMultiplier(multiplier: Float)

    /**
     * Get whether to show translation
     */
    fun getShowTranslationFlow(): Flow<Boolean>

    /**
     * Set whether to show translation
     */
    suspend fun setShowTranslation(show: Boolean)

    /**
     * Clear all preferences
     */
    suspend fun clearAll()
}
