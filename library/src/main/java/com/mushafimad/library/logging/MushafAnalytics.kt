package com.mushafimad.library.logging

/**
 * Interface for tracking user interactions
 */
interface MushafAnalytics {

    fun trackEvent(eventName: String, parameters: Map<String, Any>? = null)
    fun setUserProperty(propertyName: String, value: String)
    fun trackScreenView(screenName: String, screenClass: String? = null)
}

/**
 * No-op implementation (default)
 */
class NoOpMushafAnalytics : MushafAnalytics {
    override fun trackEvent(eventName: String, parameters: Map<String, Any>?) {}
    override fun setUserProperty(propertyName: String, value: String) {}
    override fun trackScreenView(screenName: String, screenClass: String?) {}
}

/**
 * Predefined event names
 */
object MushafEvents {
    const val PAGE_CHANGED = "mushaf_page_changed"
    const val CHAPTER_SELECTED = "mushaf_chapter_selected"
    const val VERSE_SELECTED = "mushaf_verse_selected"
    const val AUDIO_PLAY_STARTED = "mushaf_audio_play_started"
    const val AUDIO_PAUSED = "mushaf_audio_paused"
    const val RECITER_CHANGED = "mushaf_reciter_changed"
    const val THEME_CHANGED = "mushaf_theme_changed"
}

/**
 * Event parameter keys
 */
object MushafEventParams {
    const val PAGE_NUMBER = "page_number"
    const val CHAPTER_NUMBER = "chapter_number"
    const val VERSE_NUMBER = "verse_number"
    const val RECITER_ID = "reciter_id"
    const val THEME_NAME = "theme_name"
}
