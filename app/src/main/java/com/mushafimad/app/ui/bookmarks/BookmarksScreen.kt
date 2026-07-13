package com.mushafimad.app.ui.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Bookmark
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookmarksViewModel : ViewModel() {
    private val repository = MushafLibrary.getBookmarkRepository()

    // getAllBookmarksFlow() is reactive - deleting updates the list with no manual refresh.
    val bookmarks: StateFlow<List<Bookmark>> = repository.getAllBookmarksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: String) = viewModelScope.launch { repository.deleteBookmark(id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onOpenPage: (Int) -> Unit,
    viewModel: BookmarksViewModel = viewModel(),
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Bookmarks (${bookmarks.size})") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (bookmarks.isEmpty()) {
                Text(
                    "No bookmarks yet.\nTap a verse in the reader to bookmark it.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPage(bookmark.pageNumber) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(bookmark.verseReference, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Page ${bookmark.pageNumber} - ${bookmark.createdAt.asDate()}" +
                                        if (bookmark.hasNote) " - ${bookmark.note}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.delete(bookmark.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete bookmark")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

internal fun Long.asDate(): String =
    SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(this))
