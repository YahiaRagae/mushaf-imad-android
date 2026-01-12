package com.mushafimad.library.domain.models

/**
 * Domain model for Quran Verse (Ayah)
 * Public API - exposed to library consumers
 */
data class Verse(
    val verseID: Int,
    val humanReadableID: String,  // e.g. "2_255"
    val number: Int,
    val text: String,
    val textWithoutTashkil: String,
    val uthmanicHafsText: String,
    val hafsSmartText: String,
    val searchableText: String,
    val chapterNumber: Int = 0,
    val pageNumber: Int = 0,
    val partNumber: Int = 0,
    val hizbNumber: Int = 0,
    val marker1441: VerseMarker? = null,
    val marker1405: VerseMarker? = null,
    val highlights1441: List<VerseHighlight> = emptyList(),
    val highlights1405: List<VerseHighlight> = emptyList()
)
