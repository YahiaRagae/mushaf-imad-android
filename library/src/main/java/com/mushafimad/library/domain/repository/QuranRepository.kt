package com.mushafimad.library.domain.repository

import com.mushafimad.library.data.cache.CacheStats
import com.mushafimad.library.domain.models.Part
import com.mushafimad.library.domain.models.Quarter

/**
 * Repository for general Quran data operations
 * Public API - exposed to library consumers
 */
interface QuranRepository {

    /**
     * Initialize the Quran database
     */
    suspend fun initialize()

    /**
     * Check if the database is initialized
     */
    fun isInitialized(): Boolean

    // Part (Juz) Operations

    /**
     * Get all parts (30 Juz)
     */
    suspend fun getAllParts(): List<Part>

    /**
     * Get a specific part by number
     */
    suspend fun getPart(number: Int): Part?

    /**
     * Get the part for a specific page
     */
    suspend fun getPartForPage(pageNumber: Int): Part?

    /**
     * Get the part for a specific verse
     */
    suspend fun getPartForVerse(chapterNumber: Int, verseNumber: Int): Part?

    // Quarter (Hizb) Operations

    /**
     * Get all quarters (Hizb fractions)
     */
    suspend fun getAllQuarters(): List<Quarter>

    /**
     * Get a specific quarter by hizb number and fraction
     */
    suspend fun getQuarter(hizbNumber: Int, fraction: Int): Quarter?

    /**
     * Get the quarter for a specific page
     */
    suspend fun getQuarterForPage(pageNumber: Int): Quarter?

    /**
     * Get the quarter for a specific verse
     */
    suspend fun getQuarterForVerse(chapterNumber: Int, verseNumber: Int): Quarter?

    // Cache Management

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats

    /**
     * Clear all caches
     */
    suspend fun clearAllCaches()
}
