package com.mushafimad.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.data.audio.ReciterDataProvider
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ReciterDataProvider hardcodes a copy of data that already ships in the AAR, and the
 * copy had drifted from the original completely: every one of its 18 entries named a
 * different reciter than the timing file with the same id, and pointed at that other
 * reciter's audio folder. Reciter 1 is Ibrahim Al-Akdar; the table called him Abdul
 * Basit and served Abdul Basit's recording, while read_1.json timed Al-Akdar's - so a
 * consumer that hit the fallback would highlight verses against a voice nobody was
 * hearing.
 *
 * Correcting the table once is not a fix, because nothing stopped it drifting in the
 * first place. This test reads the timing assets the way the library reads them and
 * fails if the two ever disagree again.
 */
@RunWith(AndroidJUnit4::class)
class ReciterFallbackMatchesShippedDataTest {

    private data class ShippedReciter(
        val id: Int,
        val nameArabic: String,
        val nameEnglish: String,
        val rewaya: String,
        val folderUrl: String
    )

    private fun readShippedReciters(): List<ShippedReciter> {
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val timingFiles = assets.list("ayah_timing").orEmpty()
            .filter { it.startsWith("read_") && it.endsWith(".json") }

        return timingFiles.map { fileName ->
            val json = assets.open("ayah_timing/$fileName").use { input ->
                JSONObject(input.reader().readText())
            }
            ShippedReciter(
                id = json.getInt("id"),
                nameArabic = json.getString("name"),
                nameEnglish = json.getString("name_en"),
                rewaya = json.getString("rewaya"),
                folderUrl = json.getString("folder_url")
            )
        }.sortedBy { it.id }
    }

    @Test
    fun fallbackNamesTheSameRecitersTheTimingFilesDo() {
        val shipped = readShippedReciters()
        val fallback = ReciterDataProvider.allReciters.sortedBy { it.id }

        // Guards the guard: if the assets ever vanish from the AAR this test must fail
        // loudly rather than pass by comparing two empty lists.
        assertThat(shipped).isNotEmpty()

        assertThat(fallback.map { it.id }).isEqualTo(shipped.map { it.id })

        shipped.zip(fallback).forEach { (expected, actual) ->
            assertThat(actual.nameEnglish).isEqualTo(expected.nameEnglish)
            assertThat(actual.nameArabic).isEqualTo(expected.nameArabic)
            assertThat(actual.rewaya).isEqualTo(expected.rewaya)
            // The audio folder is the one that silently corrupts playback: get it wrong
            // and the app plays a different reciter than the timings were measured for.
            assertThat(actual.folderUrl).isEqualTo(expected.folderUrl)
        }
    }

    /**
     * The default reciter is the one a consumer gets from
     * AudioRepository.getDefaultReciter() when nothing is selected yet, and
     * ReciterService.DEFAULT_RECITER_ID hardcodes 1 to mean it. If the fallback's first
     * entry is not that reciter, those two disagree about who the default is.
     */
    @Test
    fun defaultReciterIsTheLowestShippedId() {
        val shipped = readShippedReciters()
        val default = ReciterDataProvider.getDefaultReciter()

        assertThat(default.id).isEqualTo(shipped.first().id)
        assertThat(default.nameEnglish).isEqualTo(shipped.first().nameEnglish)
    }
}
