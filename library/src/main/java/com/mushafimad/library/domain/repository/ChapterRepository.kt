package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.Chapter
import com.mushafimad.library.domain.models.ChaptersByHizb
import com.mushafimad.library.domain.models.ChaptersByPart
import com.mushafimad.library.domain.models.ChaptersByType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Chapter-related operations
 * Public API - exposed to library consumers
 */
interface ChapterRepository {

    /**
     * Get all chapters as a Flow for reactive updates
     */
    fun getAllChaptersFlow(): Flow<List<Chapter>>

    /**
     * Get all chapters (one-time fetch)
     */
    suspend fun getAllChapters(): List<Chapter>

    /**
     * Get a specific chapter by number
     */
    suspend fun getChapter(number: Int): Chapter?

    /**
     * Get the chapter that appears on a specific page
     */
    suspend fun getChapterForPage(pageNumber: Int): Chapter?

    /**
     * Get all chapters that appear on a specific page
     */
    suspend fun getChaptersOnPage(pageNumber: Int): List<Chapter>

    /**
     * Search chapters by query text
     */
    suspend fun searchChapters(query: String): List<Chapter>

    /**
     * Get chapters grouped by Part (Juz)
     */
    suspend fun getChaptersByPart(): List<ChaptersByPart>

    /**
     * Get chapters grouped by Hizb
     */
    suspend fun getChaptersByHizb(): List<ChaptersByHizb>

    /**
     * Get chapters grouped by type (Meccan/Medinan)
     */
    suspend fun getChaptersByType(): List<ChaptersByType>

    /**
     * Load and cache all chapters with progress callback
     */
    suspend fun loadAndCacheChapters(onProgress: ((Int) -> Unit)? = null)

    /**
     * Clear chapter cache
     */
    suspend fun clearCache()
}
