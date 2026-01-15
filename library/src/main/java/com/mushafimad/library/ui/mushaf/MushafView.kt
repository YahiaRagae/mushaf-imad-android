package com.mushafimad.library.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mushafimad.library.domain.models.MushafType
import com.mushafimad.library.domain.models.Verse
import com.mushafimad.library.ui.theme.*
import kotlinx.coroutines.delay

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
 * @param viewModel Optional ViewModel (injected by default)
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
    onVerseSelected: ((Verse) -> Unit)? = null,
    onPageChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MushafViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Apply mushaf type if different
    LaunchedEffect(mushafType) {
        if (uiState.mushafType != mushafType) {
            viewModel.setMushafType(mushafType)
        }
    }

    // Load initial page if provided
    LaunchedEffect(initialPage) {
        initialPage?.let { page ->
            if (page != uiState.currentPage) {
                viewModel.goToPage(page)
            }
        }
    }

    // Notify page changes
    LaunchedEffect(uiState.currentPage) {
        onPageChanged?.invoke(uiState.currentPage)
    }

    // Auto-save reading position periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // Save every 30 seconds
            viewModel.saveReadingPosition()
        }
    }

    // Save on disposal
    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveReadingPosition()
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
                uiState.isLoading -> {
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
                    var swipeOffset by remember { mutableStateOf(0f) }

                    QuranPageView(
                        verses = uiState.verses,
                        chapters = uiState.chapters,
                        pageNumber = uiState.currentPage,
                        juzNumber = viewModel.getPageInfo().juzNumber,
                        mushafType = uiState.mushafType,
                        selectedVerse = uiState.selectedVerse,
                        highlightedVerse = highlightedVerse,
                        onVerseClick = { verse ->
                            viewModel.selectVerse(verse)
                            onVerseSelected?.invoke(verse)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        when {
                                            swipeOffset > 100 -> viewModel.previousPage()
                                            swipeOffset < -100 -> viewModel.nextPage()
                                        }
                                        swipeOffset = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        swipeOffset += dragAmount
                                    }
                                )
                            }
                    )

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
