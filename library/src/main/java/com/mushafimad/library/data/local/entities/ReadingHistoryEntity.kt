package com.mushafimad.library.data.local.entities

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

/**
 * Realm entity for tracking reading history
 * Records each reading session for analytics
 * Internal data model - not exposed in public API
 */
internal class ReadingHistoryEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var chapterNumber: Int = 0
    var verseNumber: Int = 0
    var pageNumber: Int = 0
    var timestamp: Long = 0           // Unix timestamp in milliseconds
    var durationSeconds: Int = 0      // How long the page/verse was read
    var mushafType: String = ""       // HAFS_1441, WARSH, etc.
}
