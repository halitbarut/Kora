package com.barutdev.kora.navigation

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.barutdev.kora.R
import com.barutdev.kora.ui.screens.calendar.CalendarScreen
import com.barutdev.kora.ui.screens.dashboard.DashboardScreen
import com.barutdev.kora.ui.screens.homework.HomeworkScreen
import com.barutdev.kora.ui.screens.settings.SettingsScreen
import com.barutdev.kora.ui.screens.student_list.StudentListScreen
import com.barutdev.kora.ui.navigation.BottomNavPreloadViewModel
import kotlin.jvm.JvmInline

const val STUDENT_ID_ARG = "studentId"

@JvmInline
internal value class RouteKey(val value: String) {
    val isKnown: Boolean get() = value.isNotEmpty()

    companion object {
        val Unknown = RouteKey("")
    }
}

internal fun String?.toRouteKey(): RouteKey {
    if (this.isNullOrBlank()) return RouteKey.Unknown
    val sanitized = this
        .substringBefore("?")
        .substringBefore("#")
    val baseSegment = sanitized.substringBefore("/")
    val normalized = baseSegment.substringBefore("{")
    return RouteKey(normalized)
}

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

internal fun KoraDestination.routeKey(): RouteKey = route.toRouteKey()

internal val bottomNavRouteKeys: List<RouteKey> = listOf(
    RouteKey("dashboard"),
    RouteKey("calendar"),
    RouteKey("homework")
)
internal val bottomNavRouteKeySet: Set<RouteKey> = bottomNavRouteKeys.toSet()

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KoraNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val preloadViewModel: BottomNavPreloadViewModel = hiltViewModel()
    val bottomNavTransitionState = remember { BottomNavTransitionState() }

    val currentStudentId = navBackStackEntry
        ?.arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG)

    LaunchedEffect(currentStudentId) {
        if (currentStudentId != null) {
            preloadViewModel.prime(currentStudentId)
        }
    }

    LaunchedEffect(currentRoute) {
        if (!bottomNavTransitionState.isPrimed) {
            val currentKey = currentRoute.toRouteKey()
            if (bottomNavRouteKeySet.contains(currentKey)) {
                bottomNavTransitionState.markPrimedAfterFirstFrames()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = KoraDestination.StudentList.route,
        modifier = modifier,
        enterTransition = {
            if (bottomNavTransitionState.shouldAnimate(
                    initialState.destination.route,
                    targetState.destination.route
                )
            ) {
                koraEnterTransition()
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = FADE_DURATION_MS,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        },
        exitTransition = {
            if (bottomNavTransitionState.shouldAnimate(
                    initialState.destination.route,
                    targetState.destination.route
                )
            ) {
                koraExitTransition()
            } else {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = FADE_DURATION_MS,
                        easing = FastOutLinearInEasing
                    )
                )
            }
        },
        popEnterTransition = {
            if (bottomNavTransitionState.shouldAnimate(
                    initialState.destination.route,
                    targetState.destination.route
                )
            ) {
                koraPopEnterTransition()
            } else {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = FADE_DURATION_MS,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        },
        popExitTransition = {
            if (bottomNavTransitionState.shouldAnimate(
                    initialState.destination.route,
                    targetState.destination.route
                )
            ) {
                koraPopExitTransition()
            } else {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = FADE_DURATION_MS,
                        easing = FastOutLinearInEasing
                    )
                )
            }
        }
    ) {
        composable(route = KoraDestination.StudentList.route) {
            StudentListScreen(
                onAddStudent = {},
                onStudentClick = { studentId ->
                    studentId.toIntOrNull()?.let { id ->
                        navController.navigateKora(
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
                    navController.navigateKora(route)
                },
                onNavigateToSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                },
                onNavigateToStudentList = {
                    navController.navigate(KoraDestination.StudentList.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
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
                    navController.navigateKora(route)
                },
                onNavigateToStudentList = {
                    navController.navigate(KoraDestination.StudentList.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
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
        ) {
            HomeworkScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigateKora(route)
                },
                onNavigateToStudentList = {
                    navController.navigate(KoraDestination.StudentList.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(route = KoraDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private const val SLIDE_DURATION_MS = 240
private const val FADE_DURATION_MS = 180

private fun AnimatedContentTransitionScope<NavBackStackEntry>.koraEnterTransition(): EnterTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideInHorizontally(
            animationSpec = tween(durationMillis = SLIDE_DURATION_MS, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth ->
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> fullWidth
                    AnimatedContentTransitionScope.SlideDirection.Right -> -fullWidth
                    else -> fullWidth
                }
            }
        ) + fadeIn(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = LinearOutSlowInEasing))
    } else {
        fadeIn(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = LinearOutSlowInEasing))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.koraExitTransition(): ExitTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideOutHorizontally(
            animationSpec = tween(durationMillis = SLIDE_DURATION_MS, easing = FastOutLinearInEasing),
            targetOffsetX = { fullWidth ->
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> -fullWidth
                    AnimatedContentTransitionScope.SlideDirection.Right -> fullWidth
                    else -> -fullWidth
                }
            }
        ) + fadeOut(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = FastOutLinearInEasing))
    } else {
        fadeOut(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = FastOutLinearInEasing))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.koraPopEnterTransition(): EnterTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideInHorizontally(
            animationSpec = tween(durationMillis = SLIDE_DURATION_MS, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth ->
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> fullWidth
                    AnimatedContentTransitionScope.SlideDirection.Right -> -fullWidth
                    else -> fullWidth
                }
            }
        ) + fadeIn(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = LinearOutSlowInEasing))
    } else {
        fadeIn(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = LinearOutSlowInEasing))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.koraPopExitTransition(): ExitTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideOutHorizontally(
            animationSpec = tween(durationMillis = SLIDE_DURATION_MS, easing = FastOutLinearInEasing),
            targetOffsetX = { fullWidth ->
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> -fullWidth
                    AnimatedContentTransitionScope.SlideDirection.Right -> fullWidth
                    else -> -fullWidth
                }
            }
        ) + fadeOut(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = FastOutLinearInEasing))
    } else {
        fadeOut(animationSpec = tween(durationMillis = FADE_DURATION_MS, easing = FastOutLinearInEasing))
    }
}

@VisibleForTesting
internal fun resolveSlideDirection(
    initialRoute: String?,
    targetRoute: String?
): AnimatedContentTransitionScope.SlideDirection? {
    val initialKey = initialRoute.toRouteKey()
    val targetKey = targetRoute.toRouteKey()
    val initialIndex = bottomNavRouteKeys.indexOf(initialKey)
    val targetIndex = bottomNavRouteKeys.indexOf(targetKey)
    if (initialIndex == -1 || targetIndex == -1 || initialIndex == targetIndex) {
        return null
    }
    return if (targetIndex > initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun NavHostController.navigateKora(route: String) {
    val targetKey = route.toRouteKey()
    val isBottomNavDestination = routeMatchesBottomNav(targetKey)
    if (isBottomNavDestination && shouldSkipBottomNavNavigation(targetKey, route)) {
        return
    }
    navigate(route) {
        launchSingleTop = true
        if (isBottomNavDestination) {
            restoreState = true
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
}

@VisibleForTesting
internal fun routeMatchesBottomNav(route: String?): Boolean {
    return routeMatchesBottomNav(route.toRouteKey())
}

@VisibleForTesting
internal fun routeMatchesDestination(route: String?, destination: KoraDestination): Boolean {
    return routeMatchesDestination(route.toRouteKey(), destination)
}

internal fun routeMatchesDestination(routeKey: RouteKey, destination: KoraDestination): Boolean {
    if (!routeKey.isKnown) return false
    return routeKey == destination.routeKey()
}

private fun routeMatchesBottomNav(routeKey: RouteKey): Boolean {
    if (!routeKey.isKnown) return false
    return bottomNavRouteKeySet.contains(routeKey)
}

private fun NavHostController.shouldSkipBottomNavNavigation(
    targetKey: RouteKey,
    targetRoute: String
): Boolean {
    val currentEntry = currentBackStackEntry ?: return false
    val currentKey = currentEntry.destination.route.toRouteKey()
    if (currentKey != targetKey) return false

    val targetStudentId = targetRoute.extractStudentId()
    val currentStudentId = currentEntry.arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG)

    return targetStudentId != null && currentStudentId != null && currentStudentId == targetStudentId
}

private fun String.extractStudentId(): Int? {
    val sanitized = substringBefore("?").substringBefore("#")
    val segments = sanitized.split("/")
    if (segments.size < 2) return null
    return segments[1].toIntOrNull()
}
