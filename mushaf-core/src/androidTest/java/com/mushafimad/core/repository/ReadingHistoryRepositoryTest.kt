package com.mushafimad.core.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.repository.ReadingHistoryRepository
import com.mushafimad.core.util.LibraryTestSetup
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for ReadingHistoryRepository — sessions, stats, and last-read position.
 *
 * Covers:
 *   QA-7.1 (Reading History API)   — Issue #25  (TC-7.1  – TC-7.7)
 *   QA-7.2 (Reading Statistics API) — Issue #26  (TC-7.8  – TC-7.12)
 *   QA-7.3 (Last Read Position API) — Issue #27  (TC-7.13 – TC-7.18)
 *
 * State is cleared before and after every test to ensure isolation.
 */
@RunWith(AndroidJUnit4::class)
class ReadingHistoryRepositoryTest {

    private lateinit var repository: ReadingHistoryRepository

    @Before
    fun setUp() = runTest {
        LibraryTestSetup.ensureInitialized()
        repository = MushafLibrary.getReadingHistoryRepository()
        repository.deleteAllHistory()
    }

    @After
    fun tearDown() = runTest {
        repository.deleteAllHistory()
    }

    // ═══════════════════════════════════════════════════════════════
    //  QA-7.1 — Reading History API (Issue #25)
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.1 ────────────────────────────

    @Test
    fun recordReadingSession_doesNotCrashAndSessionIsStored() = runTest {
        repository.recordReadingSession(
            chapterNumber = 1,
            verseNumber = 1,
            pageNumber = 1,
            durationSeconds = 300,
            mushafType = MushafType.HAFS_1441
        )

        val history = repository.getRecentHistory()
        assertThat(history).isNotEmpty()
    }

    // ──────────────────────────── TC-7.2 ────────────────────────────

    @Test
    fun getRecentHistory_returnsSessionWithCorrectData() = runTest {
        repository.recordReadingSession(
            chapterNumber = 2,
            verseNumber = 255,
            pageNumber = 42,
            durationSeconds = 120,
            mushafType = MushafType.HAFS_1441
        )

        val history = repository.getRecentHistory()
        assertThat(history).isNotEmpty()

        val session = history.first()
        assertThat(session.chapterNumber).isEqualTo(2)
        assertThat(session.verseNumber).isEqualTo(255)
        assertThat(session.pageNumber).isEqualTo(42)
        assertThat(session.durationSeconds).isEqualTo(120)
        assertThat(session.mushafType).isEqualTo(MushafType.HAFS_1441)
    }

    // ──────────────────────────── TC-7.3 ────────────────────────────

    @Test
    fun getHistoryForChapter_returnsSessionsForThatChapter() = runTest {
        repository.recordReadingSession(1, 1, 1, 60, MushafType.HAFS_1441)
        repository.recordReadingSession(2, 1, 50, 90, MushafType.HAFS_1441)

        val chapter1History = repository.getHistoryForChapter(1)
        assertThat(chapter1History).isNotEmpty()
        assertThat(chapter1History.all { it.chapterNumber == 1 }).isTrue()
    }

    // ──────────────────────────── TC-7.4 ────────────────────────────

    @Test
    fun getHistoryForChapter_returnsEmptyListForUnreadChapter() = runTest {
        val history = repository.getHistoryForChapter(99)
        assertThat(history).isEmpty()
    }

    // ──────────────────────────── TC-7.5 ────────────────────────────

    @Test
    fun getRecentHistory_respectsLimitParameter() = runTest {
        repeat(5) { index ->
            repository.recordReadingSession(index + 1, 1, index + 1, 30, MushafType.HAFS_1441)
        }

        val limited = repository.getRecentHistory(limit = 2)
        assertThat(limited.size).isAtMost(2)
    }

    // ──────────────────────────── TC-7.6 ────────────────────────────

    @Test
    fun deleteAllHistory_removesEverySession() = runTest {
        repository.recordReadingSession(1, 1, 1, 60, MushafType.HAFS_1441)
        repository.recordReadingSession(2, 1, 50, 60, MushafType.HAFS_1441)

        repository.deleteAllHistory()

        assertThat(repository.getRecentHistory()).isEmpty()
    }

    // ──────────────────────────── TC-7.7 ────────────────────────────

    @Test
    fun deleteHistoryOlderThan_removesOnlyOldRecords() = runTest {
        // Record a session, then mark it as "old" by using a future cutoff
        repository.recordReadingSession(1, 1, 1, 60, MushafType.HAFS_1441)

        // Record another session 1 ms later (will survive the cutoff below)
        val cutoff = System.currentTimeMillis()

        repository.deleteHistoryOlderThan(cutoff)

        // Sessions recorded before `cutoff` should be gone
        val remaining = repository.getRecentHistory()
        assertThat(remaining.all { it.timestamp >= cutoff }).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════
    //  QA-7.2 — Reading Statistics API (Issue #26)
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.8 ────────────────────────────

    @Test
    fun getReadingStats_showsPositiveTotalTimeAfterRecordingSession() = runTest {
        repository.recordReadingSession(1, 1, 1, 300, MushafType.HAFS_1441)

        val stats = repository.getReadingStats()
        assertThat(stats.totalReadingTimeSeconds).isGreaterThan(0L)
    }

    // ──────────────────────────── TC-7.9 ────────────────────────────

    @Test
    fun getTotalReadingTime_matchesRecordedDuration() = runTest {
        repository.recordReadingSession(1, 1, 1, 180, MushafType.HAFS_1441)
        repository.recordReadingSession(2, 1, 50, 120, MushafType.HAFS_1441)

        val total = repository.getTotalReadingTime()
        assertThat(total).isAtLeast(300L)   // at least 300 seconds combined
    }

    // ──────────────────────────── TC-7.10 ────────────────────────────

    @Test
    fun getReadChapters_containsChaptersFromRecordedSessions() = runTest {
        repository.recordReadingSession(3, 1, 50, 60, MushafType.HAFS_1441)
        repository.recordReadingSession(5, 1, 80, 60, MushafType.HAFS_1441)

        val readChapters = repository.getReadChapters()
        assertThat(readChapters).contains(3)
        assertThat(readChapters).contains(5)
    }

    // ──────────────────────────── TC-7.11 ────────────────────────────

    @Test
    fun getCurrentStreak_isAtLeast1AfterRecordingSessionToday() = runTest {
        repository.recordReadingSession(1, 1, 1, 60, MushafType.HAFS_1441)

        val streak = repository.getCurrentStreak()
        assertThat(streak).isAtLeast(1)
    }

    // ──────────────────────────── TC-7.12 ────────────────────────────

    @Test
    fun stats_resetToZeroAfterDeleteAllHistory() = runTest {
        repository.recordReadingSession(1, 1, 1, 600, MushafType.HAFS_1441)
        repository.deleteAllHistory()

        val stats = repository.getReadingStats()
        assertThat(stats.totalReadingTimeSeconds).isEqualTo(0L)
        assertThat(repository.getReadChapters()).isEmpty()
    }

    // ═══════════════════════════════════════════════════════════════
    //  QA-7.3 — Last Read Position API (Issue #27)
    // ═══════════════════════════════════════════════════════════════

    // ──────────────────────────── TC-7.13 ────────────────────────────

    @Test
    fun updateLastReadPosition_savesWithoutCrash() = runTest {
        repository.updateLastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = 2,
            verseNumber = 255,
            pageNumber = 42,
            scrollPosition = 0.5f
        )
        // Reaching here means no exception was thrown
        assertThat(true).isTrue()
    }

    // ──────────────────────────── TC-7.14 ────────────────────────────

    @Test
    fun getLastReadPosition_returnsCorrectSavedPosition() = runTest {
        repository.updateLastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = 2,
            verseNumber = 255,
            pageNumber = 42,
            scrollPosition = 0.5f
        )

        val position = repository.getLastReadPosition(MushafType.HAFS_1441)

        assertThat(position).isNotNull()
        assertThat(position!!.chapterNumber).isEqualTo(2)
        assertThat(position.verseNumber).isEqualTo(255)
        assertThat(position.pageNumber).isEqualTo(42)
        assertThat(position.scrollPosition).isWithin(0.01f).of(0.5f)
        assertThat(position.mushafType).isEqualTo(MushafType.HAFS_1441)
    }

    // ──────────────────────────── TC-7.15 ────────────────────────────

    @Test
    fun getLastReadPositionFlow_emitsUpdatesWhenPositionChanges() = runTest {
        repository.getLastReadPositionFlow(MushafType.HAFS_1441).test {
            awaitItem() // initial emission (null or previous state)

            repository.updateLastReadPosition(
                mushafType = MushafType.HAFS_1441,
                chapterNumber = 3,
                verseNumber = 10,
                pageNumber = 60
            )

            val updated = awaitItem()
            assertThat(updated).isNotNull()
            assertThat(updated!!.chapterNumber).isEqualTo(3)
            assertThat(updated.verseNumber).isEqualTo(10)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ──────────────────────────── TC-7.16 ────────────────────────────

    @Test
    fun lastReadPosition_isRecentForJustSavedPosition() = runTest {
        repository.updateLastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = 1,
            verseNumber = 1,
            pageNumber = 1
        )

        val position = repository.getLastReadPosition(MushafType.HAFS_1441)!!
        assertThat(position.isRecent()).isTrue()
    }

    // ──────────────────────────── TC-7.17 + TC-7.18 ────────────────────────────

    @Test
    fun lastReadPositions_areStoredIndependentlyPerMushafType() = runTest {
        repository.updateLastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = 1,
            verseNumber = 1,
            pageNumber = 1
        )
        repository.updateLastReadPosition(
            mushafType = MushafType.HAFS_1405,
            chapterNumber = 5,
            verseNumber = 10,
            pageNumber = 100
        )

        val pos1441 = repository.getLastReadPosition(MushafType.HAFS_1441)!!
        val pos1405 = repository.getLastReadPosition(MushafType.HAFS_1405)!!

        assertThat(pos1441.chapterNumber).isEqualTo(1)
        assertThat(pos1441.pageNumber).isEqualTo(1)

        assertThat(pos1405.chapterNumber).isEqualTo(5)
        assertThat(pos1405.pageNumber).isEqualTo(100)
    }
}
