package com.mushafimad.library.data.local.entities

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

/**
 * Realm entity for user bookmarks
 * Internal data model - not exposed in public API
 */
internal class BookmarkEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var chapterNumber: Int = 0
    var verseNumber: Int = 0
    var pageNumber: Int = 0
    var createdAt: Long = 0  // Unix timestamp in milliseconds
    var note: String = ""    // Optional note for the bookmark
    var tags: String = ""    // Comma-separated tags
}
