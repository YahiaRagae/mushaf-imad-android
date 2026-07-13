package com.mushafimad.app.ui.home

import com.mushafimad.app.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.core.domain.models.Chapter
import com.mushafimad.core.utils.getRevelationInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChapterClick: (Int) -> Unit,
    onListenClick: (Int) -> Unit,
    onResumeClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("Quran Reader")
                    Text(
                        "${state.chapters.size} surahs - powered by mushaf-ui ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    "Library error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    state.lastRead?.let { pos ->
                        item {
                            ElevatedCard(
                                onClick = onResumeClick,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Continue reading", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "${state.lastReadChapterName ?: "Surah ${pos.chapterNumber}"} " +
                                                "- verse ${pos.verseNumber} - page ${pos.pageNumber}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                }
                            }
                        }
                    }

                    items(state.chapters, key = { it.number }) { chapter ->
                        ChapterRow(
                            chapter = chapter,
                            onClick = { onChapterClick(chapter.number) },
                            onListen = { onListenClick(chapter.number) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: Chapter, onClick: () -> Unit, onListen: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("${chapter.number}", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(chapter.englishTitle, fontWeight = FontWeight.SemiBold)
            Text(
                chapter.getRevelationInfo("en"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(chapter.arabicTitle, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onListen) {
            Icon(Icons.Default.Headphones, contentDescription = "Listen to ${chapter.englishTitle}")
        }
    }
}
