package com.mushafimad.core.data.local.dao

import com.mushafimad.core.data.local.entities.BookmarkEntity
import com.mushafimad.core.data.repository.RealmService
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

/**
 * Realm implementation of BookmarkDao
 * All Realm-specific operations are encapsulated here
 *
 * @internal This class is not part of the public API
 */
internal class RealmBookmarkDao(
    private val realmService: RealmService
) : BookmarkDao {

    private val realm: Realm
        get() = realmService.getUserRealm()

    override fun getAllFlow(): Flow<List<BookmarkEntity>> {
        return realm.query<BookmarkEntity>()
            .sort("createdAt", io.realm.kotlin.query.Sort.DESCENDING)
            .asFlow()
            .map { results -> results.list }
    }

    override suspend fun getAll(): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>()
            .sort("createdAt", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
    }

    override suspend fun getById(id: ObjectId): BookmarkEntity? = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("id == $0", id)
            .first()
            .find()
    }

    override suspend fun getForChapter(chapterNumber: Int): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("chapterNumber == $0", chapterNumber)
            .sort("verseNumber")
            .find()
    }

    override suspend fun getForVerse(chapterNumber: Int, verseNumber: Int): BookmarkEntity? = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
            .first()
            .find()
    }

    override suspend fun upsert(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        note: String,
        tags: String,
        createdAt: Long
    ): ObjectId = withContext(Dispatchers.IO) {
        // Check if bookmark already exists
        val existing = getForVerse(chapterNumber, verseNumber)

        if (existing != null) {
            // Update existing bookmark
            realm.write {
                val entity = query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
                    .first()
                    .find()

                entity?.apply {
                    this.note = note
                    this.tags = tags
                    this.createdAt = createdAt
                }
            }
            existing.id
        } else {
            // Create new bookmark
            realm.write {
                val entity = BookmarkEntity().apply {
                    this.chapterNumber = chapterNumber
                    this.verseNumber = verseNumber
                    this.pageNumber = pageNumber
                    this.createdAt = createdAt
                    this.note = note
                    this.tags = tags
                }
                copyToRealm(entity).id
            }
        }
    }

    override suspend fun updateNote(id: ObjectId, note: String) = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", id)
                .first()
                .find()

            entity?.note = note
        }
    }

    override suspend fun updateTags(id: ObjectId, tags: String) = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", id)
                .first()
                .find()

            entity?.tags = tags
        }
    }

    override suspend fun delete(id: ObjectId): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", id)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteForVerse(chapterNumber: Int, verseNumber: Int): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        realm.write {
            val bookmarks = query<BookmarkEntity>().find()
            delete(bookmarks)
        }
    }

    override suspend fun isVerseBookmarked(chapterNumber: Int, verseNumber: Int): Boolean = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
            .count()
            .find() > 0
    }

    override suspend fun search(query: String): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        val lowerQuery = query.lowercase()
        realm.query<BookmarkEntity>()
            .find()
            .filter { bookmark ->
                bookmark.note.lowercase().contains(lowerQuery) ||
                        bookmark.tags.lowercase().contains(lowerQuery)
            }
    }
}
