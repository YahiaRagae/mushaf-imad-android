package com.mushafimad.app.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mushafimad.app.ui.bookmarks.BookmarksScreen
import com.mushafimad.app.ui.history.HistoryScreen
import com.mushafimad.app.ui.home.HomeScreen
import com.mushafimad.app.ui.reader.ReaderScreen
import com.mushafimad.app.ui.search.SearchScreen
import com.mushafimad.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    /**
     * chapter = -1  -> open at the last read position (initialPage = null)
     * page    = -1  -> derive the page from the chapter
     */
    const val READER = "reader?chapter={chapter}&page={page}&player={player}"

    fun reader(chapter: Int = -1, page: Int = -1, player: Boolean = false) =
        "reader?chapter=$chapter&page=$page&player=$player"

    fun resume() = reader()
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Surahs", Icons.AutoMirrored.Filled.MenuBook),
    Tab(Routes.SEARCH, "Search", Icons.Default.Search),
    Tab(Routes.BOOKMARKS, "Bookmarks", Icons.Default.Bookmarks),
    Tab(Routes.HISTORY, "History", Icons.Default.History),
    Tab(Routes.SETTINGS, "Settings", Icons.Default.Settings),
)

@Composable
fun QuranApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onChapterClick = { chapter ->
                            navController.navigate(Routes.reader(chapter = chapter))
                        },
                        onListenClick = { chapter ->
                            navController.navigate(Routes.reader(chapter = chapter, player = true))
                        },
                        onResumeClick = { navController.navigate(Routes.resume()) }
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        onOpenPage = { page ->
                            navController.navigate(Routes.reader(page = page))
                        },
                        onOpenChapter = { chapter ->
                            navController.navigate(Routes.reader(chapter = chapter))
                        }
                    )
                }
                composable(Routes.BOOKMARKS) {
                    BookmarksScreen(
                        onOpenPage = { page -> navController.navigate(Routes.reader(page = page)) }
                    )
                }
                composable(Routes.HISTORY) {
                    HistoryScreen(
                        onOpenPage = { page -> navController.navigate(Routes.reader(page = page)) }
                    )
                }
                composable(Routes.SETTINGS) { SettingsScreen() }

                composable(
                    route = Routes.READER,
                    arguments = listOf(
                        navArgument("chapter") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("page") { type = NavType.IntType; defaultValue = -1 },
                        navArgument("player") { type = NavType.BoolType; defaultValue = false },
                    )
                ) { entry ->
                    ReaderScreen(
                        chapterNumber = entry.arguments?.getInt("chapter") ?: -1,
                        requestedPage = entry.arguments?.getInt("page") ?: -1,
                        startWithPlayer = entry.arguments?.getBoolean("player") ?: false,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
