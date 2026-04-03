package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mushafimad.core.MushafLibrary
import com.mushafimad.sampleapp.Util.General.formatDuration
import com.mushafimad.sampleapp.component.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryDataScreen(navController: NavHostController) {
    val historyRepository = remember { MushafLibrary.getReadingHistoryRepository() }
    var totalReadingTime by remember { mutableStateOf(0L) }
    var currentStreak by remember { mutableStateOf(0) }
    var readChapters by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        totalReadingTime = historyRepository.getTotalReadingTime()
        currentStreak = historyRepository.getCurrentStreak()
        readChapters = historyRepository.getReadChapters()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReadingHistoryRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
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
                        text = "Reading Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.Companion.padding(bottom = 16.dp)
                    )
                }

                item {
                    StatCard(
                        title = "getTotalReadingTime()",
                        value = formatDuration(totalReadingTime),
                        subtitle = "Total time spent reading"
                    )
                }

                item {
                    StatCard(
                        title = "getCurrentStreak()",
                        value = "$currentStreak days",
                        subtitle = "Consecutive days with reading activity"
                    )
                }

                item {
                    StatCard(
                        title = "getReadChapters()",
                        value = "${readChapters.size} / 114",
                        subtitle = "Chapters read: ${
                            if (readChapters.isEmpty()) "None yet" else readChapters.take(
                                10
                            ).joinToString(", ") + if (readChapters.size > 10) "..." else ""
                        }"
                    )
                }
            }
        }
    }
}