package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Verse

/**
 * Repository for Verse-related operations
 * Public API - exposed to library consumers
 */
interface VerseRepository {

    /**
     * Get all verses for a specific page
     */
    suspend fun getVersesForPage(
        pageNumber: Int,
        mushafType: MushafType = MushafType.HAFS_1441
    ): List<Verse>

    /**
     * Get all verses for a specific chapter
     */
    suspend fun getVersesForChapter(chapterNumber: Int): List<Verse>

    /**
     * Get a specific verse by chapter and verse number
     */
    suspend fun getVerse(chapterNumber: Int, verseNumber: Int): Verse?

    /**
     * Get all verses that contain sajda (prostration)
     */
    suspend fun getSajdaVerses(): List<Verse>

    /**
     * Search verses by query text
     */
    suspend fun searchVerses(query: String): List<Verse>

    /**
     * Get cached verses for a page (returns null if not cached)
     */
    suspend fun getCachedVersesForPage(pageNumber: Int): List<Verse>?

    /**
     * Get cached verses for a chapter (returns null if not cached)
     */
    suspend fun getCachedVersesForChapter(chapterNumber: Int): List<Verse>?
}
