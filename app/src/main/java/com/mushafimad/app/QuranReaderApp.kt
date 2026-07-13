package com.mushafimad.app

import android.app.Application
import com.mushafimad.app.ui.AppSettings
import com.mushafimad.core.MushafLibrary

/**
 * The MushafImad library auto-initializes via its own ContentProvider before
 * Application.onCreate() runs, so there is nothing to set up here.
 * We only touch it to log that it is already up.
 */
class QuranReaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
        android.util.Log.i(
            "QuranReaderApp",
            "MushafLibrary.isInitialized() = ${MushafLibrary.isInitialized()} (before any app call)"
        )
    }
}
