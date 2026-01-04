package com.mushafimad.library.domain.models

/**
 * Reading history entry for analytics
 * Public API - exposed to library consumers
 */
data class ReadingHistory(
    val id: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val pageNumber: Int,
    val timestamp: Long,
    val durationSeconds: Int,
    val mushafType: MushafType
) {
    /**
     * Verse reference in format "chapter:verse"
     */
    val verseReference: String
        get() = "$chapterNumber:$verseNumber"

    /**
     * Duration in minutes
     */
    val durationMinutes: Int
        get() = durationSeconds / 60
}

/**
 * Reading statistics aggregated from history
 * Public API - exposed to library consumers
 */
data class ReadingStats(
    val totalReadingTimeSeconds: Long,
    val totalPagesRead: Int,
    val totalChaptersRead: Int,
    val totalVersesRead: Int,
    val mostReadChapter: Int?,
    val currentStreak: Int,          // Days
    val longestStreak: Int,           // Days
    val averageDailyMinutes: Int
) {
    /**
     * Total reading time in minutes
     */
    val totalReadingTimeMinutes: Long
        get() = totalReadingTimeSeconds / 60

    /**
     * Total reading time in hours
     */
    val totalReadingTimeHours: Long
        get() = totalReadingTimeMinutes / 60
}
