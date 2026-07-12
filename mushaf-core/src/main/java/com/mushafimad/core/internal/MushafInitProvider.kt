package com.mushafimad.core.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.core.MushafLibrary

/**
 * ContentProvider that automatically initializes the Mushaf library.
 * Runs before Application.onCreate() to ensure zero-configuration setup.
 *
 * The library uses its own isolated Koin context (see [MushafKoin]), so a
 * host app that starts Koin itself is unaffected.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false
        MushafLibrary.initializeInternal(context.applicationContext)
        return true
    }

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
