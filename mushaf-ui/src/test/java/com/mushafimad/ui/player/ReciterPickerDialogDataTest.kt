package com.mushafimad.ui.player

import com.mushafimad.core.domain.models.ReciterInfo
import org.junit.Assert.*
import org.junit.Test

class ReciterPickerDialogDataTest {

    private fun testReciter(
        id: Int = 1,
        nameArabic: String = "إبراهيم الأكدر",
        nameEnglish: String = "Ibrahim Al-Akdar",
        rewaya: String = "حفص عن عاصم",
        folderUrl: String = "https://example.com/"
    ) = ReciterInfo(id, nameArabic, nameEnglish, rewaya, folderUrl)

    // ===== QA-5.1: ReciterPickerDialog =====

    // TC-5.1: ReciterInfo getDisplayName returns English by default
    @Test
    fun `getDisplayName returns English name by default`() {
        val reciter = testReciter()
        assertEquals("Ibrahim Al-Akdar", reciter.getDisplayName())
    }

    // TC-5.2: ReciterInfo getDisplayName returns Arabic when ar
    @Test
    fun `getDisplayName returns Arabic name when language is ar`() {
        val reciter = testReciter()
        assertEquals("إبراهيم الأكدر", reciter.getDisplayName("ar"))
    }

    // TC-5.3: ReciterInfo getDisplayName returns English for en
    @Test
    fun `getDisplayName returns English name when language is en`() {
        val reciter = testReciter()
        assertEquals("Ibrahim Al-Akdar", reciter.getDisplayName("en"))
    }

    // TC-5.4: ReciterInfo getAudioUrl formats chapter number
    @Test
    fun `getAudioUrl formats chapter number with 3-digit padding`() {
        val reciter = testReciter(folderUrl = "https://server.com/audio/")
        assertEquals("https://server.com/audio/001.mp3", reciter.getAudioUrl(1))
        assertEquals("https://server.com/audio/010.mp3", reciter.getAudioUrl(10))
        assertEquals("https://server.com/audio/114.mp3", reciter.getAudioUrl(114))
    }

    // TC-5.5: ReciterInfo isHafs returns true for Hafs reciters
    @Test
    fun `isHafs returns true for Hafs reciter`() {
        val reciter = testReciter(rewaya = "حفص عن عاصم")
        assertTrue(reciter.isHafs)
    }

    // TC-5.6: ReciterInfo isHafs returns true for English hafs
    @Test
    fun `isHafs returns true for English hafs keyword`() {
        val reciter = testReciter(rewaya = "Hafs an Asim")
        assertTrue(reciter.isHafs)
    }

    // TC-5.7: ReciterInfo isWarsh returns true for Warsh reciters
    @Test
    fun `isWarsh returns true for Warsh reciter`() {
        val reciter = testReciter(rewaya = "ورش عن نافع")
        assertTrue(reciter.isWarsh)
    }

    // TC-5.8: ReciterInfo isWarsh returns true for English warsh
    @Test
    fun `isWarsh returns true for English warsh keyword`() {
        val reciter = testReciter(rewaya = "Warsh an Nafi")
        assertTrue(reciter.isWarsh)
    }

    // TC-5.9: ReciterInfo isHafs returns false for Warsh reciter
    @Test
    fun `isHafs returns false for Warsh reciter`() {
        val reciter = testReciter(rewaya = "ورش عن نافع")
        assertFalse(reciter.isHafs)
    }

    // TC-5.10: ReciterInfo isWarsh returns false for Hafs reciter
    @Test
    fun `isWarsh returns false for Hafs reciter`() {
        val reciter = testReciter(rewaya = "حفص عن عاصم")
        assertFalse(reciter.isWarsh)
    }

    // TC-5.11: Selection comparison uses id
    @Test
    fun `reciter selection comparison works by id`() {
        val r1 = testReciter(id = 1)
        val r2 = testReciter(id = 2)
        val selected = r1

        assertTrue(r1.id == selected.id)
        assertFalse(r2.id == selected.id)
    }

    // TC-5.12: Empty reciter list
    @Test
    fun `empty reciter list has size 0`() {
        val reciters = emptyList<ReciterInfo>()
        assertTrue(reciters.isEmpty())
    }

    // TC-5.13: Multiple reciters in list
    @Test
    fun `reciter list maintains order and distinct ids`() {
        val reciters = listOf(
            testReciter(id = 1, nameEnglish = "Reciter A"),
            testReciter(id = 2, nameEnglish = "Reciter B"),
            testReciter(id = 3, nameEnglish = "Reciter C")
        )
        assertEquals(3, reciters.size)
        assertEquals(1, reciters[0].id)
        assertEquals(3, reciters[2].id)
        assertEquals(3, reciters.map { it.id }.distinct().size)
    }

    // TC-5.14: ReciterInfo data class equality
    @Test
    fun `ReciterInfo data class equality works correctly`() {
        val r1 = testReciter(id = 1)
        val r2 = testReciter(id = 1)
        assertEquals(r1, r2)
    }

    // TC-5.15: ReciterInfo data class inequality
    @Test
    fun `ReciterInfo with different ids are not equal`() {
        val r1 = testReciter(id = 1)
        val r2 = testReciter(id = 2)
        assertNotEquals(r1, r2)
    }

    // TC-5.16: ReciterInfo rewaya field preserved
    @Test
    fun `rewaya field is preserved in ReciterInfo`() {
        val reciter = testReciter(rewaya = "حفص عن عاصم")
        assertEquals("حفص عن عاصم", reciter.rewaya)
    }

    // TC-5.17: ReciterInfo copy changes only specified fields
    @Test
    fun `ReciterInfo copy preserves unmodified fields`() {
        val original = testReciter(id = 1, nameEnglish = "Original")
        val copy = original.copy(nameEnglish = "Modified")

        assertEquals(1, copy.id)
        assertEquals("Modified", copy.nameEnglish)
        assertEquals(original.nameArabic, copy.nameArabic)
        assertEquals(original.rewaya, copy.rewaya)
    }

    // TC-5.18: getAudioUrl with edge chapter numbers
    @Test
    fun `getAudioUrl handles chapter boundary values`() {
        val reciter = testReciter(folderUrl = "https://server.com/")
        assertEquals("https://server.com/001.mp3", reciter.getAudioUrl(1))
        assertEquals("https://server.com/114.mp3", reciter.getAudioUrl(114))
    }

    // TC-5.19: ReciterInfo hashCode consistency
    @Test
    fun `ReciterInfo hashCode is consistent for equal objects`() {
        val r1 = testReciter(id = 5)
        val r2 = testReciter(id = 5)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    // TC-5.20: Filtering reciters by rewaya type
    @Test
    fun `filtering reciters by Hafs works`() {
        val reciters = listOf(
            testReciter(id = 1, rewaya = "حفص عن عاصم"),
            testReciter(id = 2, rewaya = "ورش عن نافع"),
            testReciter(id = 3, rewaya = "حفص عن عاصم")
        )
        val hafsOnly = reciters.filter { it.isHafs }
        assertEquals(2, hafsOnly.size)
    }
}
