package com.mushafimad.app.ui.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mushafimad.ui.search.SearchView

/**
 * The library's SearchView does everything (query box, filters, history,
 * results). The consumer only wires up navigation.
 */
@Composable
fun SearchScreen(
    onOpenPage: (Int) -> Unit,
    onOpenChapter: (Int) -> Unit,
) {
    SearchView(
        onVerseSelected = { verse -> onOpenPage(verse.pageNumber) },
        onChapterSelected = { chapter -> onOpenChapter(chapter.number) },
        onDismiss = null,
        modifier = Modifier.fillMaxSize()
    )
}
