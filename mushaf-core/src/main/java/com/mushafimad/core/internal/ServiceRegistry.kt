package com.mushafimad.core.internal

import android.content.Context
import android.content.SharedPreferences
import com.mushafimad.core.data.audio.AyahTimingService
import com.mushafimad.core.data.audio.MediaSessionManager
import com.mushafimad.core.data.audio.ReciterService
import com.mushafimad.core.data.cache.ChaptersDataCache
import com.mushafimad.core.data.cache.QuranDataCacheService
import com.mushafimad.core.data.repository.RealmService
import com.mushafimad.core.data.repository.RealmServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Internal singleton registry for managing library services.
 *
 * This replaces Hilt dependency injection with manual singleton management.
 * All services are created lazily when first accessed and use thread-safe
 * double-checked locking pattern.
 *
 * @internal This class is not part of the public API.
 */
internal object ServiceRegistry {
    private val lock = Any()

    @Volatile private var _context: Context? = null
    @Volatile private var _realmService: RealmService? = null
    @Volatile private var _reciterService: ReciterService? = null
    @Volatile private var _ayahTimingService: AyahTimingService? = null
    @Volatile private var _mediaSessionManager: MediaSessionManager? = null
    @Volatile private var _sharedPreferences: SharedPreferences? = null
    @Volatile private var _chaptersCache: ChaptersDataCache? = null
    @Volatile private var _quranCacheService: QuranDataCacheService? = null

    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Initialize the service registry with application context.
     * This is called automatically by MushafInitProvider.
     *
     * @param context Application context (will be stored as applicationContext)
     */
    fun initialize(context: Context) {
        synchronized(lock) {
            if (_context != null) return@synchronized
            _context = context.applicationContext

            // Start critical service initialization asynchronously
            // This ensures Realm is ready by the time UI needs it
            initScope.launch {
                // Realm must init first (long-running: 5-10s file copy)
                getRealmService()

                // Then dependent services can load
                getReciterService()
            }
        }
    }

    /**
     * Get application context.
     * @throws IllegalStateException if registry not initialized
     */
    fun getContext(): Context = _context
        ?: throw IllegalStateException("ServiceRegistry not initialized. This should never happen if MushafInitProvider is working correctly.")

    /**
     * Get RealmService singleton.
     * Thread-safe lazy initialization.
     */
    fun getRealmService(): RealmService = _realmService ?: synchronized(lock) {
        _realmService ?: RealmServiceImpl(getContext()).also {
            _realmService = it
        }
    }

    /**
     * Get SharedPreferences singleton.
     * Thread-safe lazy initialization.
     */
    fun getSharedPreferences(): SharedPreferences = _sharedPreferences ?: synchronized(lock) {
        _sharedPreferences ?: getContext()
            .getSharedPreferences("mushaf_preferences", Context.MODE_PRIVATE)
            .also { _sharedPreferences = it }
    }

    /**
     * Get AyahTimingService singleton.
     * Thread-safe lazy initialization.
     */
    fun getAyahTimingService(): AyahTimingService = _ayahTimingService ?: synchronized(lock) {
        _ayahTimingService ?: AyahTimingService(getContext()).also {
            _ayahTimingService = it
        }
    }

    /**
     * Get ReciterService singleton.
     * Thread-safe lazy initialization with dependencies.
     */
    fun getReciterService(): ReciterService = _reciterService ?: synchronized(lock) {
        _reciterService ?: ReciterService(
            getContext(),
            getAyahTimingService(),
            getSharedPreferences()
        ).also { _reciterService = it }
    }

    /**
     * Get MediaSessionManager singleton.
     * Thread-safe lazy initialization and automatic initialize() call.
     */
    fun getMediaSessionManager(): MediaSessionManager = _mediaSessionManager ?: synchronized(lock) {
        _mediaSessionManager ?: MediaSessionManager(getContext()).also {
            it.initialize()
            _mediaSessionManager = it
        }
    }

    /**
     * Get ChaptersDataCache singleton.
     * Thread-safe lazy initialization.
     */
    fun getChaptersCache(): ChaptersDataCache = _chaptersCache ?: synchronized(lock) {
        _chaptersCache ?: ChaptersDataCache(getRealmService()).also {
            _chaptersCache = it
        }
    }

    /**
     * Get QuranDataCacheService singleton.
     * Thread-safe lazy initialization.
     */
    fun getQuranCacheService(): QuranDataCacheService = _quranCacheService ?: synchronized(lock) {
        _quranCacheService ?: QuranDataCacheService(getRealmService()).also {
            _quranCacheService = it
        }
    }
}
