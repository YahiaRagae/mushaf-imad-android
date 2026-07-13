package com.mushafimad.app.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.app.ui.bookmarks.asDate
import com.mushafimad.core.MushafLibrary
import com.mushafimad.core.domain.models.ReadingHistory
import com.mushafimad.core.domain.models.ReadingStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<ReadingHistory> = emptyList(),
    val stats: ReadingStats? = null,
)

class HistoryViewModel : ViewModel() {
    private val repository = MushafLibrary.getReadingHistoryRepository()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        // The mushafType-persistence bug (recordReadingSession() dropping it,
        // getRecentHistory() then throwing on MushafType.valueOf("")) is fixed
        // as of library 0.2.2. The app calls the library straight and is
        // allowed to crash if the library regresses - that is the library's
        // job to keep working, not this app's job to mask.
        _uiState.value = HistoryUiState(
            entries = repository.getRecentHistory(limit = 100),
            stats = repository.getReadingStats()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenPage: (Int) -> Unit,
    viewModel: HistoryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Reading history") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(Modifier.fillMaxSize()) {
                state.stats?.let { stats ->
                    item {
                        Card(Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Stat("${stats.totalReadingTimeMinutes} min", "Read time")
                                Stat("${stats.totalPagesRead}", "Pages")
                                Stat("${stats.totalChaptersRead}", "Surahs")
                                Stat("${stats.currentStreak}", "Streak")
                            }
                        }
                    }
                }

                if (state.entries.isEmpty()) {
                    item {
                        Text(
                            "No reading sessions recorded yet.\nOpen the reader and stay on a page for a few seconds.",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(state.entries, key = { it.id }) { entry ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPage(entry.pageNumber) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text("Page ${entry.pageNumber} - verse ${entry.verseReference}", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${entry.timestamp.asDate()} - ${entry.durationSeconds}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
