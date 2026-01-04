package com.mushafimad.library.utils

import com.mushafimad.library.domain.models.Verse

/**
 * Extension functions for Verse model
 * Public API - exposed to library consumers
 */

/**
 * Get verse reference in standard format (e.g., "2:255")
 */
val Verse.reference: String
    get() = "$chapterNumber:$number"

/**
 * Get verse reference in Arabic format
 */
fun Verse.getReferenceArabic(): String {
    return "سورة $chapterNumber آية $number"
}

/**
 * Get verse reference with chapter name
 */
fun Verse.getReferenceWithChapterName(chapterName: String, languageCode: String = "en"): String {
    return if (languageCode == "ar") {
        "سورة $chapterName - آية $number"
    } else {
        "$chapterName - Verse $number"
    }
}

/**
 * Format verse number with leading zeros
 */
fun Verse.getFormattedNumber(): String {
    return String.format("%03d", number)
}

/**
 * Check if this is the first verse of a chapter
 */
val Verse.isFirstVerse: Boolean
    get() = number == 1

/**
 * Get text preview (first 50 characters)
 */
fun Verse.getTextPreview(maxLength: Int = 50): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}

/**
 * Get searchable text (without diacritics)
 */
fun Verse.getSearchableContent(): String {
    return textWithoutTashkil.lowercase()
}

/**
 * Check if verse contains search query
 */
fun Verse.containsQuery(query: String, ignoreCase: Boolean = true): Boolean {
    return if (ignoreCase) {
        textWithoutTashkil.contains(query, ignoreCase = true) ||
        text.contains(query, ignoreCase = true) ||
        searchableText.contains(query, ignoreCase = true)
    } else {
        searchableText.contains(query)
    }
}

/**
 * Get verse position info (Juz, Hizb, Page)
 */
fun Verse.getPositionInfo(languageCode: String = "en"): String {
    return if (languageCode == "ar") {
        "جزء $partNumber - حزب $hizbNumber - صفحة $pageNumber"
    } else {
        "Juz $partNumber - Hizb $hizbNumber - Page $pageNumber"
    }
}
