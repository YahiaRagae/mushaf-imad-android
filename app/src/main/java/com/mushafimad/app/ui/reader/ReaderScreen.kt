package com.mushafimad.app.ui.reader

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    // `immersive` toggles ONLY the floating in-app top bar (below). It does NOT touch the
    // system bars: those stay hidden the whole time the reader is open (see the effect
    // below), so revealing the toolbar never resizes or pushes the page.
    var immersive by remember { mutableStateOf(true) }

    // Keep the system status/navigation bars hidden for the entire time the reader is on
    // screen, so the page uses the full display and never reflows. Transient-by-swipe
    // behaviour means the user can still peek the bars with an edge swipe, and because a
    // transient bar is drawn as an overlay it does not consume insets - the page stays put.
    // The bars are restored when leaving the reader, so the rest of the app is never left
    // in full-screen mode.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            val w = view.context.findActivity()?.window
            if (w != null) {
                WindowCompat.getInsetsController(w, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

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
        // The parent QuranApp Scaffold already insets this screen for the status and
        // navigation bars, so this inner Scaffold must NOT inset again - doing so doubled
        // the status-bar gap at the top of the page. Consume no insets here.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            // The reader fills the area the parent already inset for us. Its position never
            // depends on whether the app bar is showing: the bar floats over the reader
            // (see below) instead of pushing it down, so the page never jumps on toggle.
            Box(Modifier.fillMaxSize()) {
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
                            showNavigationControls = false,
                            showAudioPlayer = true,
                            onVerseSelected = viewModel::onVerseTapped,
                            onVerseLongPress = viewModel::toggleBookmark,
                            onPageTap = { immersive = !immersive },
                            onPageChanged = viewModel::onPageChanged,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MushafView(
                            readingTheme = readingTheme,
                            colorScheme = colorScheme,
                            mushafType = mushafType,
                            initialPage = start.page,
                            showNavigationControls = false,
                            onVerseSelected = viewModel::onVerseTapped,
                            onVerseLongPress = viewModel::toggleBookmark,
                            onPageTap = { immersive = !immersive },
                            onPageChanged = viewModel::onPageChanged,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // App bar OVERLAYS the top edge, drawn on top of the reader's own header.
            // Showing/hiding it animates the bar in and out without moving the page
            // underneath, matching the reader UX where the chrome floats over the page.
            AnimatedVisibility(
                visible = !immersive,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                TopAppBar(
                    // Already positioned below the status bar by the parent inset, so the
                    // bar itself adds no top inset (otherwise it floats with a gap above it).
                    windowInsets = WindowInsets(0, 0, 0, 0),
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
        }
    }
}

/** Walk up the Context wrappers to the hosting Activity (whose window we toggle). */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
