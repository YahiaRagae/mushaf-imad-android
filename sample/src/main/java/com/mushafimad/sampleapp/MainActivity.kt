package com.mushafimad.sampleapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mushafimad.core.domain.models.MushafType
import com.mushafimad.ui.mushaf.MushafView
import com.mushafimad.ui.mushaf.MushafWithPlayerView
import com.mushafimad.ui.search.SearchView
import com.mushafimad.ui.theme.ColorSchemeType
import com.mushafimad.ui.theme.ReadingTheme

/**
 * Sample app demonstrating MushafView and Audio Player integration
 * Structured like iOS example app with categories
 */
class MainActivity : ComponentActivity() {

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - background audio notifications will work
        } else {
            // Permission denied - background audio will still work but without notification controls
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+ (required for playback controls)
        requestNotificationPermissionIfNeeded()

        setContent {
            MaterialTheme {
                SampleAppContent()
            }
        }
    }

    /**
     * Request POST_NOTIFICATIONS permission for Android 13+ (API 33+)
     * This is required to show playback controls notification
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

// Navigation routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MushafReader : Screen("mushaf_reader")
    object MushafWithAudio : Screen("mushaf_with_audio")
    object ChaptersList : Screen("chapters_list")
    object SearchDemo : Screen("search_demo")
    object ThemeCustomization : Screen("theme_customization")
    object AudioPlayerDemo : Screen("audio_player_demo")
}

@Composable
fun SampleAppContent() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.MushafReader.route) {
            MushafReaderScreen(navController = navController)
        }

        composable(Screen.MushafWithAudio.route) {
            MushafWithAudioScreen(navController = navController)
        }

        composable(Screen.ChaptersList.route) {
            ChaptersListScreen(navController = navController)
        }

        composable(Screen.SearchDemo.route) {
            SearchDemoScreen(navController = navController)
        }

        composable(Screen.ThemeCustomization.route) {
            ThemeCustomizationScreen(navController = navController)
        }

        composable(Screen.AudioPlayerDemo.route) {
            AudioPlayerDemoScreen(navController = navController)
        }
    }
}

// ============================================================================
// MARK: - Home Screen (Category List)
// ============================================================================

data class DemoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    val quickStartItems = listOf(
        DemoItem(
            title = "Chapters List",
            subtitle = "Browse all 114 chapters",
            icon = Icons.Default.List,
            route = Screen.ChaptersList.route
        ),
        DemoItem(
            title = "Read the Mushaf",
            subtitle = "Basic Mushaf reader",
            icon = Icons.Default.Home,
            route = Screen.MushafReader.route
        )
    )

    val features = listOf(
        DemoItem(
            title = "Search",
            subtitle = "Search verses and chapters",
            icon = Icons.Default.Search,
            route = Screen.SearchDemo.route
        ),
        DemoItem(
            title = "Theme Customization",
            subtitle = "Reading themes and color schemes",
            icon = Icons.Default.Settings,
            route = Screen.ThemeCustomization.route
        )
    )

    val audioItems = listOf(
        DemoItem(
            title = "Mushaf with Audio",
            subtitle = "Integrated audio player with verse highlighting",
            icon = Icons.Default.PlayArrow,
            route = Screen.MushafWithAudio.route
        ),
        DemoItem(
            title = "Audio Player UI",
            subtitle = "Standalone audio player demo",
            icon = Icons.Default.Star,
            route = Screen.AudioPlayerDemo.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MushafImad Examples") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Quick Start section
            item {
                SectionHeader("Quick Start")
            }
            items(quickStartItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }

            // Features section
            item {
                SectionHeader("Features")
            }
            items(features) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }

            // Audio section
            item {
                SectionHeader("Audio")
            }
            items(audioItems) { item ->
                DemoListItem(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }

            // Helpful Links section
            item {
                SectionHeader("Helpful Links")
            }
            item {
                LinkItem(
                    title = "View on GitHub",
                    subtitle = "Source code and documentation",
                    icon = Icons.Default.Info,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/YahiaRagae/mushaf-imad-android"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DemoListItem(
    item: DemoItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.title) },
        supportingContent = { Text(item.subtitle) },
        leadingContent = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun LinkItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open link",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// ============================================================================
// MARK: - Demo Screens
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafReaderScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mushaf Reader") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafView(
                readingTheme = ReadingTheme.COMFORTABLE,
                colorScheme = ColorSchemeType.DEFAULT,
                mushafType = MushafType.HAFS_1441,
                initialPage = 1,
                showNavigationControls = true,
                showPageInfo = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MushafWithAudioScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mushaf with Audio") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafWithPlayerView(
                readingTheme = ReadingTheme.COMFORTABLE,
                colorScheme = ColorSchemeType.DEFAULT,
                mushafType = MushafType.HAFS_1441,
                initialPage = 1,
                showNavigationControls = true,
                showPageInfo = true,
                showAudioPlayer = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersListScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chapters") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Chapters list coming soon...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDemoScreen(navController: NavHostController) {
    var currentPage by remember { mutableStateOf<Int?>(null) }

    if (currentPage != null) {
        // Show Mushaf at the selected page
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Page $currentPage") },
                    navigationIcon = {
                        IconButton(onClick = { currentPage = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to search")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                MushafView(
                    readingTheme = ReadingTheme.COMFORTABLE,
                    colorScheme = ColorSchemeType.DEFAULT,
                    mushafType = MushafType.HAFS_1441,
                    initialPage = currentPage,
                    showNavigationControls = true,
                    showPageInfo = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        // Show search
        SearchView(
            onVerseSelected = { verse ->
                currentPage = verse.pageNumber
            },
            onChapterSelected = { chapter ->
                println("Chapter selected: ${chapter.number}")
            },
            onDismiss = {
                navController.popBackStack()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationScreen(navController: NavHostController) {
    var selectedTheme by remember { mutableStateOf(ReadingTheme.COMFORTABLE) }
    var selectedColorScheme by remember { mutableStateOf(ColorSchemeType.DEFAULT) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Customization") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafView(
                readingTheme = selectedTheme,
                colorScheme = selectedColorScheme,
                mushafType = MushafType.HAFS_1441,
                initialPage = 1,
                showNavigationControls = true,
                showPageInfo = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false }
            ) {
                ThemeSettingsSheet(
                    selectedReadingTheme = selectedTheme,
                    selectedColorScheme = selectedColorScheme,
                    onReadingThemeChange = { selectedTheme = it },
                    onColorSchemeChange = { selectedColorScheme = it },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerDemoScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player Demo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            MushafWithPlayerView(
                readingTheme = ReadingTheme.COMFORTABLE,
                colorScheme = ColorSchemeType.DEFAULT,
                mushafType = MushafType.HAFS_1441,
                initialPage = 2,
                showNavigationControls = true,
                showPageInfo = true,
                showAudioPlayer = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ============================================================================
// MARK: - Settings Sheet
// ============================================================================

@Composable
fun ThemeSettingsSheet(
    selectedReadingTheme: ReadingTheme,
    selectedColorScheme: ColorSchemeType,
    onReadingThemeChange: (ReadingTheme) -> Unit,
    onColorSchemeChange: (ColorSchemeType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Theme Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Reading Theme Selection
        Text(
            text = "Reading Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ReadingTheme.entries.forEach { theme ->
            FilterChip(
                selected = theme == selectedReadingTheme,
                onClick = { onReadingThemeChange(theme) },
                label = { Text(theme.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Scheme Selection
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ColorSchemeType.entries.forEach { scheme ->
            FilterChip(
                selected = scheme == selectedColorScheme,
                onClick = { onColorSchemeChange(scheme) },
                label = { Text(scheme.name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
