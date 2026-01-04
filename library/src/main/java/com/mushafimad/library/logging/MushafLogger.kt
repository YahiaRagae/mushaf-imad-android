package com.mushafimad.library.logging

import android.util.Log

/**
 * Interface for library logging
 */
interface MushafLogger {

    enum class LogLevel {
        TRACE, DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL
    }

    enum class LogCategory {
        APP, UI, AUDIO, NETWORK, DATABASE, DOWNLOAD, TIMING, REALM, IMAGES, MUSHAF
    }

    fun log(
        message: String,
        level: LogLevel = LogLevel.INFO,
        category: LogCategory = LogCategory.APP,
        throwable: Throwable? = null,
        metadata: Map<String, Any>? = null
    )

    // Convenience methods
    fun trace(message: String, category: LogCategory = LogCategory.APP) =
        log(message, LogLevel.TRACE, category)

    fun debug(message: String, category: LogCategory = LogCategory.APP) =
        log(message, LogLevel.DEBUG, category)

    fun info(message: String, category: LogCategory = LogCategory.APP) =
        log(message, LogLevel.INFO, category)

    fun warning(message: String, category: LogCategory = LogCategory.APP) =
        log(message, LogLevel.WARNING, category)

    fun error(message: String, throwable: Throwable? = null, category: LogCategory = LogCategory.APP) =
        log(message, LogLevel.ERROR, category, throwable)
}

/**
 * Default logger implementation using Android Logcat
 */
class DefaultMushafLogger : MushafLogger {
    override fun log(
        message: String,
        level: MushafLogger.LogLevel,
        category: MushafLogger.LogCategory,
        throwable: Throwable?,
        metadata: Map<String, Any>?
    ) {
        val tag = "Mushaf[${category.name}]"
        val fullMessage = metadata?.let { "$message | $it" } ?: message

        when (level) {
            MushafLogger.LogLevel.TRACE,
            MushafLogger.LogLevel.DEBUG -> Log.d(tag, fullMessage, throwable)
            MushafLogger.LogLevel.INFO,
            MushafLogger.LogLevel.NOTICE -> Log.i(tag, fullMessage, throwable)
            MushafLogger.LogLevel.WARNING -> Log.w(tag, fullMessage, throwable)
            MushafLogger.LogLevel.ERROR,
            MushafLogger.LogLevel.CRITICAL -> Log.e(tag, fullMessage, throwable)
        }
    }
}
