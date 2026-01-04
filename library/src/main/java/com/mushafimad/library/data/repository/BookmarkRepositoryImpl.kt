package com.mushafimad.library.data.repository

import com.mushafimad.library.data.local.entities.BookmarkEntity
import com.mushafimad.library.domain.models.Bookmark
import com.mushafimad.library.domain.repository.BookmarkRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BookmarkRepository using Realm
 * Internal implementation - not exposed in public API
 */
@Singleton
internal class BookmarkRepositoryImpl @Inject constructor(
    private val realmService: RealmService
) : BookmarkRepository {

    private val realm: Realm
        get() = realmService.getRealm()

    override fun getAllBookmarksFlow(): Flow<List<Bookmark>> {
        return realm.query<BookmarkEntity>()
            .sort("createdAt", io.realm.kotlin.query.Sort.DESCENDING)
            .asFlow()
            .map { results ->
                results.list.map { it.toDomain() }
            }
    }

    override suspend fun getAllBookmarks(): List<Bookmark> = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>()
            .sort("createdAt", io.realm.kotlin.query.Sort.DESCENDING)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getBookmarkById(id: String): Bookmark? = withContext(Dispatchers.IO) {
        val objectId = try {
            ObjectId(id)
        } catch (e: Exception) {
            return@withContext null
        }

        realm.query<BookmarkEntity>("id == $0", objectId)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun getBookmarksForChapter(chapterNumber: Int): List<Bookmark> = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("chapterNumber == $0", chapterNumber)
            .sort("verseNumber")
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getBookmarkForVerse(chapterNumber: Int, verseNumber: Int): Bookmark? = withContext(Dispatchers.IO) {
        realm.query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun addBookmark(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        note: String,
        tags: List<String>
    ): Bookmark = withContext(Dispatchers.IO) {
        // Check if bookmark already exists
        val existing = getBookmarkForVerse(chapterNumber, verseNumber)
        if (existing != null) {
            // Update existing bookmark
            realm.write {
                val entity = query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
                    .first()
                    .find()

                entity?.apply {
                    this.note = note
                    this.tags = tags.joinToString(",")
                    this.createdAt = System.currentTimeMillis()
                }
            }
            return@withContext getBookmarkForVerse(chapterNumber, verseNumber)!!
        }

        // Create new bookmark
        val bookmarkId = realm.write {
            val entity = BookmarkEntity().apply {
                this.chapterNumber = chapterNumber
                this.verseNumber = verseNumber
                this.pageNumber = pageNumber
                this.createdAt = System.currentTimeMillis()
                this.note = note
                this.tags = tags.joinToString(",")
            }
            copyToRealm(entity).id
        }

        getBookmarkById(bookmarkId.toHexString())!!
    }

    override suspend fun updateBookmarkNote(id: String, note: String) = withContext(Dispatchers.IO) {
        val objectId = ObjectId(id)
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", objectId)
                .first()
                .find()

            entity?.note = note
        }
    }

    override suspend fun updateBookmarkTags(id: String, tags: List<String>) = withContext(Dispatchers.IO) {
        val objectId = ObjectId(id)
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", objectId)
                .first()
                .find()

            entity?.tags = tags.joinToString(",")
        }
    }

    override suspend fun deleteBookmark(id: String): Unit = withContext(Dispatchers.IO) {
        val objectId = ObjectId(id)
        realm.write {
            val entity = query<BookmarkEntity>("id == $0", objectId)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteBookmarkForVerse(chapterNumber: Int, verseNumber: Int): Unit = withContext(Dispatchers.IO) {
        realm.write {
            val entity = query<BookmarkEntity>("chapterNumber == $0 AND verseNumber == $1", chapterNumber, verseNumber)
                .first()
                .find()

            entity?.let { delete(it) }
        }
    }

    override suspend fun deleteAllBookmarks() = withContext(Dispatchers.IO) {
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

    override suspend fun searchBookmarks(query: String): List<Bookmark> = withContext(Dispatchers.IO) {
        val lowerQuery = query.lowercase()
        realm.query<BookmarkEntity>()
            .find()
            .filter { bookmark ->
                bookmark.note.lowercase().contains(lowerQuery) ||
                bookmark.tags.lowercase().contains(lowerQuery)
            }
            .map { it.toDomain() }
    }

    private fun BookmarkEntity.toDomain(): Bookmark {
        return Bookmark(
            id = id.toHexString(),
            chapterNumber = chapterNumber,
            verseNumber = verseNumber,
            pageNumber = pageNumber,
            createdAt = createdAt,
            note = note,
            tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }
        )
    }
}
