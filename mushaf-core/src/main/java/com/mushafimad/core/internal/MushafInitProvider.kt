package com.mushafimad.core.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.core.MushafLibrary

/**
 * ContentProvider that automatically initializes the Mushaf library when the app starts.
 *
 * This provider runs before Application.onCreate(), ensuring the library is ready
 * for use immediately. This pattern is used by Firebase, WorkManager, and other
 * modern Android libraries to provide zero-configuration initialization.
 *
 * No consumer code is required - the library initializes automatically when the
 * ContentProvider is created by the Android system.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafInitProvider : ContentProvider() {

    /**
     * Called by the Android system when the ContentProvider is created.
     * This happens before Application.onCreate().
     *
     * @return true if initialization successful, false otherwise
     */
    override fun onCreate(): Boolean {
        val context = context ?: return false

        // Initialize the library with application context
        // This calls ServiceRegistry.initialize() internally
        MushafLibrary.initializeInternal(context.applicationContext)

        return true
    }

    // No-op implementations - this ContentProvider doesn't handle data queries
    // It exists solely for automatic initialization

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
