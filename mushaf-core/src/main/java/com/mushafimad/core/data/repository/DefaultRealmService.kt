package com.mushafimad.core.data.repository

import android.content.Context
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.data.local.entities.*
import com.mushafimad.core.domain.models.*
import com.mushafimad.core.logging.MushafLogger.LogCategory
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
 *
 * Two separate Realm files are used:
 * - The content realm (copied from assets, read-only data): safe to delete and
 *   recopy if it ever fails to open.
 * - The user realm (bookmarks, reading history, last position, search history):
 *   created on device, never deleted, so user data survives app restarts and
 *   library upgrades.
 *
 * @param context Application context
 * @param useInMemory If true, uses in-memory Realm for testing (no file I/O). Default is false.
 */
internal class DefaultRealmService(
    private val context: Context,
    private val useInMemory: Boolean = false
) : RealmService {

    private var realm: Realm? = null
    @Volatile private var userRealm: Realm? = null
    private val initMutex = Mutex()
    private val userRealmLock = Any()
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val logger get() = MushafLibrary.logger

    companion object {
        private const val REALM_ASSET_NAME = "quran.realm"

        // Bump together with a new bundled asset so the on-device copy from an
        // older release never collides with the new one.
        private const val CONTENT_ASSET_VERSION = 1
        private const val CONTENT_REALM_NAME = "quran-v$CONTENT_ASSET_VERSION.realm"
        private const val CONTENT_SCHEMA_VERSION = 24L

        private const val USER_REALM_NAME = "userdata.realm"
        private const val USER_SCHEMA_VERSION = 1L

        // On-device file name used by releases <= 0.1, which mixed content and
        // user data and wiped the file on every launch.
        private const val LEGACY_REALM_NAME = "quran.realm"

        // Must match the tables stored in the bundled asset (shared with iOS,
        // entity names mapped via @PersistedName).
        private val CONTENT_SCHEMA = setOf(
            ChapterEntity::class,
            VerseEntity::class,
            PageEntity::class,
            PartEntity::class,
            QuarterEntity::class,
            VerseHighlightEntity::class,
            VerseMarkerEntity::class,
            PageHeaderEntity::class,
            ChapterHeaderEntity::class,
            QuranSectionEntity::class
        )

        private val USER_SCHEMA = setOf(
            BookmarkEntity::class,
            ReadingHistoryEntity::class,
            LastReadPositionEntity::class,
            SearchHistoryEntity::class
        )
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
            openContentRealm()
        }
    }

    /**
     * Open the content realm - must be called on background thread
     */
    private fun openContentRealm() {
        // Skip if already initialized
        if (realm != null) return

        logger.debug("Opening content realm (useInMemory=$useInMemory)", LogCategory.REALM)

        if (useInMemory) {
            realm = Realm.open(
                RealmConfiguration.Builder(schema = CONTENT_SCHEMA)
                    .name("$CONTENT_REALM_NAME.in-memory")
                    .inMemory()
                    .build()
            )
            return
        }

        deleteRealmFiles(LEGACY_REALM_NAME)

        realm = try {
            Realm.open(contentConfig())
        } catch (first: Exception) {
            // The on-device copy holds no user data, so an incompatible or
            // corrupt file can simply be recopied from assets. One retry only;
            // if the bundled asset itself is unusable, fail loudly.
            logger.error(
                "Content realm failed to open; recopying from assets",
                first,
                LogCategory.REALM
            )
            deleteRealmFiles(CONTENT_REALM_NAME)
            Realm.open(contentConfig())
        }

        logger.info("Content realm opened", LogCategory.REALM)
    }

    private fun contentConfig(): RealmConfiguration =
        RealmConfiguration.Builder(schema = CONTENT_SCHEMA)
            .name(CONTENT_REALM_NAME)
            .directory(context.filesDir.absolutePath)
            .schemaVersion(CONTENT_SCHEMA_VERSION)
            .initialRealmFile(REALM_ASSET_NAME)
            .build()

    private fun deleteRealmFiles(name: String) {
        val dir = context.filesDir
        File(dir, name).delete()
        File(dir, "$name.lock").delete()
        File(dir, "$name.note").delete()
        File(dir, "$name.management").deleteRecursively()
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

    override fun getUserRealm(): Realm {
        userRealm?.let { return it }
        synchronized(userRealmLock) {
            userRealm?.let { return it }

            val config = if (useInMemory) {
                RealmConfiguration.Builder(schema = USER_SCHEMA)
                    .name("$USER_REALM_NAME.in-memory")
                    .inMemory()
                    .build()
            } else {
                RealmConfiguration.Builder(schema = USER_SCHEMA)
                    .name(USER_REALM_NAME)
                    .directory(context.filesDir.absolutePath)
                    .schemaVersion(USER_SCHEMA_VERSION)
                    .build()
            }

            // Opening is fast (small or empty file) and must never be subject
            // to the content realm's delete-and-recopy self-healing.
            return Realm.open(config).also { userRealm = it }
        }
    }

    override fun close() {
        realm?.close()
        realm = null
        synchronized(userRealmLock) {
            userRealm?.close()
            userRealm = null
        }
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
        val realmInstance = realm ?: return@withContext com.mushafimad.core.utils.QuranUtils.TOTAL_PAGES
        realmInstance.query<PageEntity>().count().find().toInt()
    }

    private suspend fun getPageEntity(number: Int): PageEntity? = withContext(Dispatchers.IO) {
        ensureInitialized()
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

    override suspend fun getChapterHeaders(
        pageNumber: Int,
        mushafType: MushafType
    ): List<com.mushafimad.core.domain.models.ChapterHeader> = withContext(Dispatchers.IO) {
        val page = getPageEntity(pageNumber) ?: return@withContext emptyList()

        val headers = when (mushafType) {
            MushafType.HAFS_1441 -> page.chapterHeaders1441
            MushafType.HAFS_1405 -> page.chapterHeaders1405
        }

        headers.map { it.toDomain() }
    }

    // MARK: - Verse Operations

    override suspend fun getVersesForPage(pageNumber: Int, mushafType: MushafType): List<Verse> =
        withContext(Dispatchers.IO) {
            ensureInitialized()
            val page = getPageEntity(pageNumber)

            if (page == null) {
                logger.warning("Page $pageNumber not found", LogCategory.REALM)
                return@withContext emptyList()
            }

            val verses = when (mushafType) {
                MushafType.HAFS_1441 -> page.verses1441
                MushafType.HAFS_1405 -> page.verses1405
            }

            verses.map { it.toDomain() }
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

        // Known sajda verse IDs (humanReadableID format: "chapter_verse")
        val sajdaVerseKeys = listOf(
            "7_206", "13_15", "16_50", "17_109", "19_58",
            "22_18", "22_77", "25_60", "27_26", "32_15",
            "38_24", "41_38", "53_62", "84_21", "96_19"
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

    private fun ChapterHeaderEntity.toDomain() = com.mushafimad.core.domain.models.ChapterHeader(
        chapterNumber = chapter?.number ?: 0,
        line = line,
        centerX = centerX,
        centerY = centerY
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
