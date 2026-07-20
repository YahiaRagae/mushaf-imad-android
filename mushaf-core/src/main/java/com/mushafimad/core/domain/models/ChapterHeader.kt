package com.mushafimad.core.domain.models

/**
 * Domain model for a surah (chapter) header shown on a page
 * Contains the chapter it belongs to and normalized coordinates (0-1) for where its
 * decorative name bar sits on the page, so the reader can draw the ornament behind the
 * surah name that is baked into the line image.
 * Public API - exposed to library consumers
 */
data class ChapterHeader(
    val chapterNumber: Int,
    val line: Int,
    val centerX: Float,  // Normalized 0-1
    val centerY: Float   // Normalized 0-1
)
