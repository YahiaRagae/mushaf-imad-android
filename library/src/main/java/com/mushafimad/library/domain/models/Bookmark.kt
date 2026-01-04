package com.mushafimad.library.domain.models

/**
 * User bookmark for a specific verse or page
 * Public API - exposed to library consumers
 */
data class Bookmark(
    val id: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val pageNumber: Int,
    val createdAt: Long,
    val note: String = "",
    val tags: List<String> = emptyList()
) {
    /**
     * Verse reference in format "chapter:verse"
     */
    val verseReference: String
        get() = "$chapterNumber:$verseNumber"

    /**
     * Check if bookmark has a note
     */
    val hasNote: Boolean
        get() = note.isNotBlank()

    /**
     * Check if bookmark has tags
     */
    val hasTags: Boolean
        get() = tags.isNotEmpty()
}
