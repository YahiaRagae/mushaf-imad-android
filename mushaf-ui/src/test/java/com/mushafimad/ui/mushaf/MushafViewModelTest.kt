package com.mushafimad.ui.mushaf

import com.mushafimad.core.domain.models.*
import com.mushafimad.core.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MushafViewModelTest {

    private lateinit var verseRepository: VerseRepository
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var readingHistoryRepository: ReadingHistoryRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(): MushafViewModel {
        return MushafViewModel(
            verseRepository, chapterRepository,
            readingHistoryRepository, preferencesRepository
        )
    }

    private fun testVerse(
        chapterNumber: Int = 1, number: Int = 1,
        pageNumber: Int = 1
    ) = Verse(
        verseID = chapterNumber * 1000 + number,
        humanReadableID = "${chapterNumber}_$number",
        number = number, text = "verse text",
        textWithoutTashkil = "verse text",
        uthmanicHafsText = "verse text",
        hafsSmartText = "verse text",
        searchableText = "verse text",
        chapterNumber = chapterNumber,
        pageNumber = pageNumber
    )

    private fun testChapter(number: Int = 1) = Chapter(
        identifier = number, number = number,
        isMeccan = true, title = "Al-Fatihah",
        arabicTitle = "الفاتحة", englishTitle = "The Opening",
        titleCodePoint = "", searchableText = "",
        searchableKeywords = "", versesCount = 7
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        verseRepository = mockk(relaxed = true)
        chapterRepository = mockk(relaxed = true)
        readingHistoryRepository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)

        coEvery { preferencesRepository.getMushafTypeFlow() } returns flowOf(MushafType.HAFS_1441)
        coEvery { preferencesRepository.getCurrentPageFlow() } returns flowOf(1)
        coEvery { readingHistoryRepository.getLastReadPosition(any()) } returns null
        coEvery { verseRepository.getVersesForPage(any(), any()) } returns listOf(testVerse())
        coEvery { chapterRepository.getChapter(any()) } returns testChapter()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== QA-2.1: MushafView Basic Rendering =====

    // TC-2.1: Initial state defaults
    @Test
    fun `initial state has correct defaults`() = runTest {
        val vm = createViewModel()
        val state = vm.uiState.value

        assertEquals(MushafType.HAFS_1441, state.mushafType)
        assertFalse(state.isLoading)
    }

    // TC-2.2: loadPage sets loading then completes
    @Test
    fun `loadPage updates verses and clears loading`() = runTest {
        val verses = listOf(testVerse(1, 1), testVerse(1, 2))
        coEvery { verseRepository.getVersesForPage(1, any()) } returns verses

        val vm = createViewModel()
        vm.loadPage(1)

        val state = vm.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(2, state.verses.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // TC-2.3: loadPage with invalid page shows error
    @Test
    fun `loadPage with page 0 sets error`() = runTest {
        val vm = createViewModel()
        vm.loadPage(0)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Invalid page"))
    }

    // TC-2.4: loadPage with page 605 sets error
    @Test
    fun `loadPage with page 605 sets error`() = runTest {
        val vm = createViewModel()
        vm.loadPage(605)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Invalid page"))
    }

    // TC-2.5: loadPage with empty verses shows error
    @Test
    fun `loadPage with empty verses shows error`() = runTest {
        coEvery { verseRepository.getVersesForPage(500, any()) } returns emptyList()

        val vm = createViewModel()
        vm.loadPage(500)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("No verses found"))
    }

    // TC-2.6: loadPage persists page to preferences
    @Test
    fun `loadPage persists current page to preferences`() = runTest {
        val vm = createViewModel()
        vm.loadPage(42)

        coVerify { preferencesRepository.setCurrentPage(42) }
    }

    // TC-2.7: nextPage increments
    @Test
    fun `nextPage increments current page`() = runTest {
        val vm = createViewModel()
        vm.loadPage(1)
        vm.nextPage()

        assertEquals(2, vm.uiState.value.currentPage)
    }

    // TC-2.8: nextPage at 604 does nothing
    @Test
    fun `nextPage at page 604 does not exceed 604`() = runTest {
        coEvery { verseRepository.getVersesForPage(604, any()) } returns listOf(testVerse(pageNumber = 604))
        val vm = createViewModel()
        vm.loadPage(604)
        vm.nextPage()

        assertEquals(604, vm.uiState.value.currentPage)
    }

    // TC-2.9: previousPage decrements
    @Test
    fun `previousPage decrements current page`() = runTest {
        coEvery { verseRepository.getVersesForPage(5, any()) } returns listOf(testVerse(pageNumber = 5))
        coEvery { verseRepository.getVersesForPage(4, any()) } returns listOf(testVerse(pageNumber = 4))
        val vm = createViewModel()
        vm.loadPage(5)
        vm.previousPage()

        assertEquals(4, vm.uiState.value.currentPage)
    }

    // TC-2.10: previousPage at 1 does nothing
    @Test
    fun `previousPage at page 1 does not go below 1`() = runTest {
        val vm = createViewModel()
        vm.loadPage(1)
        vm.previousPage()

        assertEquals(1, vm.uiState.value.currentPage)
    }

    // TC-2.11: goToChapter navigates
    @Test
    fun `goToChapter navigates to correct page`() = runTest {
        val verse = testVerse(chapterNumber = 2, number = 1, pageNumber = 2)
        coEvery { verseRepository.getVerse(2, 1) } returns verse
        coEvery { verseRepository.getVersesForPage(2, any()) } returns listOf(verse)

        val vm = createViewModel()
        vm.goToChapter(2)

        assertEquals(2, vm.uiState.value.currentPage)
        assertEquals(2, vm.uiState.value.currentChapter)
    }

    // TC-2.12: goToChapter with invalid chapter sets error
    @Test
    fun `goToChapter with chapter 0 sets error`() = runTest {
        val vm = createViewModel()
        vm.goToChapter(0)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Invalid chapter"))
    }

    // TC-2.13: goToChapter with chapter 115 sets error
    @Test
    fun `goToChapter with chapter 115 sets error`() = runTest {
        val vm = createViewModel()
        vm.goToChapter(115)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Invalid chapter"))
    }

    // TC-2.14: selectVerse updates selected verse
    @Test
    fun `selectVerse updates state with selected verse`() = runTest {
        val verse = testVerse(chapterNumber = 2, number = 255, pageNumber = 42)
        val vm = createViewModel()
        vm.selectVerse(verse)

        assertEquals(verse, vm.uiState.value.selectedVerse)
        assertEquals(2, vm.uiState.value.currentChapter)
        assertEquals(255, vm.uiState.value.currentVerse)
    }

    // TC-2.15: clearSelection clears selected verse
    @Test
    fun `clearSelection resets selectedVerse to null`() = runTest {
        val verse = testVerse()
        val vm = createViewModel()
        vm.selectVerse(verse)
        vm.clearSelection()

        assertNull(vm.uiState.value.selectedVerse)
    }

    // TC-2.16: toggleVerseSelection adds and removes
    @Test
    fun `toggleVerseSelection adds verse then removes on second toggle`() = runTest {
        val verse = testVerse()
        val vm = createViewModel()

        vm.toggleVerseSelection(verse)
        assertEquals(1, vm.uiState.value.selectedVerses.size)

        vm.toggleVerseSelection(verse)
        assertEquals(0, vm.uiState.value.selectedVerses.size)
    }

    // TC-2.17: clearAllSelections empties set
    @Test
    fun `clearAllSelections empties selectedVerses set`() = runTest {
        val v1 = testVerse(1, 1)
        val v2 = testVerse(1, 2)
        val vm = createViewModel()

        vm.toggleVerseSelection(v1)
        vm.toggleVerseSelection(v2)
        assertEquals(2, vm.uiState.value.selectedVerses.size)

        vm.clearAllSelections()
        assertTrue(vm.uiState.value.selectedVerses.isEmpty())
    }

    // TC-2.18: clearError clears error
    @Test
    fun `clearError resets error to null`() = runTest {
        val vm = createViewModel()
        vm.loadPage(0) // triggers error
        assertNotNull(vm.uiState.value.error)

        vm.clearError()
        assertNull(vm.uiState.value.error)
    }

    // TC-2.19: getPageInfo returns correct info
    @Test
    fun `getPageInfo returns correct page info`() = runTest {
        coEvery { verseRepository.getVersesForPage(100, any()) } returns listOf(testVerse(pageNumber = 100))
        val vm = createViewModel()
        vm.loadPage(100)

        val info = vm.getPageInfo()
        assertEquals(100, info.pageNumber)
        assertEquals(604, info.totalPages)
        assertTrue(info.progress in 1..100)
    }

    // TC-2.20: calculateJuzNumber via getPageInfo
    @Test
    fun `getPageInfo calculates correct juz number for page 1`() = runTest {
        val vm = createViewModel()
        vm.loadPage(1)
        val info = vm.getPageInfo()
        assertEquals(1, info.juzNumber)
    }

    // TC-2.21: updateScrollPosition updates state
    @Test
    fun `updateScrollPosition updates state scroll position`() = runTest {
        val vm = createViewModel()
        vm.updateScrollPosition(0.75f)
        assertEquals(0.75f, vm.uiState.value.scrollPosition)
    }

    // TC-2.22: saveReadingPosition delegates to repository
    @Test
    fun `saveReadingPosition calls readingHistoryRepository`() = runTest {
        val vm = createViewModel()
        vm.saveReadingPosition()

        coVerify {
            readingHistoryRepository.updateLastReadPosition(
                mushafType = any(),
                chapterNumber = any(),
                verseNumber = any(),
                pageNumber = any(),
                scrollPosition = any()
            )
        }
    }

    // TC-2.23: recordReadingSession delegates to repository
    @Test
    fun `recordReadingSession calls readingHistoryRepository`() = runTest {
        val vm = createViewModel()
        vm.recordReadingSession(300)

        coVerify {
            readingHistoryRepository.recordReadingSession(
                chapterNumber = any(),
                verseNumber = any(),
                pageNumber = any(),
                durationSeconds = 300,
                mushafType = any()
            )
        }
    }

    // TC-2.24: loadPage exception sets error
    @Test
    fun `loadPage with repository exception sets error state`() = runTest {
        coEvery { verseRepository.getVersesForPage(any(), any()) } throws RuntimeException("DB error")

        val vm = createViewModel()
        vm.loadPage(10)

        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    // TC-2.25: goToChapter with verse not found sets error
    @Test
    fun `goToChapter when verse not found sets error`() = runTest {
        coEvery { verseRepository.getVerse(50, 1) } returns null

        val vm = createViewModel()
        vm.goToChapter(50)

        assertNotNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.contains("Verse not found"))
    }

    // ===== QA-2.3: MushafView Theme & MushafType =====

    // TC-2.30: setMushafType updates state
    @Test
    fun `setMushafType updates mushafType in state`() = runTest {
        val vm = createViewModel()
        vm.setMushafType(MushafType.HAFS_1405)

        assertEquals(MushafType.HAFS_1405, vm.uiState.value.mushafType)
    }

    // TC-2.31: setMushafType persists preference
    @Test
    fun `setMushafType persists type to preferencesRepository`() = runTest {
        val vm = createViewModel()
        vm.setMushafType(MushafType.HAFS_1405)

        coVerify { preferencesRepository.setMushafType(MushafType.HAFS_1405) }
    }

    // TC-2.32: setMushafType reloads current page
    @Test
    fun `setMushafType reloads current page with new type`() = runTest {
        val vm = createViewModel()
        vm.loadPage(5)
        clearMocks(verseRepository, answers = false)
        coEvery { verseRepository.getVersesForPage(5, any()) } returns listOf(testVerse(pageNumber = 5))

        vm.setMushafType(MushafType.HAFS_1405)

        coVerify { verseRepository.getVersesForPage(5, any()) }
    }

    // TC-2.33: default mushafType is HAFS_1441
    @Test
    fun `default mushafType is HAFS_1441`() = runTest {
        val state = MushafUiState()
        assertEquals(MushafType.HAFS_1441, state.mushafType)
    }

    // TC-2.34: setMushafType exception sets error
    @Test
    fun `setMushafType with preference failure sets error`() = runTest {
        coEvery { preferencesRepository.setMushafType(any()) } throws RuntimeException("Write failed")

        val vm = createViewModel()
        vm.setMushafType(MushafType.HAFS_1405)

        assertNotNull(vm.uiState.value.error)
    }

    // TC-2.35: MushafType enum values exist
    @Test
    fun `MushafType has exactly HAFS_1441 and HAFS_1405`() {
        val values = MushafType.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(MushafType.HAFS_1441))
        assertTrue(values.contains(MushafType.HAFS_1405))
    }

    // TC-2.36: loadPreferences restores last read position
    @Test
    fun `loadPreferences restores last read position from repository`() = runTest {
        val lastPos = LastReadPosition(
            mushafType = MushafType.HAFS_1441,
            chapterNumber = 18, verseNumber = 10,
            pageNumber = 293, lastReadAt = System.currentTimeMillis(),
            scrollPosition = 0.5f
        )
        coEvery { readingHistoryRepository.getLastReadPosition(MushafType.HAFS_1441) } returns lastPos
        coEvery { verseRepository.getVersesForPage(293, any()) } returns listOf(testVerse(18, 10, 293))

        val vm = createViewModel()

        assertEquals(293, vm.uiState.value.currentPage)
        assertEquals(18, vm.uiState.value.currentChapter)
        assertEquals(10, vm.uiState.value.currentVerse)
        assertEquals(0.5f, vm.uiState.value.scrollPosition)
    }

    // TC-2.37: goToVerse delegates to goToChapter
    @Test
    fun `goToVerse navigates to specific verse`() = runTest {
        val verse = testVerse(chapterNumber = 36, number = 12, pageNumber = 440)
        coEvery { verseRepository.getVerse(36, 12) } returns verse
        coEvery { verseRepository.getVersesForPage(440, any()) } returns listOf(verse)

        val vm = createViewModel()
        vm.goToVerse(36, 12)

        assertEquals(440, vm.uiState.value.currentPage)
        assertEquals(36, vm.uiState.value.currentChapter)
        assertEquals(12, vm.uiState.value.currentVerse)
    }

    // TC-2.38: goToPage delegates to loadPage
    @Test
    fun `goToPage loads the specified page`() = runTest {
        coEvery { verseRepository.getVersesForPage(300, any()) } returns listOf(testVerse(pageNumber = 300))
        val vm = createViewModel()
        vm.goToPage(300)
        assertEquals(300, vm.uiState.value.currentPage)
    }

    // TC-2.39: MushafUiState defaults
    @Test
    fun `MushafUiState default values are correct`() {
        val state = MushafUiState()
        assertEquals(1, state.currentPage)
        assertEquals(1, state.currentChapter)
        assertEquals(1, state.currentVerse)
        assertEquals(0f, state.scrollPosition)
        assertTrue(state.verses.isEmpty())
        assertTrue(state.chapters.isEmpty())
        assertNull(state.selectedVerse)
        assertTrue(state.selectedVerses.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // TC-2.40: PageInfo progress calculation
    @Test
    fun `PageInfo progress is percentage of 604`() {
        val info = PageInfo(
            pageNumber = 302, totalPages = 604,
            chapterName = "Test", juzNumber = 16, progress = 50
        )
        assertEquals(50, info.progress)
        assertEquals(604, info.totalPages)
    }
}
