package com.mushafimad.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.domain.models.MushafType
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A recorded reading session must be readable again.
 *
 * In 0.2.1 it was not: recordReadingSession() dropped its verseNumber and
 * mushafType arguments before they reached the database, so every row was
 * written with an empty mushaf type, and reading one back did
 * MushafType.valueOf("") and threw. Nothing caught it because until 0.2.1
 * nothing ever recorded a session, so the table was always empty and the mapper
 * never ran. 0.2.1 then started recording sessions automatically - which meant
 * any consumer that displayed reading history crashed on its first visit.
 */
@RunWith(AndroidJUnit4::class)
class ReadingHistoryRoundTripTest {

    private val repo = MushafLibrary.getReadingHistoryRepository()

    @Test
    fun recordedSessionCanBeReadBack() = runBlocking {
        repo.recordReadingSession(
            chapterNumber = 18,
            verseNumber = 10,
            pageNumber = 294,
            durationSeconds = 42,
            mushafType = MushafType.HAFS_1441
        )

        // This threw IllegalArgumentException in 0.2.1
        val history = repo.getRecentHistory(limit = 10)

        assertThat(history).isNotEmpty()
        val entry = history.first { it.pageNumber == 294 }
        assertThat(entry.chapterNumber).isEqualTo(18)
        assertThat(entry.verseNumber).isEqualTo(10)      // was silently dropped
        assertThat(entry.durationSeconds).isEqualTo(42)
        assertThat(entry.mushafType).isEqualTo(MushafType.HAFS_1441)   // was ""
    }

    @Test
    fun otherHistoryQueriesShareTheMapperAndAlsoWork() = runBlocking {
        repo.recordReadingSession(
            chapterNumber = 2,
            verseNumber = 255,
            pageNumber = 42,
            durationSeconds = 30,
            mushafType = MushafType.HAFS_1441
        )

        val now = System.currentTimeMillis()
        assertThat(repo.getHistoryForDateRange(now - 60_000, now + 60_000)).isNotEmpty()
        assertThat(repo.getHistoryForChapter(2)).isNotEmpty()
    }

    @Test
    fun statsStillWork() = runBlocking {
        repo.recordReadingSession(
            chapterNumber = 1,
            verseNumber = 1,
            pageNumber = 1,
            durationSeconds = 15,
            mushafType = MushafType.HAFS_1441
        )

        assertThat(repo.getTotalReadingTime()).isGreaterThan(0L)
        assertThat(repo.getReadChapters()).contains(1)
    }
}
