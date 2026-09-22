package com.dailycompany.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.dailycompany.feature.events.EventsRoute
import com.dailycompany.feature.outfits.OutfitsRoute
import com.dailycompany.feature.settings.SettingsRoute
import com.dailycompany.feature.tasks.TaskEditRoute
import com.dailycompany.feature.tasks.TasksRoute
import com.dailycompany.feature.today.TodayRoute
import com.dailycompany.feature.wardrobe.WardrobeRoute
import com.dailycompany.ui.theme.strings

object Routes {
    const val TODAY = "today"
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit?taskId={taskId}"
    const val EVENTS = "events"
    const val WARDROBE = "wardrobe"
    const val OUTFITS = "outfits"
    const val SETTINGS = "settings"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topLevelRoutes = setOf(
    Routes.TODAY,
    Routes.TASKS,
    Routes.EVENTS,
    Routes.WARDROBE,
    Routes.OUTFITS,
)

@Composable
fun DailyCompanyApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevel = currentDestination?.route in topLevelRoutes

    val s = strings()
    val items = listOf(
        BottomNavItem(Routes.TODAY, s.navToday, Icons.Outlined.WbSunny),
        BottomNavItem(Routes.TASKS, s.navTasks, Icons.Outlined.CheckCircleOutline),
        BottomNavItem(Routes.EVENTS, s.navEvents, Icons.Outlined.CalendarMonth),
        BottomNavItem(Routes.WARDROBE, s.navWardrobe, Icons.Outlined.Checkroom),
        BottomNavItem(Routes.OUTFITS, s.navOutfits, Icons.Outlined.AutoAwesome),
    )

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(text = item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.TODAY) {
                TodayRoute(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.TASKS) {
                TasksRoute(
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onCreateTask = { navController.navigate("task_edit") },
                    onOpenTask = { taskId -> navController.navigate("task_edit?taskId=$taskId") },
                )
            }
            composable(
                route = Routes.TASK_EDIT,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) {
                TaskEditRoute(onBack = { navController.navigateUp() })
            }
            composable(Routes.EVENTS) {
                EventsRoute(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.WARDROBE) {
                WardrobeRoute(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.OUTFITS) {
                OutfitsRoute(onNavigateToSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.SETTINGS) {
                SettingsRoute(onBack = { navController.navigateUp() })
            }
        }
    }
}