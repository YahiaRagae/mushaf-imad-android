package com.mushafimad.library.domain.repository

import com.mushafimad.library.domain.models.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing user bookmarks
 * Public API - exposed to library consumers
 */
interface BookmarkRepository {

    /**
     * Observe all bookmarks
     */
    fun getAllBookmarksFlow(): Flow<List<Bookmark>>

    /**
     * Get all bookmarks
     */
    suspend fun getAllBookmarks(): List<Bookmark>

    /**
     * Get bookmark by ID
     * @param id The bookmark ID
     * @return Bookmark if found, null otherwise
     */
    suspend fun getBookmarkById(id: String): Bookmark?

    /**
     * Get bookmarks for a specific chapter
     * @param chapterNumber The chapter number (1-114)
     */
    suspend fun getBookmarksForChapter(chapterNumber: Int): List<Bookmark>

    /**
     * Get bookmark for a specific verse
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     * @return Bookmark if found, null otherwise
     */
    suspend fun getBookmarkForVerse(chapterNumber: Int, verseNumber: Int): Bookmark?

    /**
     * Add or update a bookmark
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     * @param pageNumber The page number
     * @param note Optional note
     * @param tags Optional tags
     * @return The created/updated bookmark
     */
    suspend fun addBookmark(
        chapterNumber: Int,
        verseNumber: Int,
        pageNumber: Int,
        note: String = "",
        tags: List<String> = emptyList()
    ): Bookmark

    /**
     * Update bookmark note
     * @param id The bookmark ID
     * @param note The new note
     */
    suspend fun updateBookmarkNote(id: String, note: String)

    /**
     * Update bookmark tags
     * @param id The bookmark ID
     * @param tags The new tags
     */
    suspend fun updateBookmarkTags(id: String, tags: List<String>)

    /**
     * Delete a bookmark
     * @param id The bookmark ID
     */
    suspend fun deleteBookmark(id: String)

    /**
     * Delete bookmark for a specific verse
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     */
    suspend fun deleteBookmarkForVerse(chapterNumber: Int, verseNumber: Int)

    /**
     * Delete all bookmarks
     */
    suspend fun deleteAllBookmarks()

    /**
     * Check if a verse is bookmarked
     * @param chapterNumber The chapter number (1-114)
     * @param verseNumber The verse number
     */
    suspend fun isVerseBookmarked(chapterNumber: Int, verseNumber: Int): Boolean

    /**
     * Search bookmarks by note content or tags
     * @param query Search query
     */
    suspend fun searchBookmarks(query: String): List<Bookmark>
}
