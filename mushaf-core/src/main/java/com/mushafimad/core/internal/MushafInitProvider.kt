package com.mushafimad.core.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * ContentProvider that automatically initializes the Mushaf library.
 * Runs before Application.onCreate() to ensure zero-configuration setup.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false

        MushafLibrary.initializeInternal(context.applicationContext)

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(context.applicationContext)
            modules(coreModule)
        }

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
