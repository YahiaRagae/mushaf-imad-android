package com.mushafimad.api

class ReadingStatisticsApi {

    fun startReadingSession(): ReadingSession {
        // Simulate the start of a reading session
        return ReadingSession(sessionId = "12345")
    }

    fun getReadingStatistics(session: ReadingSession): ReadingStatistics {
        // Simulate fetching reading statistics
        return ReadingStatistics(pagesRead = 5, timeSpent = 10)
    }
}

data class ReadingSession(val sessionId: String)

data class ReadingStatistics(val pagesRead: Int, val timeSpent: Int)
