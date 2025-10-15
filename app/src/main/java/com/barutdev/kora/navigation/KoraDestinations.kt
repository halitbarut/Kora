package com.barutdev.kora.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.barutdev.kora.R

const val STUDENT_ID_ARG = "studentId"

internal sealed class KoraDestination(
    val route: String,
    @StringRes val labelRes: Int
) {

    object StudentList : KoraDestination(
        route = "student_list",
        labelRes = R.string.student_list_title
    )

    sealed class StudentScoped(
        internal val baseRoute: String,
        @StringRes labelRes: Int
    ) : KoraDestination(
        route = "$baseRoute/{$STUDENT_ID_ARG}",
        labelRes = labelRes
    ) {
        fun createRoute(studentId: Int): String = "$baseRoute/$studentId"

        fun arguments() = listOf(
            navArgument(STUDENT_ID_ARG) {
                type = NavType.IntType
            }
        )
    }

    object Dashboard : StudentScoped(
        baseRoute = "dashboard",
        labelRes = R.string.dashboard_title
    )

    object Calendar : StudentScoped(
        baseRoute = "calendar",
        labelRes = R.string.calendar_title
    )

    object Homework : StudentScoped(
        baseRoute = "homework",
        labelRes = R.string.homework_title
    )

    object Settings : KoraDestination(
        route = "settings",
        labelRes = R.string.settings_title
    )

    companion object {
        val bottomBarDestinations: List<StudentScoped> by lazy(LazyThreadSafetyMode.PUBLICATION) {
            listOf(Dashboard, Calendar, Homework)
        }

        fun fromRoute(route: String?): KoraDestination? = when (route) {
            StudentList.route -> StudentList
            Dashboard.route -> Dashboard
            Calendar.route -> Calendar
            Homework.route -> Homework
            Settings.route -> Settings
            else -> null
        }

        fun studentScopedFromRoute(route: String?): StudentScoped? {
            if (route == null) return null
            return when {
                route == Dashboard.route || route.startsWith("${Dashboard.baseRoute}/") -> Dashboard
                route == Calendar.route || route.startsWith("${Calendar.baseRoute}/") -> Calendar
                route == Homework.route || route.startsWith("${Homework.baseRoute}/") -> Homework
                else -> null
            }
        }
    }
}
