package com.mushafimad.library

import android.content.Context
import com.mushafimad.library.domain.repository.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Helper class for initializing Mushaf library components
 *
 * Usage:
 * ```kotlin
 * // In your Application class or composable
 * val initializer = MushafInitializer.getInstance(context)
 * initializer.initializeAsync(quranRepository)
 * ```
 */
class MushafInitializer private constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isDbInitialized = false

    /**
     * Initialize the Quran database asynchronously
     *
     * @param quranRepository The QuranRepository instance (injected via Hilt)
     * @param onComplete Callback when initialization completes
     * @param onError Callback when initialization fails
     */
    fun initializeAsync(
        quranRepository: QuranRepository,
        onComplete: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (isDbInitialized) {
            MushafLibrary.logger.warning("Database already initialized")
            onComplete?.invoke()
            return
        }

        scope.launch {
            try {
                MushafLibrary.logger.info("Initializing Quran database...")
                quranRepository.initialize()
                isDbInitialized = true
                MushafLibrary.logger.info("Quran database initialized successfully")
                onComplete?.invoke()
            } catch (e: Exception) {
                MushafLibrary.logger.error("Failed to initialize database", e)
                onError?.invoke(e)
            }
        }
    }

    /**
     * Check if database is initialized
     */
    fun isDatabaseInitialized(): Boolean = isDbInitialized

    companion object {
        @Volatile
        private var instance: MushafInitializer? = null

        /**
         * Get singleton instance of MushafInitializer
         */
        fun getInstance(context: Context): MushafInitializer {
            return instance ?: synchronized(this) {
                instance ?: MushafInitializer(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
