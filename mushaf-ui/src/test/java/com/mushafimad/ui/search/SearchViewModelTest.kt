package com.mushafimad.ui.search

import com.google.common.truth.Truth.assertThat
import com.mushafimad.core.domain.models.SearchType
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.core.domain.repository.BookmarkRepository
import com.mushafimad.core.domain.repository.ChapterRepository
import com.mushafimad.core.domain.repository.SearchHistoryRepository
import com.mushafimad.core.domain.repository.VerseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for the QA-reported "search callbacks not working" bug:
 * rapid typing launched overlapping uncancelled queries, letting a stale slow
 * result overwrite a newer one and churning the list mid-tap.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val verseRepository: VerseRepository = mockk(relaxed = true)
    private val chapterRepository: ChapterRepository = mockk(relaxed = true)
    private val bookmarkRepository: BookmarkRepository = mockk(relaxed = true)
    private val searchHistoryRepository: SearchHistoryRepository = mockk(relaxed = true)

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = SearchViewModel(
            verseRepository = verseRepository,
            chapterRepository = chapterRepository,
            bookmarkRepository = bookmarkRepository,
            searchHistoryRepository = searchHistoryRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun verse(id: String) = Verse(
        verseID = id.hashCode(),
        humanReadableID = id,
        number = 1,
        text = id,
        textWithoutTashkil = id,
        uthmanicHafsText = id,
        hafsSmartText = id,
        searchableText = id,
        chapterNumber = 1,
        pageNumber = 1,
        partNumber = 1,
        hizbNumber = 1
    )

    @Test
    fun rapidTyping_onlyTheLastQueryExecutes() = runTest(dispatcher.scheduler) {
        coEvery { verseRepository.searchVerses(any()) } returns emptyList()

        viewModel.search("a", SearchType.VERSE)
        advanceTimeBy(100) // below the 300 ms debounce
        viewModel.search("ab", SearchType.VERSE)
        advanceUntilIdle()

        coVerify(exactly = 0) { verseRepository.searchVerses("a") }
        coVerify(exactly = 1) { verseRepository.searchVerses("ab") }
    }

    @Test
    fun staleSlowQuery_neverOverwritesNewerResults() = runTest(dispatcher.scheduler) {
        val staleVerse = verse("stale")
        val freshVerse = verse("fresh")
        coEvery { verseRepository.searchVerses("slow") } coAnswers {
            delay(5_000)
            listOf(staleVerse)
        }
        coEvery { verseRepository.searchVerses("fast") } returns listOf(freshVerse)

        viewModel.search("slow", SearchType.VERSE)
        advanceTimeBy(400) // past debounce: the slow query is now in flight
        viewModel.search("fast", SearchType.VERSE)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.results.verseResults).containsExactly(freshVerse)
        assertThat(viewModel.uiState.value.isSearching).isFalse()
    }

    @Test
    fun historyIsRecorded_onlyForExecutedQueries() = runTest(dispatcher.scheduler) {
        coEvery { verseRepository.searchVerses(any()) } returns emptyList()

        viewModel.search("a", SearchType.VERSE)
        advanceTimeBy(100)
        viewModel.search("ab", SearchType.VERSE)
        advanceTimeBy(100)
        viewModel.search("abc", SearchType.VERSE)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchHistoryRepository.recordSearch("a", any(), any()) }
        coVerify(exactly = 0) { searchHistoryRepository.recordSearch("ab", any(), any()) }
        coVerify(exactly = 1) { searchHistoryRepository.recordSearch("abc", any(), any()) }
    }

    @Test
    fun blankQuery_cancelsInFlightSearch_andClearsState() = runTest(dispatcher.scheduler) {
        coEvery { verseRepository.searchVerses("abc") } coAnswers {
            delay(5_000)
            listOf(verse("late"))
        }

        viewModel.search("abc", SearchType.VERSE)
        advanceTimeBy(400) // in flight
        viewModel.search("", SearchType.VERSE)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEmpty()
        assertThat(viewModel.uiState.value.results.isEmpty).isTrue()
        assertThat(viewModel.uiState.value.hasSearched).isFalse()
    }

    @Test
    fun failedSearch_surfacesError_withoutCrashing() = runTest(dispatcher.scheduler) {
        coEvery { verseRepository.searchVerses("boom") } throws IllegalStateException("db closed")

        viewModel.search("boom", SearchType.VERSE)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isEqualTo("db closed")
        assertThat(viewModel.uiState.value.isSearching).isFalse()
    }
}
