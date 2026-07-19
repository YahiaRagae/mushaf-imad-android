package com.mushafimad.core.domain.repository

import com.mushafimad.core.domain.models.ChapterHeader
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Page
import com.mushafimad.core.domain.models.PageHeaderInfo

/**
 * Repository for Page-related operations
 * Public API - exposed to library consumers
 */
interface PageRepository {

    /**
     * Get a specific page by number
     */
    suspend fun getPage(number: Int): Page?

    /**
     * Get total number of pages (default 604)
     */
    suspend fun getTotalPages(): Int

    /**
     * Get page header information
     */
    suspend fun getPageHeaderInfo(
        pageNumber: Int,
        mushafType: MushafType = MushafType.HAFS_1441
    ): PageHeaderInfo?

    /**
     * Get the surah headers that start on a page, each with the normalized position of the
     * chapter's name, so a reader can draw the decorative name bar behind it.
     */
    suspend fun getChapterHeaders(
        pageNumber: Int,
        mushafType: MushafType = MushafType.HAFS_1441
    ): List<ChapterHeader>

    /**
     * Pre-cache a specific page
     */
    suspend fun cachePage(pageNumber: Int)

    /**
     * Pre-cache a range of pages
     */
    suspend fun cachePageRange(pageRange: IntRange)

    /**
     * Check if a page is cached
     */
    suspend fun isPageCached(pageNumber: Int): Boolean

    /**
     * Clear page cache
     */
    suspend fun clearPageCache(pageNumber: Int)

    /**
     * Clear all page caches
     */
    suspend fun clearAllPageCache()
}
