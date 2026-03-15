package com.mushafimad.readingstatistics

import org.junit.Assert
import org.junit.Test
import com.mushafimad.api.ReadingStatisticsApi

class ReadingStatisticsTest {

    @Test
    fun testReadingStatisticsApi() {
        val api = ReadingStatisticsApi()
        val readingSession = api.startReadingSession() // Simulate starting a reading session
        val statistics = api.getReadingStatistics(readingSession)

        Assert.assertNotNull("Reading statistics should not be null", statistics)
        Assert.assertTrue("Reading statistics should contain valid data", statistics.pagesRead > 0)
        Assert.assertTrue("Reading statistics should contain valid time spent", statistics.timeSpent > 0)
    }
}
