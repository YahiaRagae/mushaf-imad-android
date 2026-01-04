package com.mushafimad.library.utils

import com.mushafimad.library.domain.models.Chapter

/**
 * Extension functions for Chapter model
 * Public API - exposed to library consumers
 */

/**
 * Get chapter type display name
 */
fun Chapter.getTypeDisplayName(languageCode: String = "en"): String {
    return if (languageCode == "ar") {
        if (isMeccan) "مكية" else "مدنية"
    } else {
        if (isMeccan) "Meccan" else "Medinan"
    }
}

/**
 * Get chapter number with proper formatting
 */
fun Chapter.getFormattedNumber(): String {
    return String.format("%03d", number)
}

/**
 * Get chapter revelation info
 */
fun Chapter.getRevelationInfo(languageCode: String = "en"): String {
    val type = getTypeDisplayName(languageCode)
    return if (languageCode == "ar") {
        "$type - $versesCount آية"
    } else {
        "$type - $versesCount verses"
    }
}

/**
 * Check if this is a short chapter (less than 10 verses)
 */
val Chapter.isShort: Boolean
    get() = versesCount < 10

/**
 * Check if this is a medium chapter (10-50 verses)
 */
val Chapter.isMedium: Boolean
    get() = versesCount in 10..50

/**
 * Check if this is a long chapter (more than 50 verses)
 */
val Chapter.isLong: Boolean
    get() = versesCount > 50

/**
 * Get chapter display info for lists
 */
fun Chapter.getListDisplayInfo(languageCode: String = "en"): String {
    return "${getFormattedNumber()}. ${getDisplayTitle(languageCode)}"
}
