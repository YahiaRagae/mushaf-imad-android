package com.mushafimad.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Black-box checks against the library (from a consumer's seat - `source` or
 * `published`, this test runs against both flavours). Also serves as a
 * runnable repro for the bugs found in 0.2.1.
 */
@RunWith(AndroidJUnit4::class)
class MushafLibraryContractTest {

    @Test
    fun libraryAutoInitializesViaContentProvider() {
        assertTrue("MushafLibrary should be initialized before any app code runs", MushafLibrary.isInitialized())
    }

    @Test
    fun chapterRepositoryReturnsAll114Surahs() = runBlocking {
        val chapters = MushafLibrary.getChapterRepository().getAllChapters()
        assertEquals(114, chapters.size)
        assertEquals("Al-Fātiḥah", chapters.first().englishTitle)
        assertEquals(6, chapters.last().versesCount)
    }

    @Test
    fun audioRepositoryExposesAll18Reciters() = runBlocking {
        val reciters = MushafLibrary.getAudioRepository().getAllReciters()
        assertEquals(18, reciters.size)
        assertTrue(reciters.all { it.folderUrl.startsWith("http") })
    }

    /** Arabic verse search - cannot be driven through `adb shell input text`. */
    @Test
    fun verseSearchFindsArabicText() = runBlocking {
        val results = MushafLibrary.getVerseRepository().searchVerses("الرحمن")
        assertTrue("expected verse hits for الرحمن, got ${results.size}", results.isNotEmpty())
        assertTrue(results.all { it.pageNumber in 1..604 })
    }

    @Test
    fun verseLookupGivesTheStartPageOfASurah() = runBlocking {
        // The only way a consumer can map surah -> page: there is no Chapter.startPage.
        val verse = MushafLibrary.getVerseRepository().getVerse(2, 1)
        assertNotNull(verse)
        assertEquals(2, verse!!.pageNumber)
    }

    @Test
    fun bookmarksRoundTrip() = runBlocking {
        val repo = MushafLibrary.getBookmarkRepository()
        repo.deleteBookmarkForVerse(2, 255)
        val created = repo.addBookmark(chapterNumber = 2, verseNumber = 255, pageNumber = 42, note = "ayat al-kursi")
        assertTrue(repo.isVerseBookmarked(2, 255))
        assertEquals("ayat al-kursi", repo.getBookmarkById(created.id)?.note)
        repo.deleteBookmark(created.id)
        assertTrue(!repo.isVerseBookmarked(2, 255))
    }

    /**
     * FIXED in library 0.2.2: recordReadingSession() used to drop mushafType,
     * leaving ReadingHistoryEntity.mushafType as "" so getRecentHistory() blew
     * up doing MushafType.valueOf(""). That forced every consumer showing
     * reading history to wrap the call in runCatching just to avoid crashing
     * on first launch.
     *
     * As of 0.2.2 the round trip must simply work - no try/catch needed. This
     * inverts the old broken-behaviour repro that used to live here (see git
     * history / the `consumer-check` module this test grew out of).
     */
    @Test
    fun readingSessionRoundTripsWithCorrectMushafType() = runBlocking {
        val repo = MushafLibrary.getReadingHistoryRepository()

        repo.recordReadingSession(
            chapterNumber = 18,
            verseNumber = 10,
            pageNumber = 294,
            durationSeconds = 42,
            mushafType = MushafType.HAFS_1441
        )

        // Threw IllegalArgumentException in 0.2.1 - must not throw here.
        val history = repo.getRecentHistory(limit = 10)

        assertTrue(history.isNotEmpty())
        val entry = history.first { it.pageNumber == 294 }
        assertEquals(10, entry.verseNumber)
        assertEquals(MushafType.HAFS_1441, entry.mushafType)
    }

    /**
     * The reader must survive being reopened. In 0.1 the database was deleted and
     * recopied on every launch by two racing service instances, which both crashed
     * the app and destroyed all user data.
     */
    @Test
    fun lastReadPositionRoundTrips() = runBlocking {
        val repo = MushafLibrary.getReadingHistoryRepository()
        repo.updateLastReadPosition(MushafType.HAFS_1441, chapterNumber = 36, verseNumber = 1, pageNumber = 440)
        val pos = repo.getLastReadPosition(MushafType.HAFS_1441)
        assertNotNull(pos)
        assertEquals(440, pos?.pageNumber)
        assertEquals(36, pos?.chapterNumber)
    }
}
