package com.mushafimad.ui.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.ui.di.uiModule
import org.koin.core.context.loadKoinModules

/**
 * ContentProvider that automatically loads the UI module into Koin.
 * Runs after mushaf-core's provider to ensure Koin is initialized.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafUiInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        loadKoinModules(uiModule)
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
