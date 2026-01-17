package com.mushafimad.core.data.repository

import android.content.Context
import com.mushafimad.core.data.local.entities.*
import com.mushafimad.core.domain.models.*
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Implementation of RealmService that provides access to the Quran database
 * Internal API - not exposed to library consumers
 */
internal class RealmServiceImpl(
    private val context: Context
) : RealmService {

    private var realm: Realm? = null
    private var configuration: RealmConfiguration? = null
    private val initMutex = Mutex()
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val REALM_FILE_NAME = "quran.realm"
        private const val SCHEMA_VERSION = 24L  // Match bundled file schema version
    }

    init {
        // Eagerly start initialization on background thread
        // This ensures Realm is ready by the time UI needs it
        initScope.launch {
            ensureInitialized()
        }
    }

    override val isInitialized: Boolean
        get() = realm != null

    /**
     * Internal initialization - runs on background thread
     * Thread-safe lazy initialization with mutex
     */
    private suspend fun ensureInitialized() = initMutex.withLock {
        // Skip if already initialized
        if (realm != null) return@withLock

        withContext(Dispatchers.IO) {
            initializeRealm()
        }
    }

    /**
     * Initialize Realm - must be called on background thread
     */
    private fun initializeRealm() {
        // Skip if already initialized
        if (realm != null) return

        println("RealmService: Initializing Realm...")

        // Get the bundled Realm file from assets
        val assetManager = context.assets
        val realmInputStream = try {
            assetManager.open(REALM_FILE_NAME)
        } catch (e: Exception) {
            println("RealmService: ERROR - Could not find $REALM_FILE_NAME in assets")
            throw IllegalStateException("Could not find $REALM_FILE_NAME in assets", e)
        }

        // Get app's internal storage directory
        val appFilesDir = context.filesDir
        val realmFile = File(appFilesDir, REALM_FILE_NAME)

        println("RealmService: Realm file path: ${realmFile.absolutePath}")
        println("RealmService: Realm file exists: ${realmFile.exists()}, size: ${if (realmFile.exists()) realmFile.length() else 0} bytes")

        // TEMPORARY: Force fresh copy from assets to fix schema mismatch
        // Delete existing file if it exists
        if (realmFile.exists()) {
            println("RealmService: Deleting existing realm file to force fresh copy...")
            realmFile.delete()
            // Also delete lock files
            File(appFilesDir, "$REALM_FILE_NAME.lock").delete()
            File(appFilesDir, "$REALM_FILE_NAME.management").delete()
        }

        // Copy the bundled Realm file from assets
        println("RealmService: Copying realm file from assets...")
        realmInputStream.use { input ->
            realmFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        println("RealmService: Copied realm file, new size: ${realmFile.length()} bytes")

        // Configure Realm
        val config = RealmConfiguration.Builder(
            schema = setOf(
                // Core Quran entities
                ChapterEntity::class,
                VerseEntity::class,
                PageEntity::class,
                PartEntity::class,
                QuarterEntity::class,
                VerseHighlightEntity::class,
                VerseMarkerEntity::class,
                PageHeaderEntity::class,
                ChapterHeaderEntity::class,
                QuranSectionEntity::class,
                // User data entities (Week 7)
                BookmarkEntity::class,
                ReadingHistoryEntity::class,
                LastReadPositionEntity::class,
                // Search history entities (Week 8)
                SearchHistoryEntity::class
            )
        )
            .name(REALM_FILE_NAME)
            .schemaVersion(SCHEMA_VERSION)
            .directory(appFilesDir.absolutePath)
            .build()

        configuration = config
        realm = Realm.open(config)

        println("RealmService: Realm opened successfully")
        println("RealmService: Testing query - chapter count...")
        try {
            val chapterCount = realm?.query<ChapterEntity>()?.count()?.find()
            println("RealmService: Found $chapterCount chapters in database")
            val verseCount = realm?.query<VerseEntity>()?.count()?.find()
            println("RealmService: Found $verseCount verses in database")
        } catch (e: Exception) {
            println("RealmService: ERROR querying database: ${e.message}")
        }
    }

    /**
     * Public suspend initialize for manual initialization if needed
     */
    override suspend fun initialize() {
        ensureInitialized()
    }

    override fun getRealm(): Realm {
        return realm ?: throw IllegalStateException("Realm not initialized. Call initialize() first.")
    }

    // MARK: - Chapter Operations

    override suspend fun fetchAllChaptersAsync(): List<Chapter> = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: throw IllegalStateException("Realm not initialized")
        realmInstance.query<ChapterEntity>()
            .sort("number")
            .find()
            .map { it.toDomain() }
    }

    override suspend fun getChapter(number: Int): Chapter? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext null
        realmInstance.query<ChapterEntity>("number == $0", number)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun getChapterForPage(pageNumber: Int): Chapter? = withContext(Dispatchers.IO) {
        val page = getPageEntity(pageNumber) ?: return@withContext null

        // Check if page has chapter headers (new chapters starting on this page)
        val firstHeader = page.chapterHeaders1441.firstOrNull()
        if (firstHeader != null) {
            return@withContext firstHeader.chapter?.toDomain()
        }

        // Otherwise, get the chapter of the first verse on the page
        val firstVerse = page.verses1441.firstOrNull()
        return@withContext firstVerse?.chapter?.toDomain()
    }

    override suspend fun getChaptersOnPage(pageNumber: Int): List<Chapter> = withContext(Dispatchers.IO) {
        val page = getPageEntity(pageNumber) ?: return@withContext emptyList()

        val chapters = mutableSetOf<ChapterEntity>()

        // Add chapters from headers
        page.chapterHeaders1441.forEach { header ->
            header.chapter?.let { chapters.add(it) }
        }

        // Add chapters from verses
        page.verses1441.forEach { verse ->
            verse.chapter?.let { chapters.add(it) }
        }

        chapters.sortedBy { it.number }.map { it.toDomain() }
    }

    // MARK: - Page Operations

    override suspend fun getPage(number: Int): Page? = withContext(Dispatchers.IO) {
        getPageEntity(number)?.toDomain()
    }

    override suspend fun fetchPageAsync(number: Int): Page? = getPage(number)

    override suspend fun getTotalPages(): Int = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext 604
        realmInstance.query<PageEntity>().count().find().toInt()
    }

    private suspend fun getPageEntity(number: Int): PageEntity? = withContext(Dispatchers.IO) {
        val realmInstance = realm ?: return@withContext null
        realmInstance.query<PageEntity>("number == $0", number)
            .first()
            .find()
    }

    // MARK: - Page Header Operations

    override suspend fun getPageHeaderInfo(pageNumber: Int, mushafType: MushafType): PageHeaderInfo? =
        withContext(Dispatchers.IO) {
            val page = getPageEntity(pageNumber) ?: return@withContext null

            val header = when (mushafType) {
                MushafType.HAFS_1441 -> page.header1441
                MushafType.HAFS_1405 -> page.header1405
            } ?: return@withContext null

            PageHeaderInfo(
                partNumber = header.part?.number,
                partArabicTitle = header.part?.arabicTitle,
                partEnglishTitle = header.part?.englishTitle,
                hizbNumber = header.quarter?.hizbNumber,
                hizbFraction = header.quarter?.hizbFraction,
                quarterArabicTitle = header.quarter?.arabicTitle,
                quarterEnglishTitle = header.quarter?.englishTitle,
                chapters = header.chapters.map { chapter ->
                    ChapterInfo(
                        number = chapter.number,
                        arabicTitle = chapter.arabicTitle,
                        englishTitle = chapter.englishTitle
                    )
                }
            )
        }

    // MARK: - Verse Operations

    override suspend fun getVersesForPage(pageNumber: Int, mushafType: MushafType): List<Verse> =
        withContext(Dispatchers.IO) {
            ensureInitialized()
            println("RealmService: getVersesForPage($pageNumber, $mushafType)")
            val page = getPageEntity(pageNumber)

            if (page == null) {
                println("RealmService: ERROR - Page $pageNumber not found!")
                return@withContext emptyList()
            }

            println("RealmService: Found page ${page.number}")

            val verses = when (mushafType) {
                MushafType.HAFS_1441 -> {
                    println("RealmService: Getting verses1441, count: ${page.verses1441.size}")
                    page.verses1441
                }
                MushafType.HAFS_1405 -> {
                    println("RealmService: Getting verses1405, count: ${page.verses1405.size}")
                    page.verses1405
                }
            }

            val result = verses.map { it.toDomain() }
            println("RealmService: Returning ${result.size} verses")
            result
        }

    override suspend fun getVersesForChapter(chapterNumber: Int): List<Verse> = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext emptyList()
        val chapter = realmInstance.query<ChapterEntity>("number == $0", chapterNumber)
            .first()
            .find() ?: return@withContext emptyList()

        chapter.verses.map { it.toDomain() }
    }

    override suspend fun getVerse(chapterNumber: Int, verseNumber: Int): Verse? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext null
        val humanReadableID = "${chapterNumber}_${verseNumber}"

        realmInstance.query<VerseEntity>("humanReadableID == $0", humanReadableID)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun getSajdaVerses(): List<Verse> = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext emptyList()

        // Known sajda verse IDs
        val sajdaVerseKeys = listOf(
            "7:206", "13:15", "16:50", "17:109", "19:58",
            "22:18", "22:77", "25:60", "27:26", "32:15",
            "38:24", "41:38", "53:62", "84:21", "96:19"
        )

        sajdaVerseKeys.mapNotNull { key ->
            realmInstance.query<VerseEntity>("humanReadableID == $0", key)
                .first()
                .find()
                ?.toDomain()
        }
    }

    // MARK: - Part (Juz) Operations

    override suspend fun getPart(number: Int): Part? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext null
        realmInstance.query<PartEntity>("number == $0", number)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun getPartForPage(pageNumber: Int): Part? = withContext(Dispatchers.IO) {
        val page = getPageEntity(pageNumber) ?: return@withContext null
        page.header1441?.part?.toDomain()
    }

    override suspend fun getPartForVerse(chapterNumber: Int, verseNumber: Int): Part? =
        withContext(Dispatchers.IO) {
            val verse = getVerse(chapterNumber, verseNumber) ?: return@withContext null
            val realmInstance = realm ?: return@withContext null

            realmInstance.query<VerseEntity>("humanReadableID == $0", "${chapterNumber}_${verseNumber}")
                .first()
                .find()
                ?.part
                ?.toDomain()
        }

    override suspend fun fetchAllPartsAsync(): List<Part> = withContext(Dispatchers.IO) {
        val realmInstance = realm ?: throw IllegalStateException("Realm not initialized")
        realmInstance.query<PartEntity>()
            .sort("number")
            .find()
            .map { it.toDomain() }
    }

    // MARK: - Quarter (Hizb) Operations

    override suspend fun getQuarter(hizbNumber: Int, fraction: Int): Quarter? = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext null
        realmInstance.query<QuarterEntity>("hizbNumber == $0 AND hizbFraction == $1", hizbNumber, fraction)
            .first()
            .find()
            ?.toDomain()
    }

    override suspend fun getQuarterForPage(pageNumber: Int): Quarter? = withContext(Dispatchers.IO) {
        val page = getPageEntity(pageNumber) ?: return@withContext null
        page.header1441?.quarter?.toDomain()
    }

    override suspend fun getQuarterForVerse(chapterNumber: Int, verseNumber: Int): Quarter? =
        withContext(Dispatchers.IO) {
            ensureInitialized()
            val realmInstance = realm ?: return@withContext null

            realmInstance.query<VerseEntity>("humanReadableID == $0", "${chapterNumber}_${verseNumber}")
                .first()
                .find()
                ?.quarter
                ?.toDomain()
        }

    override suspend fun fetchAllQuartersAsync(): List<Quarter> = withContext(Dispatchers.IO) {
        val realmInstance = realm ?: throw IllegalStateException("Realm not initialized")
        realmInstance.query<QuarterEntity>()
            .find()
            .sortedWith(compareBy({ it.hizbNumber }, { it.hizbFraction }))
            .map { it.toDomain() }
    }

    // MARK: - Search Operations

    override suspend fun searchVerses(query: String): List<Verse> = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext emptyList()
        realmInstance.query<VerseEntity>("searchableText CONTAINS[c] $0", query)
            .find()
            .map { it.toDomain() }
    }

    override suspend fun searchChapters(query: String): List<Chapter> = withContext(Dispatchers.IO) {
        ensureInitialized()
        val realmInstance = realm ?: return@withContext emptyList()
        realmInstance.query<ChapterEntity>(
            "searchableText CONTAINS[c] $0 OR searchableKeywords CONTAINS[c] $0",
            query
        )
            .find()
            .map { it.toDomain() }
    }

    // MARK: - Mapper Extensions

    private fun ChapterEntity.toDomain() = Chapter(
        identifier = identifier,
        number = number,
        isMeccan = isMeccan,
        title = title,
        arabicTitle = arabicTitle,
        englishTitle = englishTitle,
        titleCodePoint = titleCodePoint,
        searchableText = searchableText,
        searchableKeywords = searchableKeywords,
        versesCount = verses.size
    )

    private fun VerseEntity.toDomain() = Verse(
        verseID = verseID,
        humanReadableID = humanReadableID,
        number = number,
        text = text,
        textWithoutTashkil = textWithoutTashkil,
        uthmanicHafsText = uthmanicHafsText,
        hafsSmartText = hafsSmartText,
        searchableText = searchableText,
        chapterNumber = chapter?.number ?: 0,
        pageNumber = page1441?.number ?: 0,
        partNumber = part?.number ?: 0,
        hizbNumber = quarter?.hizbNumber ?: 0,
        marker1441 = marker1441?.toDomain(),
        marker1405 = marker1405?.toDomain(),
        highlights1441 = highlights1441.map { it.toDomain() },
        highlights1405 = highlights1405.map { it.toDomain() }
    )

    private fun VerseMarkerEntity.toDomain() = com.mushafimad.core.domain.models.VerseMarker(
        numberCodePoint = numberCodePoint,
        line = line,
        centerX = centerX,
        centerY = centerY
    )

    private fun VerseHighlightEntity.toDomain() = com.mushafimad.core.domain.models.VerseHighlight(
        line = line,
        left = left,
        right = right
    )

    private fun PageEntity.toDomain() = Page(
        identifier = identifier,
        number = number,
        isRight = isRight
    )

    private fun PartEntity.toDomain() = Part(
        identifier = identifier,
        number = number,
        arabicTitle = arabicTitle,
        englishTitle = englishTitle
    )

    private fun QuarterEntity.toDomain() = Quarter(
        identifier = identifier,
        hizbNumber = hizbNumber,
        hizbFraction = hizbFraction,
        arabicTitle = arabicTitle,
        englishTitle = englishTitle,
        partNumber = part?.number ?: 0
    )
}
