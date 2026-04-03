package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.Bookmark
import com.mushafimad.sampleapp.component.BookmarkItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksDataScreen(navController: NavHostController) {
    val bookmarkRepository = remember { MushafLibrary.getBookmarkRepository() }
    var bookmarks by remember { mutableStateOf<List<Bookmark>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        bookmarks = bookmarkRepository.getAllBookmarks()
        isLoading = false
    }

    fun refreshBookmarks() {
        coroutineScope.launch {
            bookmarks = bookmarkRepository.getAllBookmarks()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BookmarkRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        // Add a sample bookmark
                        val randomChapter = (1..114).random()
                        val randomVerse = (1..7).random()
                        bookmarkRepository.addBookmark(
                            chapterNumber = randomChapter,
                            verseNumber = randomVerse,
                            pageNumber = 1,
                            note = "Sample bookmark"
                        )
                        refreshBookmarks()
                    }
                }
            ) {
                Icon(Icons.Default.Add, "Add Bookmark")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.Companion.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Companion.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "getAllBookmarks() - ${bookmarks.size} bookmarks",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.Companion.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Tap + to add a random bookmark",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.Companion.padding(bottom = 8.dp)
                    )
                }
                if (bookmarks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "No bookmarks yet. Tap + to add one.",
                                modifier = Modifier.Companion.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(bookmarks) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onDelete = {
                                coroutineScope.launch {
                                    bookmarkRepository.deleteBookmark(bookmark.id)
                                    refreshBookmarks()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}