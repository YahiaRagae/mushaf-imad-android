package com.mushafimad.core.internal

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.repository.RealmService
import com.mushafimad.core.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * ContentProvider that automatically initializes the Mushaf library.
 * Runs before Application.onCreate() to ensure zero-configuration setup.
 *
 * Test-aware: If Koin is already started (e.g., by tests), this provider
 * will skip initialization to allow test modules to be used instead.
 *
 * @internal This class is not part of the public API.
 */
internal class MushafInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false

        MushafLibrary.initializeInternal(context.applicationContext)

        // Check if Koin is already started (e.g., by tests)
        // If so, skip initialization to allow test modules
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.DEBUG)
                androidContext(context.applicationContext)
                modules(coreModule)
            }
            // Instantiating the service kicks off the asynchronous database
            // open so it is usually ready by the time the UI first needs it.
            GlobalContext.get().get<RealmService>()
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
