package com.mushafimad.core.data.cache

import com.mushafimad.core.data.repository.RealmService
import com.mushafimad.core.domain.models.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Singleton cache for chapters data to avoid reloading on every view appearance
 * Internal API - not exposed to library consumers
 */
internal class ChaptersDataCache(
    private val realmService: RealmService
) {
    private val mutex = Mutex()

    // Cached data
    private var _allChapters: List<Chapter> = emptyList()
    private var _allChaptersByPart: List<ChaptersByPart> = emptyList()
    private var _allChaptersByHizb: List<ChaptersByHizb> = emptyList()
    private var _allChaptersByType: List<ChaptersByType> = emptyList()

    private var _isCached = false
    private var _isPartsCached = false
    private var _isHizbCached = false
    private var _isTypeCached = false

    val allChapters: List<Chapter>
        get() = _allChapters

    val allChaptersByPart: List<ChaptersByPart>
        get() = _allChaptersByPart

    val allChaptersByHizb: List<ChaptersByHizb>
        get() = _allChaptersByHizb

    val allChaptersByType: List<ChaptersByType>
        get() = _allChaptersByType

    val isCached: Boolean
        get() = _isCached

    val isPartsCached: Boolean
        get() = _isPartsCached

    val isHizbCached: Boolean
        get() = _isHizbCached

    val isTypeCached: Boolean
        get() = _isTypeCached

    /**
     * Load and cache chapters data only (with progressive loading callback)
     * Grouped data is loaded on-demand via separate methods
     */
    suspend fun loadAndCache(onBatchLoaded: ((Int) -> Unit)? = null) = mutex.withLock {
        // Skip if already cached
        if (_isCached && _allChapters.isNotEmpty()) {
            return
        }

        // Load chapters
        val chapters = realmService.fetchAllChaptersAsync()
        _allChapters = chapters

        // Notify that chapters are ready
        onBatchLoaded?.invoke(_allChapters.size)

        _isCached = true
    }

    /**
     * Load and cache parts grouping (lazy-loaded) - directly from Parts in database
     */
    suspend fun loadPartsGrouping() = mutex.withLock {
        if (_isPartsCached) {
            return
        }

        val parts = realmService.fetchAllPartsAsync()

        // Create a lookup dictionary for chapters by number for efficient access
        val chaptersDict = _allChapters.associateBy { it.number }

        _allChaptersByPart = parts.mapNotNull { part ->
            // Get chapters that belong to this part
            val partChapters = realmService.getVersesForChapter(1) // We'll iterate through all chapters
                .filter { it.partNumber == part.number }
                .mapNotNull { chaptersDict[it.chapterNumber] }
                .distinctBy { it.number }
                .sortedBy { it.number }

            if (partChapters.isEmpty()) return@mapNotNull null

            // Get first verse from the part
            val firstVerse = realmService.getVersesForChapter(partChapters.first().number)
                .firstOrNull { it.partNumber == part.number }

            ChaptersByPart(
                id = part.identifier,
                partNumber = part.number,
                arabicTitle = part.arabicTitle,
                englishTitle = part.englishTitle,
                chapters = partChapters,
                firstPage = firstVerse?.pageNumber,
                firstVerse = firstVerse
            )
        }

        _isPartsCached = true
    }

    /**
     * Load and cache quarters grouping (lazy-loaded) - directly from Quarters in database
     */
    suspend fun loadQuartersGrouping() = mutex.withLock {
        if (_isHizbCached) {
            return
        }

        val quarters = realmService.fetchAllQuartersAsync()

        // Create a lookup dictionary for chapters by number for efficient access
        val chaptersDict = _allChapters.associateBy { it.number }

        // Group quarters by hizbNumber
        val hizbDict = quarters.groupBy { it.hizbNumber }

        // Build ChaptersByHizb structure
        _allChaptersByHizb = hizbDict.keys.sorted().mapNotNull { hizbNumber ->
            val quartersInHizb = hizbDict[hizbNumber] ?: return@mapNotNull null

            // Create quarters for all 4 fractions (0, 1, 2, 3)
            val quartersList = (0..3).mapNotNull { fraction ->
                val quarter = quartersInHizb.firstOrNull { it.hizbFraction == fraction }
                    ?: return@mapNotNull null

                // Get chapters that belong to this quarter
                val quarterChapters = _allChapters.filter { chapter ->
                    // Check if any verse in this chapter belongs to this quarter
                    realmService.getVersesForChapter(chapter.number)
                        .any { it.hizbNumber == hizbNumber }
                }.sortedBy { it.number }

                if (quarterChapters.isEmpty()) return@mapNotNull null

                // Get first verse from the quarter
                val firstVerse = quarterChapters.firstOrNull()?.let { firstChapter ->
                    realmService.getVersesForChapter(firstChapter.number)
                        .firstOrNull { it.hizbNumber == hizbNumber }
                }

                ChaptersByQuarter(
                    id = quarter.identifier,
                    quarterNumber = quarter.identifier,
                    hizbNumber = hizbNumber,
                    hizbFraction = fraction,
                    arabicTitle = quarter.arabicTitle,
                    englishTitle = quarter.englishTitle,
                    chapters = quarterChapters,
                    firstPage = firstVerse?.pageNumber,
                    firstVerse = firstVerse
                )
            }

            if (quartersList.isEmpty()) return@mapNotNull null

            ChaptersByHizb(
                id = hizbNumber,
                hizbNumber = hizbNumber,
                quarters = quartersList
            )
        }

        _isHizbCached = true
    }

    /**
     * Load and cache types grouping (lazy-loaded) - simple sort by isMeccan
     */
    fun loadTypesGrouping() {
        if (!_isCached || _allChapters.isEmpty()) {
            return
        }

        if (_isTypeCached) {
            return
        }

        // Simple sort by type
        val meccanChapters = _allChapters.filter { it.isMeccan }.sortedBy { it.number }
        val medinanChapters = _allChapters.filter { !it.isMeccan }.sortedBy { it.number }

        _allChaptersByType = listOfNotNull(
            if (meccanChapters.isNotEmpty()) {
                ChaptersByType(
                    id = "meccan",
                    type = "Meccan",
                    arabicType = "مكية",
                    chapters = meccanChapters,
                    firstPage = meccanChapters.firstOrNull()?.let { 1 }, // Simplified
                    firstVerse = null // Simplified - would need to fetch from Realm
                )
            } else null,
            if (medinanChapters.isNotEmpty()) {
                ChaptersByType(
                    id = "medinan",
                    type = "Medinan",
                    arabicType = "مدنية",
                    chapters = medinanChapters,
                    firstPage = medinanChapters.firstOrNull()?.let { 1 }, // Simplified
                    firstVerse = null // Simplified - would need to fetch from Realm
                )
            } else null
        )

        _isTypeCached = true
    }

    /**
     * Clear cache (useful for testing or force refresh)
     */
    suspend fun clearCache() = mutex.withLock {
        _allChapters = emptyList()
        _allChaptersByPart = emptyList()
        _allChaptersByHizb = emptyList()
        _allChaptersByType = emptyList()
        _isCached = false
        _isPartsCached = false
        _isHizbCached = false
        _isTypeCached = false
    }
}
