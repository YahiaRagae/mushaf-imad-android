package com.mushafimad.library.data.repository

import com.mushafimad.library.domain.models.*

/**
 * Facade around the bundled Realm database that powers Quran metadata
 * Internal API - not exposed to library consumers
 */
internal interface RealmService {

    /**
     * Initialize the Realm database
     * Copies bundled database to app storage on first launch
     */
    suspend fun initialize()

    /**
     * Check if Realm is initialized
     */
    val isInitialized: Boolean

    /**
     * Get Realm instance for direct queries
     * Only use this when repository needs direct Realm access
     */
    fun getRealm(): io.realm.kotlin.Realm

    // MARK: - Chapter (Surah) Operations

    /**
     * Fetch all chapters sorted by number
     */
    suspend fun fetchAllChaptersAsync(): List<Chapter>

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

    // MARK: - Page Operations

    /**
     * Get a specific page by number
     */
    suspend fun getPage(number: Int): Page?

    /**
     * Fetch a page asynchronously
     */
    suspend fun fetchPageAsync(number: Int): Page?

    /**
     * Get total number of pages (default 604)
     */
    suspend fun getTotalPages(): Int

    // MARK: - Page Header Operations

    /**
     * Get page header information for a specific page
     */
    suspend fun getPageHeaderInfo(
        pageNumber: Int,
        mushafType: MushafType = MushafType.HAFS_1441
    ): PageHeaderInfo?

    // MARK: - Verse Operations

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

    // MARK: - Part (Juz) Operations

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

    /**
     * Fetch all parts sorted by number
     */
    suspend fun fetchAllPartsAsync(): List<Part>

    // MARK: - Quarter (Hizb) Operations

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

    /**
     * Fetch all quarters sorted by hizb number and fraction
     */
    suspend fun fetchAllQuartersAsync(): List<Quarter>

    // MARK: - Search Operations

    /**
     * Search verses by query text
     */
    suspend fun searchVerses(query: String): List<Verse>

    /**
     * Search chapters by query text
     */
    suspend fun searchChapters(query: String): List<Chapter>
}
