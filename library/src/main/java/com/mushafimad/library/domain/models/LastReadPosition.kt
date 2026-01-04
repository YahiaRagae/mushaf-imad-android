package com.mushafimad.library.domain.models

/**
 * Last read position for resuming reading
 * Public API - exposed to library consumers
 */
data class LastReadPosition(
    val mushafType: MushafType,
    val chapterNumber: Int,
    val verseNumber: Int,
    val pageNumber: Int,
    val lastReadAt: Long,
    val scrollPosition: Float = 0f
) {
    /**
     * Verse reference in format "chapter:verse"
     */
    val verseReference: String
        get() = "$chapterNumber:$verseNumber"

    /**
     * Check if this position is recent (within last 7 days)
     */
    fun isRecent(): Boolean {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return lastReadAt > sevenDaysAgo
    }
}
