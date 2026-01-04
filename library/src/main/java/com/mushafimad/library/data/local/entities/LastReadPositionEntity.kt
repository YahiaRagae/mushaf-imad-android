package com.mushafimad.library.data.local.entities

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm entity for storing the last read position
 * Only one instance should exist per mushaf type
 * Internal data model - not exposed in public API
 */
internal class LastReadPositionEntity : RealmObject {
    @PrimaryKey
    var mushafType: String = ""       // HAFS_1441, WARSH, etc. (used as primary key)
    var chapterNumber: Int = 0
    var verseNumber: Int = 0
    var pageNumber: Int = 0
    var lastReadAt: Long = 0          // Unix timestamp in milliseconds
    var scrollPosition: Float = 0f    // Optional: scroll position on the page (0-1)
}
