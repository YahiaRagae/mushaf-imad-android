package com.mushafimad.app.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mushafimad.app.ui.AppSettings
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.mushaf.MushafWithPlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterNumber: Int,
    requestedPage: Int,
    startWithPlayer: Boolean,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mushafType by viewModel.mushafType.collectAsStateWithLifecycle()
    val readingTheme by AppSettings.readingTheme.collectAsStateWithLifecycle()
    val colorScheme by AppSettings.colorScheme.collectAsStateWithLifecycle()

    var withPlayer by rememberSaveable { mutableStateOf(startWithPlayer) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(chapterNumber, requestedPage) {
        viewModel.resolveStart(chapterNumber, requestedPage)
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.title + (state.currentPage?.let { " - p.$it" } ?: ""),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { withPlayer = !withPlayer }) {
                        Icon(
                            if (withPlayer) Icons.Default.HeadsetOff else Icons.Default.Headphones,
                            contentDescription = if (withPlayer) "Hide player" else "Show player"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val start = state.startPage) {
                StartPage.Resolving -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is StartPage.Ready -> if (withPlayer) {
                    // Reader + audio. The player's reciter chip opens the library's
                    // ReciterPickerDialog internally.
                    MushafWithPlayerView(
                        readingTheme = readingTheme,
                        colorScheme = colorScheme,
                        mushafType = mushafType,
                        initialPage = start.page,
                        showNavigationControls = true,
                        showPageInfo = true,
                        showAudioPlayer = true,
                        onVerseSelected = viewModel::onVerseTapped,
                        onVerseLongPress = viewModel::toggleBookmark,
                        onPageChanged = viewModel::onPageChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MushafView(
                        readingTheme = readingTheme,
                        colorScheme = colorScheme,
                        mushafType = mushafType,
                        initialPage = start.page,
                        showNavigationControls = true,
                        showPageInfo = true,
                        onVerseSelected = viewModel::onVerseTapped,
                        onVerseLongPress = viewModel::toggleBookmark,
                        onPageChanged = viewModel::onPageChanged,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
