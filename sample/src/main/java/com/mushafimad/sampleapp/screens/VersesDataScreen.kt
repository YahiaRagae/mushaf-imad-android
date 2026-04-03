package com.mushafimad.sampleapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.mushafimad.core.domain.models.Verse
import com.mushafimad.sampleapp.component.VerseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersesDataScreen(navController: NavHostController) {
    val verseRepository = remember { MushafLibrary.getVerseRepository() }
    var verses by remember { mutableStateOf<List<Verse>>(emptyList()) }
    var selectedPage by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedPage) {
        isLoading = true
        verses = verseRepository.getVersesForPage(selectedPage)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VerseRepository") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Page selector
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                IconButton(
                    onClick = { if (selectedPage > 1) selectedPage-- },
                    enabled = selectedPage > 1
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                }
                Text(
                    text = "Page $selectedPage / 604",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { if (selectedPage < 604) selectedPage++ },
                    enabled = selectedPage < 604
                ) {
                    Icon(Icons.Default.ArrowForward, "Next")
                }
            }

            HorizontalDivider()

            if (isLoading) {
                Box(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "getVersesForPage($selectedPage) - ${verses.size} verses",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.Companion.padding(bottom = 8.dp)
                        )
                    }
                    items(verses) { verse ->
                        VerseItem(verse)
                    }
                }
            }
        }
    }
}