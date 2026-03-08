package com.mushafimad.core.data.repository

import com.mushafimad.core.data.local.dao.ReadingHistoryDao
import com.mushafimad.core.data.local.entities.LastReadPositionEntity
import com.mushafimad.core.data.local.entities.ReadingHistoryEntity
import com.mushafimad.core.domain.models.MushafType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mongodb.kbson.ObjectId

class ReadingHistoryRepositoryTest {

    private lateinit var dao: ReadingHistoryDao
    private lateinit var repository: DefaultReadingHistoryRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = DefaultReadingHistoryRepository(dao)
    }

    private fun createHistoryEntity(
        chapterNumber: Int = 1,
        verseNumber: Int = 1,
        pageNumber: Int = 1,
        durationSeconds: Int = 300,
        mushafType: String = "HAFS_1441",
        timestamp: Long = System.currentTimeMillis()
    ): ReadingHistoryEntity {
        return ReadingHistoryEntity().apply {
            this.id = ObjectId()
            this.chapterNumber = chapterNumber
            this.verseNumber = verseNumber
            this.pageNumber = pageNumber
            this.durationSeconds = durationSeconds
            this.mushafType = mushafType
            this.timestamp = timestamp
        }
    }

    private fun createLastReadPositionEntity(
        mushafType: String = "HAFS_1441",
        chapterNumber: Int = 2,
        verseNumber: Int = 255,
        pageNumber: Int = 42
    ): LastReadPositionEntity {
        return LastReadPositionEntity().apply {
            this.mushafType = mushafType
            this.chapterNumber = chapterNumber
            this.verseNumber = verseNumber
            this.pageNumber = pageNumber
            this.lastReadAt = System.currentTimeMillis()
            this.scrollPosition = 0.5f
        }
    }

    // TC-7.1: recordReadingSession — verify no crash, session recorded
    @Test
    fun `recordReadingSession completes without crash`() = runTest {
        repository.recordReadingSession(1, 1, 1, 300, MushafType.HAFS_1441)

        coVerify {
            dao.insertHistory(
                chapterNumber = 1,
                pageNumber = 1,
                timestamp = any(),
                durationSeconds = 300
            )
        }
    }

    // TC-7.2: getRecentHistory — verify returns recorded session with correct data
    @Test
    fun `getRecentHistory returns recorded session with correct data`() = runTest {
        val entity = createHistoryEntity(chapterNumber = 1, verseNumber = 1, pageNumber = 1, durationSeconds = 300)
        coEvery { dao.getRecentHistory(50) } returns listOf(entity)

        val result = repository.getRecentHistory()

        assertEquals(1, result.size)
        assertEquals(1, result.first().chapterNumber)
        assertEquals(1, result.first().verseNumber)
        assertEquals(1, result.first().pageNumber)
        assertEquals(300, result.first().durationSeconds)
        assertEquals(MushafType.HAFS_1441, result.first().mushafType)
    }

    // TC-7.3: getHistoryForChapter(1) — verify returns sessions for chapter 1
    @Test
    fun `getHistoryForChapter returns sessions for chapter 1`() = runTest {
        val entities = listOf(
            createHistoryEntity(chapterNumber = 1, verseNumber = 1),
            createHistoryEntity(chapterNumber = 1, verseNumber = 3)
        )
        coEvery { dao.getHistoryForChapter(1) } returns entities

        val result = repository.getHistoryForChapter(1)

        assertEquals(2, result.size)
        assertTrue(result.all { it.chapterNumber == 1 })
    }

    // TC-7.4: getHistoryForChapter(99) — verify returns empty list for chapter with no history
    @Test
    fun `getHistoryForChapter returns empty list for chapter with no history`() = runTest {
        coEvery { dao.getHistoryForChapter(99) } returns emptyList()

        val result = repository.getHistoryForChapter(99)

        assertTrue(result.isEmpty())
    }

    // TC-7.5: getRecentHistory(limit = 2) — verify only 2 returned
    @Test
    fun `getRecentHistory with limit returns only requested count`() = runTest {
        val entities = listOf(
            createHistoryEntity(chapterNumber = 1),
            createHistoryEntity(chapterNumber = 2)
        )
        coEvery { dao.getRecentHistory(2) } returns entities

        val result = repository.getRecentHistory(limit = 2)

        assertEquals(2, result.size)
        coVerify { dao.getRecentHistory(2) }
    }

    // TC-7.6: deleteAllHistory — verify history is cleared
    @Test
    fun `deleteAllHistory clears history`() = runTest {
        repository.deleteAllHistory()

        coVerify { dao.deleteAllHistory() }
    }

    // TC-7.7: deleteHistoryOlderThan(timestamp) — verify only old records removed
    @Test
    fun `deleteHistoryOlderThan removes only old records`() = runTest {
        val cutoffTimestamp = System.currentTimeMillis() - 86400000L

        repository.deleteHistoryOlderThan(cutoffTimestamp)

        coVerify { dao.deleteHistoryOlderThan(cutoffTimestamp) }
    }

    // TC-7.8: getReadingStats — verify totalReadingTimeSeconds > 0
    @Test
    fun `getReadingStats returns stats with totalReadingTimeSeconds greater than zero`() = runTest {
        val entities = listOf(createHistoryEntity(durationSeconds = 300))
        coEvery { dao.getAllHistory() } returns entities

        val result = repository.getReadingStats()

        assertTrue(result.totalReadingTimeSeconds > 0)
        assertEquals(300L, result.totalReadingTimeSeconds)
    }

    // TC-7.9: getTotalReadingTime — verify returns total seconds matching recorded sessions
    @Test
    fun `getTotalReadingTime returns total seconds matching sessions`() = runTest {
        val entities = listOf(
            createHistoryEntity(durationSeconds = 300),
            createHistoryEntity(durationSeconds = 200)
        )
        coEvery { dao.getAllHistory() } returns entities

        val result = repository.getTotalReadingTime()

        assertEquals(500L, result)
    }

    // TC-7.10: getReadChapters — verify returns list containing read chapters
    @Test
    fun `getReadChapters returns list of read chapters`() = runTest {
        val entities = listOf(
            createHistoryEntity(chapterNumber = 1),
            createHistoryEntity(chapterNumber = 2),
            createHistoryEntity(chapterNumber = 1)
        )
        coEvery { dao.getAllHistory() } returns entities

        val result = repository.getReadChapters()

        assertTrue(result.contains(1))
        assertTrue(result.contains(2))
        assertEquals(2, result.size)
        assertEquals(listOf(1, 2), result)
    }

    // TC-7.11: getCurrentStreak — verify returns >= 1 after recording session today
    @Test
    fun `getCurrentStreak returns at least 1 after recording session today`() = runTest {
        val todayEntity = createHistoryEntity(timestamp = System.currentTimeMillis())
        coEvery { dao.getRecentHistory(1000) } returns listOf(todayEntity)

        val result = repository.getCurrentStreak()

        assertTrue(result >= 1)
    }

    // TC-7.12: After deleteAllHistory, verify all stats reset to zero
    @Test
    fun `stats reset to zero after deleteAllHistory`() = runTest {
        coEvery { dao.getAllHistory() } returns emptyList()

        val stats = repository.getReadingStats()

        assertEquals(0L, stats.totalReadingTimeSeconds)
        assertEquals(0, stats.totalPagesRead)
        assertEquals(0, stats.totalChaptersRead)
        assertEquals(0, stats.totalVersesRead)
        assertEquals(0, stats.currentStreak)
    }

    // TC-7.13: updateLastReadPosition — verify saves
    @Test
    fun `updateLastReadPosition saves position`() = runTest {
        repository.updateLastReadPosition(MushafType.HAFS_1441, 2, 255, 42, 0.5f)

        coVerify {
            dao.updateLastReadPosition(
                mushafType = "HAFS_1441",
                chapterNumber = 2,
                verseNumber = 255,
                pageNumber = 42,
                timestamp = any()
            )
        }
    }

    // TC-7.14: getLastReadPosition — verify returns saved position with correct data
    @Test
    fun `getLastReadPosition returns saved position with correct data`() = runTest {
        val entity = createLastReadPositionEntity()
        coEvery { dao.getLastReadPosition("HAFS_1441") } returns entity

        val result = repository.getLastReadPosition(MushafType.HAFS_1441)

        assertNotNull(result)
        assertEquals(MushafType.HAFS_1441, result!!.mushafType)
        assertEquals(2, result.chapterNumber)
        assertEquals(255, result.verseNumber)
        assertEquals(42, result.pageNumber)
        assertEquals(0.5f, result.scrollPosition, 0.001f)
    }

    // TC-7.15: getLastReadPositionFlow — verify Flow emits updates when position changes
    @Test
    fun `getLastReadPositionFlow emits updates`() = runTest {
        val entity = createLastReadPositionEntity()
        val flow = MutableStateFlow<LastReadPositionEntity?>(entity)
        coEvery { dao.getLastReadPositionFlow("HAFS_1441") } returns flow

        val result = repository.getLastReadPositionFlow(MushafType.HAFS_1441).first()

        assertNotNull(result)
        assertEquals(2, result!!.chapterNumber)
    }

    // TC-7.16: LastReadPosition.isRecent() returns true for just-saved position
    @Test
    fun `isRecent returns true for just-saved position`() = runTest {
        val entity = createLastReadPositionEntity()
        coEvery { dao.getLastReadPosition("HAFS_1441") } returns entity

        val result = repository.getLastReadPosition(MushafType.HAFS_1441)

        assertNotNull(result)
        assertTrue(result!!.isRecent())
    }

    // TC-7.17: Positions for HAFS_1441 and HAFS_1405 are stored independently
    @Test
    fun `positions for different mushaf types are stored independently`() = runTest {
        val hafs1441 = createLastReadPositionEntity(mushafType = "HAFS_1441", chapterNumber = 2, pageNumber = 42)
        val hafs1405 = createLastReadPositionEntity(mushafType = "HAFS_1405", chapterNumber = 5, pageNumber = 100)

        coEvery { dao.getLastReadPosition("HAFS_1441") } returns hafs1441
        coEvery { dao.getLastReadPosition("HAFS_1405") } returns hafs1405

        val result1441 = repository.getLastReadPosition(MushafType.HAFS_1441)
        val result1405 = repository.getLastReadPosition(MushafType.HAFS_1405)

        assertNotNull(result1441)
        assertNotNull(result1405)
        assertEquals(2, result1441!!.chapterNumber)
        assertEquals(42, result1441.pageNumber)
        assertEquals(5, result1405!!.chapterNumber)
        assertEquals(100, result1405.pageNumber)
        assertNotEquals(result1441.chapterNumber, result1405.chapterNumber)
    }

    // TC-7.18: getLastReadPosition(HAFS_1405) returns correct position
    @Test
    fun `getLastReadPosition HAFS_1405 returns correct position`() = runTest {
        val entity = createLastReadPositionEntity(mushafType = "HAFS_1405", chapterNumber = 5, pageNumber = 100)
        coEvery { dao.getLastReadPosition("HAFS_1405") } returns entity

        val result = repository.getLastReadPosition(MushafType.HAFS_1405)

        assertNotNull(result)
        assertEquals(MushafType.HAFS_1405, result!!.mushafType)
        assertEquals(5, result.chapterNumber)
        assertEquals(100, result.pageNumber)
    }
}
