package com.barutdev.kora.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barutdev.kora.R
import com.barutdev.kora.ui.screens.calendar.CalendarScreen
import com.barutdev.kora.ui.screens.dashboard.DashboardScreen
import com.barutdev.kora.ui.screens.homework.HomeworkScreen
import com.barutdev.kora.ui.screens.settings.SettingsScreen
import com.barutdev.kora.ui.screens.student_list.StudentListScreen

sealed class KoraDestination(val route: String, @StringRes val labelRes: Int) {
    data object StudentList : KoraDestination("student_list", R.string.student_list_title)
    data object Dashboard : KoraDestination("dashboard", R.string.dashboard_title)
    data object Calendar : KoraDestination("calendar", R.string.calendar_title)
    data object Homework : KoraDestination("homework", R.string.homework_title)
    data object Settings : KoraDestination("settings", R.string.settings_title)
}

@Composable
fun KoraNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = KoraDestination.StudentList.route,
        modifier = modifier
    ) {
        composable(route = KoraDestination.StudentList.route) {
            StudentListScreen(
                onAddStudent = {}
            )
        }
        composable(route = KoraDestination.Dashboard.route) {
            DashboardScreen(
                currentRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(KoraDestination.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(route = KoraDestination.Calendar.route) {
            CalendarScreen()
        }
        composable(route = KoraDestination.Homework.route) {
            HomeworkScreen()
        }
        composable(route = KoraDestination.Settings.route) {
            SettingsScreen()
        }
    }
}
