package com.mushafimad.core.data.cache

import com.mushafimad.core.data.repository.RealmService
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.PageHeaderInfo
import com.mushafimad.core.domain.models.Verse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Service to cache Quran data from Realm for quick access
 * Internal API - not exposed to library consumers
 */
internal class QuranDataCacheService(
    private val realmService: RealmService
) {
    private val mutex = Mutex()

    // Cached data structures
    private val cachedVerses = mutableMapOf<Int, List<Verse>>() // Page number -> Verses
    private val cachedPageHeaders = mutableMapOf<Int, PageHeaderInfo>() // Page number -> Header info
    private val cachedChapterVerses = mutableMapOf<Int, List<Verse>>() // Chapter number -> Verses

    // MARK: - Cache Management

    /**
     * Pre-fetch and cache data for a specific page
     */
    suspend fun cachePageData(pageNumber: Int) {
        // Cache verses for this page
        val verses = realmService.getVersesForPage(pageNumber)
        if (verses.isNotEmpty()) {
            mutex.withLock {
                cachedVerses[pageNumber] = verses
            }
        }

        // Cache page header
        val headerInfo = realmService.getPageHeaderInfo(pageNumber)
        if (headerInfo != null) {
            mutex.withLock {
                cachedPageHeaders[pageNumber] = headerInfo
            }
        }

        // Cache chapter verses for chapters on this page
        val chapters = realmService.getChaptersOnPage(pageNumber)
        for (chapter in chapters) {
            val alreadyCached = mutex.withLock { cachedChapterVerses.containsKey(chapter.number) }
            if (!alreadyCached) {
                val chapterVerses = realmService.getVersesForChapter(chapter.number)
                if (chapterVerses.isNotEmpty()) {
                    mutex.withLock {
                        cachedChapterVerses[chapter.number] = chapterVerses
                    }
                }
            }
        }
    }

    /**
     * Pre-fetch and cache data for a range of pages (e.g., for a chapter)
     */
    suspend fun cachePageRange(pageRange: IntRange) {
        for (pageNumber in pageRange) {
            cachePageData(pageNumber)
        }
    }

    // MARK: - Cache Retrieval

    /**
     * Get cached verses for a page (returns null if not cached)
     */
    suspend fun getCachedVerses(forPage: Int): List<Verse>? = mutex.withLock {
        cachedVerses[forPage]
    }

    /**
     * Get cached page header (returns null if not cached)
     */
    suspend fun getCachedPageHeader(forPage: Int): PageHeaderInfo? = mutex.withLock {
        cachedPageHeaders[forPage]
    }

    /**
     * Get cached verses for a chapter (returns null if not cached)
     */
    suspend fun getCachedChapterVerses(forChapter: Int): List<Verse>? = mutex.withLock {
        cachedChapterVerses[forChapter]
    }

    /**
     * Check if page data is cached
     */
    suspend fun isPageCached(pageNumber: Int): Boolean = mutex.withLock {
        cachedVerses.containsKey(pageNumber) && cachedPageHeaders.containsKey(pageNumber)
    }

    // MARK: - Cache Management

    /**
     * Clear cached data for a specific page
     */
    suspend fun clearPageCache(pageNumber: Int) = mutex.withLock {
        cachedVerses.remove(pageNumber)
        cachedPageHeaders.remove(pageNumber)
    }

    /**
     * Clear cached data for a chapter
     */
    suspend fun clearChapterCache(chapterNumber: Int) = mutex.withLock {
        cachedChapterVerses.remove(chapterNumber)
    }

    /**
     * Clear all cached data
     */
    suspend fun clearAllCache() = mutex.withLock {
        cachedVerses.clear()
        cachedPageHeaders.clear()
        cachedChapterVerses.clear()
    }

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats = mutex.withLock {
        CacheStats(
            cachedPagesCount = cachedVerses.size,
            cachedChaptersCount = cachedChapterVerses.size,
            totalVersesCached = cachedVerses.values.sumOf { it.size }
        )
    }
}

/**
 * Statistics about the current cache state
 * Public API - exposed to library consumers
 */
data class CacheStats(
    val cachedPagesCount: Int,
    val cachedChaptersCount: Int,
    val totalVersesCached: Int
)
