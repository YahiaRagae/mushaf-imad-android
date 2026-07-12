package com.mushafimad.core.internal

import android.content.Context
import com.mushafimad.core.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

/**
 * Isolated Koin context for the Mushaf library.
 *
 * The library must never call startKoin: that claims the global Koin context
 * and crashes (or silently breaks) any host app that uses Koin itself. All
 * library dependencies are resolved from this private KoinApplication instead.
 *
 * @internal This object is not part of the public API.
 */
internal object MushafKoin {

    @Volatile
    private var application: KoinApplication? = null
    private val lock = Any()

    val koin: Koin
        get() = application?.koin
            ?: throw IllegalStateException(
                "MushafKoin not started. This should never happen if MushafInitProvider is working correctly."
            )

    fun start(context: Context) {
        if (application != null) return
        synchronized(lock) {
            if (application != null) return
            application = koinApplication {
                androidContext(context.applicationContext)
                modules(coreModule)
            }
        }
    }
}
