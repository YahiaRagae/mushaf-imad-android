package com.mushafimad.library.data.repository

import com.mushafimad.library.data.cache.QuranDataCacheService
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Verse
import com.mushafimad.library.domain.repository.VerseRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VerseRepository
 * Internal API - not exposed to library consumers
 */
@Singleton
internal class VerseRepositoryImpl @Inject constructor(
    private val realmService: RealmService,
    private val cacheService: QuranDataCacheService
) : VerseRepository {

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
