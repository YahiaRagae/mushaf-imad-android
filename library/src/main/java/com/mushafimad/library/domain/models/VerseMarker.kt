package com.mushafimad.library.domain.models

/**
 * Domain model for Verse number marker position
 * Contains normalized coordinates (0-1) for verse number placement
 */
data class VerseMarker(
    val numberCodePoint: String,
    val line: Int,
    val centerX: Float,  // Normalized 0-1
    val centerY: Float   // Normalized 0-1
)
