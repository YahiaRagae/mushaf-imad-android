package com.mushafimad.core

import android.content.Context
import com.mushafimad.core.domain.repository.*
import com.mushafimad.core.logging.DefaultMushafLogger
import com.mushafimad.core.logging.MushafAnalytics
import com.mushafimad.core.logging.MushafLogger
import com.mushafimad.core.logging.NoOpMushafAnalytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Main entry point for MushafImad library
 *
 * The library initializes automatically via ContentProvider when your app starts.
 * You don't need to call `initialize()` manually unless you want to provide custom
 * logger or analytics implementations.
 *
 * Example usage:
 * ```kotlin
 * // Automatic initialization - no code needed!
 * class MyApp : Application() {
 *     // Library is already initialized automatically
 * }
 *
 * // Optional: custom logger/analytics
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         MushafLibrary.setLogger(MyCustomLogger())
 *         MushafLibrary.setAnalytics(MyCustomAnalytics())
 *     }
 * }
 *
 * // Access repositories
 * val quranRepo = MushafLibrary.getQuranRepository()
 * val chapters = quranRepo.getAllChapters()
 * ```
 */
object MushafLibrary : KoinComponent {

    private var isInitialized = false
    private var applicationContext: Context? = null

    var logger: MushafLogger = DefaultMushafLogger()
        private set

    internal var analytics: MushafAnalytics = NoOpMushafAnalytics()
        private set

    /**
     * Internal initialization called by MushafInitProvider (ContentProvider).
     * This runs automatically before Application.onCreate().
     *
     * @param context Application context
     * @internal This is not part of the public API.
     */
    internal fun initializeInternal(context: Context) {
        if (isInitialized) return

        applicationContext = context.applicationContext

        isInitialized = true
        logger.info("MushafLibrary auto-initialized via ContentProvider")
    }

    /**
     * Initialize the Mushaf library manually.
     *
     * **Note**: Manual initialization is optional. The library initializes automatically
     * via ContentProvider. Only call this if you need to provide custom logger or analytics
     * BEFORE the ContentProvider runs (rare case).
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
        this.logger = logger
        this.analytics = analytics

        // Call internal initialization (idempotent)
        initializeInternal(context)

        this.logger.info("MushafLibrary manually initialized with custom logger/analytics")
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
            ?: throw IllegalStateException("MushafLibrary not initialized. This should never happen if ContentProvider is working correctly.")
    }

    // ========== Repository Accessors ==========

    /**
     * Get QuranRepository for accessing Quran data (chapters, pages, verses, etc.)
     * @return QuranRepository singleton instance
     */
    @JvmStatic
    fun getQuranRepository(): QuranRepository = get()

    /**
     * Get ChapterRepository for accessing chapter (surah) data
     * @return ChapterRepository singleton instance
     */
    @JvmStatic
    fun getChapterRepository(): ChapterRepository = get()

    /**
     * Get PageRepository for accessing page data
     * @return PageRepository singleton instance
     */
    @JvmStatic
    fun getPageRepository(): PageRepository = get()

    /**
     * Get VerseRepository for accessing verse (ayah) data
     * @return VerseRepository singleton instance
     */
    @JvmStatic
    fun getVerseRepository(): VerseRepository = get()

    /**
     * Get BookmarkRepository for managing bookmarks
     * @return BookmarkRepository singleton instance
     */
    @JvmStatic
    fun getBookmarkRepository(): BookmarkRepository = get()

    /**
     * Get ReadingHistoryRepository for managing reading history
     * @return ReadingHistoryRepository singleton instance
     */
    @JvmStatic
    fun getReadingHistoryRepository(): ReadingHistoryRepository = get()

    /**
     * Get SearchHistoryRepository for managing search history
     * @return SearchHistoryRepository singleton instance
     */
    @JvmStatic
    fun getSearchHistoryRepository(): SearchHistoryRepository = get()

    /**
     * Get AudioRepository for audio playback and reciter management
     * @return AudioRepository singleton instance
     */
    @JvmStatic
    fun getAudioRepository(): AudioRepository = get()

    /**
     * Get PreferencesRepository for managing app preferences
     * @return PreferencesRepository singleton instance
     */
    @JvmStatic
    fun getPreferencesRepository(): PreferencesRepository = get()

    /**
     * Get DataExportRepository for exporting user data
     * @return DataExportRepository singleton instance
     */
    @JvmStatic
    fun getDataExportRepository(): DataExportRepository = get()
}
