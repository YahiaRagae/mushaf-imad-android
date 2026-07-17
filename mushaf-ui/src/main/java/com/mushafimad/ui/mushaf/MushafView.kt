package com.mushafimad.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mushafimad.ui.internal.mushafViewModel
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.core.domain.models.Verse
import androidx.compose.runtime.saveable.rememberSaveable
import com.mushafimad.ui.theme.*

/**
 * MushafView - Main composable for displaying Quran pages using images
 *
 * Matches iOS implementation using line images instead of text rendering
 * Images are loaded from assets/quran-images/{page}/{line}.png
 *
 * Public API for the library. Displays Quran pages with proper Arabic layout,
 * reading themes, and navigation controls.
 *
 * Usage:
 * ```
 * MushafView(
 *     readingTheme = ReadingTheme.COMFORTABLE,
 *     colorScheme = ColorSchemeType.DEFAULT,
 *     mushafType = MushafType.HAFS_1441,
 *     onVerseSelected = { verse -> /* handle selection */ }
 * )
 * ```
 *
 * @param readingTheme The reading theme (background/text colors)
 * @param colorScheme The color scheme for UI elements
 * @param mushafType The Mushaf layout type
 * @param initialPage Initial page to display (default: last read position)
 * @param highlightedVerse Verse to highlight (e.g., during audio playback)
 * @param showNavigationControls Show next/previous page buttons
 * @param showPageInfo Show page/juz information
 * @param onVerseSelected Callback when a verse is selected
 * @param onPageChanged Callback when page changes
 * @param modifier Optional modifier
 */
@Composable
fun MushafView(
    readingTheme: ReadingTheme = ReadingTheme.COMFORTABLE,
    colorScheme: ColorSchemeType = ColorSchemeType.DEFAULT,
    mushafType: MushafType = MushafType.HAFS_1441,
    initialPage: Int? = null,
    highlightedVerse: Verse? = null,
    showNavigationControls: Boolean = true,
    showPageInfo: Boolean = true,
    pageSwipeEnabled: Boolean = true,
    onVerseSelected: ((Verse) -> Unit)? = null,
    onVerseLongPress: ((Verse) -> Unit)? = null,
    onPageChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MushafViewModel = mushafViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Apply mushaf type if different
    LaunchedEffect(mushafType) {
        if (uiState.mushafType != mushafType) {
            viewModel.setMushafType(mushafType)
        }
    }

    // Apply a consumer-provided initial page exactly once per composition
    // lifetime, so a static value doesn't keep forcing the page back after
    // the user navigates away. initialPage = null means: restore the last
    // read position.
    var initialPageApplied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(initialPage) {
        if (!initialPageApplied) {
            initialPageApplied = true
            initialPage?.let { viewModel.goToPage(it) }
        }
    }

    // Notify page changes
    LaunchedEffect(uiState.currentPage) {
        onPageChanged?.invoke(uiState.currentPage)
    }

    // Save on disposal (page changes already persist via the ViewModel's
    // debounced save), and close out the reading session for the page the
    // reader was last on.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveReadingPosition()
            viewModel.flushReadingSession()
        }
    }

    MushafTheme(
        readingTheme = readingTheme,
        colorScheme = colorScheme
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(readingTheme.backgroundColor)
        ) {
            when {
                uiState.isLoading && uiState.verses.isEmpty() -> {
                    LoadingView()
                }

                uiState.error != null -> {
                    ErrorView(
                        error = uiState.error ?: "",
                        onRetry = { viewModel.loadPage(uiState.currentPage) },
                        onDismiss = { viewModel.clearError() }
                    )
                }

                uiState.verses.isNotEmpty() -> {
                    val pagerState = rememberPagerState(
                        initialPage = uiState.currentPage - 1
                    ) { TOTAL_PAGES }

                    // Programmatic navigation (buttons, goToPage/goToChapter)
                    // animates the pager to the new page
                    LaunchedEffect(uiState.currentPage) {
                        val target = uiState.currentPage - 1
                        if (pagerState.currentPage != target && !pagerState.isScrollInProgress) {
                            pagerState.animateScrollToPage(target)
                        }
                    }

                    // Commit user swipes once the pager settles on a page
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.settledPage }
                            .collect { index -> viewModel.onPageSettled(index + 1) }
                    }

                    HorizontalPager(
                        state = pagerState,
                        // Pre-compose neighbours so their content is already
                        // loaded when the swipe starts (issue #70)
                        beyondViewportPageCount = 1,
                        // Let a host lock page-turning by swipe (the nav buttons and
                        // programmatic navigation still work). Default keeps swiping on.
                        userScrollEnabled = pageSwipeEnabled,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        MushafPagerPage(
                            pageNumber = index + 1,
                            mushafType = uiState.mushafType,
                            selectedVerse = uiState.selectedVerse,
                            highlightedVerse = highlightedVerse,
                            onVerseClick = { verse ->
                                viewModel.selectVerse(verse)
                                onVerseSelected?.invoke(verse)
                            },
                            onVerseLongClick = onVerseLongPress,
                            viewModel = viewModel
                        )
                    }

                    // Navigation controls overlay
                    if (showNavigationControls) {
                        NavigationControls(
                            canGoPrevious = uiState.currentPage > 1,
                            canGoNext = uiState.currentPage < 604,
                            onPrevious = { viewModel.previousPage() },
                            onNext = { viewModel.nextPage() },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    // Page info overlay
                    if (showPageInfo) {
                        PageInfoDisplay(
                            pageInfo = viewModel.getPageInfo(),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        )
                    }
                }

                else -> {
                    EmptyView(
                        onLoadPage = { viewModel.loadPage(1) }
                    )
                }
            }
        }
    }
}

/**
 * One page inside the pager. Renders from the ViewModel's page cache,
 * loading on first composition; pre-composed neighbours load before they
 * become visible, which is what makes swiping feel instant.
 */
@Composable
private fun MushafPagerPage(
    pageNumber: Int,
    mushafType: MushafType,
    selectedVerse: Verse?,
    highlightedVerse: Verse?,
    onVerseClick: (Verse) -> Unit,
    onVerseLongClick: ((Verse) -> Unit)? = null,
    viewModel: MushafViewModel,
    modifier: Modifier = Modifier
) {
    var content by remember(pageNumber, mushafType) {
        mutableStateOf(viewModel.peekPageContent(pageNumber))
    }

    LaunchedEffect(pageNumber, mushafType) {
        if (content == null) {
            content = viewModel.pageContent(pageNumber)
        }
    }

    val pageContent = content
    if (pageContent != null) {
        QuranPageView(
            verses = pageContent.verses,
            chapters = pageContent.chapters,
            pageNumber = pageNumber,
            juzNumber = ((pageNumber - 1) / 20) + 1,
            mushafType = mushafType,
            selectedVerse = selectedVerse,
            highlightedVerse = highlightedVerse,
            onVerseClick = onVerseClick,
            onVerseLongClick = onVerseLongClick,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * LoadingView - Shows loading indicator
 */
@Composable
private fun LoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "جاري التحميل...",
                style = MushafTypography.body,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ErrorView - Shows error message with retry option
 */
@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "حدث خطأ",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إغلاق")
                    }

                    Button(onClick = onRetry) {
                        Text("إعادة المحاولة")
                    }
                }
            }
        }
    }
}

/**
 * EmptyView - Shows when no verses are loaded
 */
@Composable
private fun EmptyView(
    onLoadPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "لا توجد صفحات",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onLoadPage) {
                Text("تحميل الصفحة الأولى")
            }
        }
    }
}

/**
 * NavigationControls - Page navigation buttons
 */
@Composable
private fun NavigationControls(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous page button (right in RTL)
            if (canGoPrevious) {
                FloatingActionButton(
                    onClick = onPrevious,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "الصفحة السابقة"
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(56.dp))
            }

            // Next page button (left in RTL)
            if (canGoNext) {
                FloatingActionButton(
                    onClick = onNext,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "الصفحة التالية"
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    }
}

/**
 * PageInfoDisplay - Shows current page information
 */
@Composable
private fun PageInfoDisplay(
    pageInfo: PageInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${pageInfo.pageNumber} / ${pageInfo.totalPages}",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.End
            )

            if (pageInfo.chapterName.isNotEmpty()) {
                Text(
                    text = pageInfo.chapterName,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
