package com.mushafimad.library.data.audio

import com.mushafimad.library.domain.models.ReciterInfo

/**
 * Provider for all available Quran reciters
 * Data matches iOS implementation for compatibility
 * Internal implementation - not exposed in public API
 */
internal object ReciterDataProvider {

    /**
     * List of all available reciters with timing data
     */
    val allReciters: List<ReciterInfo> = listOf(
        ReciterInfo(
            id = 1,
            nameArabic = "عبد الباسط عبد الصمد",
            nameEnglish = "Abdul Basit Abdul Samad",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/abas_64/"
        ),
        ReciterInfo(
            id = 5,
            nameArabic = "محمد صديق المنشاوي",
            nameEnglish = "Mohamed Siddiq Al-Minshawi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/minsh/Rewayat-Hafs-A-n-Assem/"
        ),
        ReciterInfo(
            id = 9,
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Hussary",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server13.mp3quran.net/husr/"
        ),
        ReciterInfo(
            id = 10,
            nameArabic = "محمود خليل الحصري (مجود)",
            nameEnglish = "Mahmoud Khalil Al-Hussary (Mujawwad)",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server13.mp3quran.net/husr/Mujawwad/"
        ),
        ReciterInfo(
            id = 31,
            nameArabic = "مشاري راشد العفاسي",
            nameEnglish = "Mishari Rashid Al-Afasy",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server8.mp3quran.net/afs/"
        ),
        ReciterInfo(
            id = 32,
            nameArabic = "سعد الغامدي",
            nameEnglish = "Saad Al-Ghamdi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server7.mp3quran.net/s_gmd/"
        ),
        ReciterInfo(
            id = 51,
            nameArabic = "ماهر المعيقلي",
            nameEnglish = "Maher Al-Muaiqly",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server12.mp3quran.net/maher/"
        ),
        ReciterInfo(
            id = 53,
            nameArabic = "عبد الرحمن السديس",
            nameEnglish = "Abdul Rahman Al-Sudais",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/sds/"
        ),
        ReciterInfo(
            id = 60,
            nameArabic = "سعود الشريم",
            nameEnglish = "Saud Al-Shuraim",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server7.mp3quran.net/shur/"
        ),
        ReciterInfo(
            id = 62,
            nameArabic = "أحمد بن علي العجمي",
            nameEnglish = "Ahmed ibn Ali Al-Ajmi",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/ajm/"
        ),
        ReciterInfo(
            id = 67,
            nameArabic = "ياسر الدوسري",
            nameEnglish = "Yasser Al-Dosari",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/yasser/"
        ),
        ReciterInfo(
            id = 74,
            nameArabic = "عبد الله بصفر",
            nameEnglish = "Abdullah Basfar",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/bsfr/"
        ),
        ReciterInfo(
            id = 78,
            nameArabic = "خليفة الطنيجي",
            nameEnglish = "Khalifa Al-Tunaiji",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/taniji/"
        ),
        ReciterInfo(
            id = 106,
            nameArabic = "ناصر القطامي",
            nameEnglish = "Nasser Al-Qatami",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server6.mp3quran.net/qtm/"
        ),
        ReciterInfo(
            id = 112,
            nameArabic = "عبد الله الجهني",
            nameEnglish = "Abdullah Al-Juhani",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server11.mp3quran.net/jhn/"
        ),
        ReciterInfo(
            id = 118,
            nameArabic = "بندر بليلة",
            nameEnglish = "Bandar Baleela",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/bnd/"
        ),
        ReciterInfo(
            id = 159,
            nameArabic = "محمد أيوب",
            nameEnglish = "Muhammad Ayyub",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server8.mp3quran.net/ayyub/"
        ),
        ReciterInfo(
            id = 256,
            nameArabic = "عبد الله المطرود",
            nameEnglish = "Abdullah Al-Matroud",
            rewaya = "حفص عن عاصم",
            folderUrl = "https://server10.mp3quran.net/mat/"
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
     * Get default reciter (Abdul Basit)
     */
    fun getDefaultReciter(): ReciterInfo {
        return allReciters.first()
    }
}
