package com.mushafimad.ui.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.core.MushafLibrary
import com.mushafimad.ui.di.uiModule

/**
 * ContentProvider that automatically loads the UI module into the library's
 * isolated Koin context. Runs after mushaf-core's provider.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafUiInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        // Defensive: initialize the core library first if provider ordering
        // ever changes (initialize is idempotent).
        if (!MushafLibrary.isInitialized()) {
            context?.applicationContext?.let { MushafLibrary.initialize(it) }
        }
        MushafLibrary.getKoin().loadModules(listOf(uiModule))
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
