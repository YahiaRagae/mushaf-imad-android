package com.mushafimad.library.utils

/**
 * Utility functions for Quran-related operations
 * Public API - exposed to library consumers
 */
object QuranUtils {

    /**
     * Total number of chapters in the Quran
     */
    const val TOTAL_CHAPTERS = 114

    /**
     * Total number of verses in the Quran
     */
    const val TOTAL_VERSES = 6236

    /**
     * Total number of pages in the Mushaf
     */
    const val TOTAL_PAGES = 604

    /**
     * Total number of Juz (parts)
     */
    const val TOTAL_PARTS = 30

    /**
     * Total number of Hizb (60 hizbs)
     */
    const val TOTAL_HIZB = 60

    /**
     * Sajda verse positions (chapter:verse)
     */
    val SAJDA_VERSES = listOf(
        "7:206", "13:15", "16:50", "17:109", "19:58",
        "22:18", "22:77", "25:60", "27:26", "32:15",
        "38:24", "41:38", "53:62", "84:21", "96:19"
    )

    /**
     * Check if a page number is valid
     */
    fun isValidPageNumber(pageNumber: Int): Boolean {
        return pageNumber in 1..TOTAL_PAGES
    }

    /**
     * Check if a chapter number is valid
     */
    fun isValidChapterNumber(chapterNumber: Int): Boolean {
        return chapterNumber in 1..TOTAL_CHAPTERS
    }

    /**
     * Check if a part (Juz) number is valid
     */
    fun isValidPartNumber(partNumber: Int): Boolean {
        return partNumber in 1..TOTAL_PARTS
    }

    /**
     * Check if a Hizb number is valid
     */
    fun isValidHizbNumber(hizbNumber: Int): Boolean {
        return hizbNumber in 1..TOTAL_HIZB
    }

    /**
     * Parse verse reference (e.g., "2:255" -> Pair(2, 255))
     */
    fun parseVerseReference(reference: String): Pair<Int, Int>? {
        val parts = reference.split(":")
        if (parts.size != 2) return null

        val chapterNumber = parts[0].toIntOrNull() ?: return null
        val verseNumber = parts[1].toIntOrNull() ?: return null

        return if (isValidChapterNumber(chapterNumber) && verseNumber > 0) {
            Pair(chapterNumber, verseNumber)
        } else {
            null
        }
    }

    /**
     * Format verse reference (Pair(2, 255) -> "2:255")
     */
    fun formatVerseReference(chapterNumber: Int, verseNumber: Int): String {
        return "$chapterNumber:$verseNumber"
    }

    /**
     * Get Juz number for a page
     */
    fun getJuzForPage(pageNumber: Int): Int {
        return when {
            pageNumber <= 0 -> 1
            pageNumber > TOTAL_PAGES -> TOTAL_PARTS
            else -> ((pageNumber - 1) / 20) + 1
        }
    }

    /**
     * Get starting page for a Juz
     */
    fun getStartingPageForJuz(juzNumber: Int): Int {
        return when {
            juzNumber <= 1 -> 1
            juzNumber >= TOTAL_PARTS -> 582
            else -> ((juzNumber - 1) * 20) + 1
        }
    }

    /**
     * Remove Arabic diacritics (Tashkeel) from text
     */
    fun removeTashkeel(text: String): String {
        val tashkeelPattern = Regex("[\u064B-\u0652\u0670]")
        return text.replace(tashkeelPattern, "")
    }

    /**
     * Normalize Arabic text for search
     */
    fun normalizeArabicText(text: String): String {
        return removeTashkeel(text)
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .lowercase()
            .trim()
    }

    /**
     * Check if a verse has sajda (prostration)
     */
    fun isSajdaVerse(chapterNumber: Int, verseNumber: Int): Boolean {
        val reference = formatVerseReference(chapterNumber, verseNumber)
        return reference in SAJDA_VERSES
    }

    /**
     * Get Bismillah text in Arabic
     */
    fun getBismillah(): String {
        return "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    }

    /**
     * Chapters that don't start with Bismillah
     */
    val CHAPTERS_WITHOUT_BISMILLAH = setOf(1, 9)

    /**
     * Check if chapter starts with Bismillah
     */
    fun chapterHasBismillah(chapterNumber: Int): Boolean {
        return chapterNumber !in CHAPTERS_WITHOUT_BISMILLAH
    }
}
