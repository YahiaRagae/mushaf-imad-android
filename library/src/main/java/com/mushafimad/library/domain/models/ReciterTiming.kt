package com.mushafimad.library.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Timing data for a reciter
 * Used for audio-verse synchronization
 * Public API - exposed to library consumers
 */
@Serializable
data class ReciterTiming(
    val id: Int,
    val name: String,
    @SerialName("name_en")
    val nameEn: String,
    val rewaya: String,
    @SerialName("folder_url")
    val folderUrl: String,
    val chapters: List<ChapterTiming>
)

@Serializable
data class ChapterTiming(
    val id: Int,
    val name: String,
    @SerialName("aya_timing")
    val ayaTiming: List<AyahTiming>
)

@Serializable
data class AyahTiming(
    val ayah: Int,
    @SerialName("start_time")
    val startTime: Int,  // in milliseconds
    @SerialName("end_time")
    val endTime: Int     // in milliseconds
)
