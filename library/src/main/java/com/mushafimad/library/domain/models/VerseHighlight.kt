package com.mushafimad.library.domain.models

/**
 * Domain model for Verse highlight rectangle
 * Contains normalized coordinates (0-1) for selection highlighting
 */
data class VerseHighlight(
    val line: Int,
    val left: Float,   // Normalized 0-1
    val right: Float   // Normalized 0-1
)
