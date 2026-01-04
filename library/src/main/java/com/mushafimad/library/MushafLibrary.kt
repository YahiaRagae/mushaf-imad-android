package com.mushafimad.library

import android.content.Context
import com.mushafimad.library.logging.MushafAnalytics
import com.mushafimad.library.logging.MushafLogger
import com.mushafimad.library.logging.DefaultMushafLogger
import com.mushafimad.library.logging.NoOpMushafAnalytics

/**
 * Main entry point for MushafImad library
 *
 * Initialize this in your Application class:
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         MushafLibrary.initialize(
 *             context = this,
 *             logger = MyCustomLogger(),
 *             analytics = MyCustomAnalytics()
 *         )
 *     }
 * }
 * ```
 */
object MushafLibrary {

    private var isInitialized = false
    private var applicationContext: Context? = null

    internal var logger: MushafLogger = DefaultMushafLogger()
        private set

    internal var analytics: MushafAnalytics = NoOpMushafAnalytics()
        private set

    /**
     * Initialize the Mushaf library
     *
     * @param context Application context
     * @param logger Custom logger implementation (optional)
     * @param analytics Custom analytics implementation (optional)
     */
    @JvmStatic
    fun initialize(
        context: Context,
        logger: MushafLogger = DefaultMushafLogger(),
        analytics: MushafAnalytics = NoOpMushafAnalytics()
    ) {
        if (isInitialized) {
            this.logger.warning("MushafLibrary already initialized")
            return
        }

        applicationContext = context.applicationContext
        this.logger = logger
        this.analytics = analytics

        // Initialize other components here
        // e.g., FontManager, RealmService, etc.

        isInitialized = true
        this.logger.info("MushafLibrary initialized successfully")
    }

    /**
     * Update logger after initialization
     */
    @JvmStatic
    fun setLogger(logger: MushafLogger) {
        this.logger = logger
    }

    /**
     * Update analytics after initialization
     */
    @JvmStatic
    fun setAnalytics(analytics: MushafAnalytics) {
        this.analytics = analytics
    }

    /**
     * Check if library is initialized
     */
    @JvmStatic
    fun isInitialized(): Boolean = isInitialized

    /**
     * Get application context
     */
    internal fun getContext(): Context {
        return applicationContext
            ?: throw IllegalStateException("MushafLibrary not initialized. Call MushafLibrary.initialize() first.")
    }
}
