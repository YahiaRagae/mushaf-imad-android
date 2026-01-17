package com.mushafimad.core.data.repository

import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.core.domain.repository.VerseRepository
import com.mushafimad.core.internal.ServiceRegistry

/**
 * Implementation of VerseRepository
 * Internal API - not exposed to library consumers
 */
internal class VerseRepositoryImpl private constructor(
    private val realmService: RealmService,
    private val cacheService: QuranDataCacheService
) : VerseRepository {

    companion object {
        @Volatile private var instance: VerseRepositoryImpl? = null

        fun getInstance(): VerseRepository = instance ?: synchronized(this) {
            instance ?: VerseRepositoryImpl(
                ServiceRegistry.getRealmService(),
                ServiceRegistry.getQuranCacheService()
            ).also { instance = it }
        }
    }

    override suspend fun getVersesForPage(pageNumber: Int, mushafType: MushafType): List<Verse> {
        // Try cache first
        val cached = cacheService.getCachedVerses(pageNumber)
        if (cached != null) {
            return cached
        }

        // Otherwise fetch from database
        return realmService.getVersesForPage(pageNumber, mushafType)
    }

    override suspend fun getVersesForChapter(chapterNumber: Int): List<Verse> {
        // Try cache first
        val cached = cacheService.getCachedChapterVerses(chapterNumber)
        if (cached != null) {
            return cached
        }

        // Otherwise fetch from database
        return realmService.getVersesForChapter(chapterNumber)
    }

    override suspend fun getVerse(chapterNumber: Int, verseNumber: Int): Verse? {
        return realmService.getVerse(chapterNumber, verseNumber)
    }

    override suspend fun getSajdaVerses(): List<Verse> {
        return realmService.getSajdaVerses()
    }

    override suspend fun searchVerses(query: String): List<Verse> {
        return realmService.searchVerses(query)
    }

    override suspend fun getCachedVersesForPage(pageNumber: Int): List<Verse>? {
        return cacheService.getCachedVerses(pageNumber)
    }

    override suspend fun getCachedVersesForChapter(chapterNumber: Int): List<Verse>? {
        return cacheService.getCachedChapterVerses(chapterNumber)
    }
}
