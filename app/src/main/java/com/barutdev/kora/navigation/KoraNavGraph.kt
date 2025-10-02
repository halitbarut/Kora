package com.barutdev.kora.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barutdev.kora.R
import com.barutdev.kora.ui.screens.calendar.CalendarScreen
import com.barutdev.kora.ui.screens.dashboard.DashboardScreen
import com.barutdev.kora.ui.screens.homework.HomeworkScreen
import com.barutdev.kora.ui.screens.settings.SettingsScreen
import com.barutdev.kora.ui.screens.student_list.StudentListScreen

const val STUDENT_ID_ARG = "studentId"

sealed class KoraDestination(val route: String, @StringRes val labelRes: Int) {
    data object StudentList : KoraDestination("student_list", R.string.student_list_title)
    data object Dashboard : KoraDestination("dashboard/{$STUDENT_ID_ARG}", R.string.dashboard_title) {
        fun createRoute(studentId: Int) = "dashboard/$studentId"
    }
    data object Calendar : KoraDestination("calendar/{$STUDENT_ID_ARG}", R.string.calendar_title) {
        fun createRoute(studentId: Int) = "calendar/$studentId"
    }
    data object Homework : KoraDestination("homework/{$STUDENT_ID_ARG}", R.string.homework_title) {
        fun createRoute(studentId: Int) = "homework/$studentId"
    }
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
                onAddStudent = {},
                onStudentClick = { studentId ->
                    studentId.toIntOrNull()?.let { id ->
                        navController.navigate(
                            KoraDestination.Dashboard.createRoute(id)
                        )
                    }
                }
            )
        }
        composable(
            route = KoraDestination.Dashboard.route,
            arguments = listOf(
                navArgument(STUDENT_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) {
            DashboardScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(KoraDestination.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = KoraDestination.Calendar.route,
            arguments = listOf(
                navArgument(STUDENT_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) {
            CalendarScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = KoraDestination.Homework.route,
            arguments = listOf(
                navArgument(STUDENT_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt(STUDENT_ID_ARG)
                ?: return@composable
            HomeworkScreen(studentId = studentId)
        }
        composable(route = KoraDestination.Settings.route) {
            SettingsScreen()
        }
    }
}
