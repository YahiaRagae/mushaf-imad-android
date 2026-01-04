package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Page
import com.mushafimad.library.domain.models.PageHeaderInfo

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
