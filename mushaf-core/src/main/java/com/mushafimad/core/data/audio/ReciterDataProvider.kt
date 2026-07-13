package com.mushafimad.core.data.audio

import com.mushafimad.core.domain.models.ReciterInfo

/**
 * Fallback reciter list, used only when not a single timing file can be read.
 *
 * The real source of truth is `assets/ayah_timing/read_<id>.json`: each file carries
 * both the per-ayah timings and the identity of the recitation they were measured
 * against (name, rewaya, audio folder). ReciterService builds the live list from
 * those files and only reaches for this table if every one of them fails to load.
 *
 * This table therefore duplicates data that already ships in the AAR, and it had
 * drifted from it completely - all 18 entries named a different reciter than the
 * timing file with the same id, and pointed at that other reciter's audio. Reciter 1
 * is Ibrahim Al-Akdar, not Abdul Basit; asking for id 1 handed you Abdul Basit's
 * audio while read_1.json timed Al-Akdar's, so the highlighted verse would track a
 * voice nobody was listening to.
 *
 * The entries below are generated from the shipped timing files, and
 * ReciterFallbackMatchesShippedDataTest fails the build if the two ever disagree
 * again. Do not hand-edit this list: change the assets, then regenerate.
 *
 * Internal implementation - not exposed in public API.
 */
internal object ReciterDataProvider {

    /**
     * All reciters the library ships timing data for, in id order.
     */
    val allReciters: List<ReciterInfo> = listOf(
        ReciterInfo(
            id = 1,
            nameArabic = "إبراهيم الأخضر",
            nameEnglish = "Ibrahim Al-Akdar",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/akdr/"
        ),
        ReciterInfo(
            id = 5,
            nameArabic = "أحمد بن علي العجمي",
            nameEnglish = "Ahmad Al-Ajmy",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/ajm/"
        ),
        ReciterInfo(
            id = 9,
            nameArabic = "أحمد نعينع",
            nameEnglish = "Ahmad Nauina",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/ahmad_nu/"
        ),
        ReciterInfo(
            id = 10,
            nameArabic = "أكرم العلاقمي",
            nameEnglish = "Akram Alalaqmi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server9.mp3quran.net/akrm/"
        ),
        ReciterInfo(
            id = 31,
            nameArabic = "سعود الشريم",
            nameEnglish = "Saud Al-Shuraim",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server7.mp3quran.net/shur/"
        ),
        ReciterInfo(
            id = 32,
            nameArabic = "سهل ياسين",
            nameEnglish = "Sahl Yassin",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/shl/"
        ),
        ReciterInfo(
            id = 51,
            nameArabic = "عبدالباسط عبدالصمد",
            nameEnglish = "Abdulbasit Abdulsamad",
            rewaya = "المصحف المجود",
            folderUrl = "https://server7.mp3quran.net/basit/Almusshaf-Al-Mojawwad/"
        ),
        ReciterInfo(
            id = 53,
            nameArabic = "عبدالباسط عبدالصمد",
            nameEnglish = "Abdulbasit Abdulsamad",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server7.mp3quran.net/basit/"
        ),
        ReciterInfo(
            id = 60,
            nameArabic = "عبدالله بصفر",
            nameEnglish = "Abdullah Basfer",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/bsfr/"
        ),
        ReciterInfo(
            id = 62,
            nameArabic = "عبدالله عواد الجهني",
            nameEnglish = "Abdullah Al-Johany",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server13.mp3quran.net/jhn/"
        ),
        ReciterInfo(
            id = 67,
            nameArabic = "عبدالمحسن القاسم",
            nameEnglish = "Abdulmohsen Al-Qasim",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server8.mp3quran.net/qasm/"
        ),
        ReciterInfo(
            id = 74,
            nameArabic = "علي بن عبدالرحمن الحذيفي",
            nameEnglish = "Ali Alhuthaifi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server9.mp3quran.net/hthfi/"
        ),
        ReciterInfo(
            id = 78,
            nameArabic = "عماد زهير حافظ",
            nameEnglish = "Emad Hafez",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/hafz/"
        ),
        ReciterInfo(
            id = 106,
            nameArabic = "محمد الطبلاوي",
            nameEnglish = "Mohammad Al-Tablaway",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server12.mp3quran.net/tblawi/"
        ),
        ReciterInfo(
            id = 112,
            nameArabic = "محمد صديق المنشاوي",
            nameEnglish = "Mohammed Siddiq Al-Minshawi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/minsh/"
        ),
        ReciterInfo(
            id = 118,
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Hussary",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server13.mp3quran.net/husr/"
        ),
        ReciterInfo(
            id = 159,
            nameArabic = "خالد المهنا",
            nameEnglish = "Khalid Almohana",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/mohna/"
        ),
        ReciterInfo(
            id = 256,
            nameArabic = "أحمد خليل شاهين",
            nameEnglish = "Ahmad Shaheen",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server16.mp3quran.net/shaheen/Rewayat-Hafs-A-n-Assem/"
        )
    )

    /**
     * Get reciter by ID
     * @param reciterId The reciter ID
     * @return ReciterInfo if found, null otherwise
     */
    fun getReciterById(reciterId: Int): ReciterInfo? {
        return allReciters.find { it.id == reciterId }
    }

    /**
     * Get all reciter IDs
     */
    fun getAllReciterIds(): List<Int> {
        return allReciters.map { it.id }
    }

    /**
     * Search reciters by name (Arabic or English)
     * @param query Search query
     * @param languageCode Language for search ("ar" for Arabic, "en" for English)
     * @return List of matching reciters
     */
    fun searchReciters(query: String, languageCode: String = "en"): List<ReciterInfo> {
        val normalizedQuery = query.trim().lowercase()
        return allReciters.filter { reciter ->
            when (languageCode) {
                "ar" -> reciter.nameArabic.contains(normalizedQuery, ignoreCase = true)
                else -> reciter.nameEnglish.lowercase().contains(normalizedQuery)
            }
        }
    }

    /**
     * Get reciters by rewaya (recitation style)
     * @param rewaya The rewaya name (e.g., "حفص", "hafs")
     * @return List of reciters with matching rewaya
     */
    fun getRecitersByRewaya(rewaya: String): List<ReciterInfo> {
        val normalizedRewaya = rewaya.trim().lowercase()
        return allReciters.filter { reciter ->
            reciter.rewaya.lowercase().contains(normalizedRewaya)
        }
    }

    /**
     * Get all Hafs reciters
     */
    fun getHafsReciters(): List<ReciterInfo> {
        return allReciters.filter { it.isHafs }
    }

    /**
     * Get the default reciter: the lowest id we ship timing data for.
     * Matches ReciterService.DEFAULT_RECITER_ID.
     */
    fun getDefaultReciter(): ReciterInfo {
        return allReciters.first()
    }
}
