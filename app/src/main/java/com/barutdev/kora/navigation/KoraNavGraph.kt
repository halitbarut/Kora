package com.barutdev.kora.navigation

import android.os.SystemClock
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.barutdev.kora.ui.navigation.KoraScaffoldController
import com.barutdev.kora.ui.navigation.LocalKoraScaffoldController
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.navigation.rememberKoraScaffoldController
import com.barutdev.kora.util.koraStringResource
import java.util.Locale
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
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scaffoldController: KoraScaffoldController = rememberKoraScaffoldController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val preloadViewModel: BottomNavPreloadViewModel = hiltViewModel()
    val bottomNavTransitionState = remember { BottomNavTransitionState() }
    val performanceLogger = remember { NavigationPerformanceLogger() }

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
        performanceLogger.onRouteChanged(currentRoute)
    }

    val bottomBarVisible = shouldShowBottomBar(currentRoute, currentStudentId != null)

    CompositionLocalProvider(LocalKoraScaffoldController provides scaffoldController) {
        Scaffold(
            modifier = modifier,
            topBar = { KoraTopBar(scaffoldController.topBarConfig.value) },
            bottomBar = {
                if (bottomBarVisible && currentStudentId != null) {
                    KoraBottomNavigation(
                        currentRoute = currentRoute,
                        studentId = currentStudentId,
                        onNavigate = { route -> navController.navigateKora(route) }
                    )
                }
            },
            floatingActionButton = {
                val fabConfig = scaffoldController.fabConfig.value
                if (fabConfig != null) {
                    FloatingActionButton(
                        onClick = fabConfig.onClick,
                        containerColor = fabConfig.containerColor
                            ?: MaterialTheme.colorScheme.primary,
                        contentColor = fabConfig.contentColor
                            ?: MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = fabConfig.icon,
                            contentDescription = fabConfig.contentDescription
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = scaffoldController.snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = KoraDestination.StudentList.route,
                modifier = Modifier.padding(innerPadding),
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
                        onNavigateToSettings = {
                            navController.navigate(KoraDestination.Settings.route) {
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
    }
}

private const val SLIDE_DURATION_MS = 240
private const val FADE_DURATION_MS = 180

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun KoraTopBar(config: TopBarConfig?) {
    if (config == null) return
    TopAppBar(
        title = { Text(text = config.title) },
        navigationIcon = {
            val navigation = config.navigationIcon
            if (navigation != null) {
                IconButton(onClick = navigation.onClick) {
                    Icon(
                        imageVector = navigation.icon,
                        contentDescription = navigation.contentDescription
                    )
                }
            }
        },
        actions = {
            config.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
        }
    )
}

@Composable
private fun KoraBottomNavigation(
    currentRoute: String?,
    studentId: Int,
    onNavigate: (String) -> Unit
) {
    val currentRouteKey = remember(currentRoute) { currentRoute.toRouteKey() }
    val items = remember(studentId) {
        listOf(
            BottomNavItem(
                route = KoraDestination.Dashboard.createRoute(studentId),
                routeKey = KoraDestination.Dashboard.routeKey(),
                icon = Icons.Outlined.Dashboard,
                labelRes = R.string.dashboard_title
            ),
            BottomNavItem(
                route = KoraDestination.Calendar.createRoute(studentId),
                routeKey = KoraDestination.Calendar.routeKey(),
                icon = Icons.Outlined.CalendarMonth,
                labelRes = R.string.calendar_title
            ),
            BottomNavItem(
                route = KoraDestination.Homework.createRoute(studentId),
                routeKey = KoraDestination.Homework.routeKey(),
                icon = Icons.Outlined.Assignment,
                labelRes = R.string.homework_title
            )
        )
    }

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRouteKey == item.routeKey
            val label = koraStringResource(id = item.labelRes)
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = label
                    )
                },
                label = { Text(text = label) }
            )
        }
    }
}

@VisibleForTesting
internal fun shouldShowBottomBar(route: String?, hasStudentId: Boolean): Boolean {
    if (!hasStudentId) return false
    val routeKey = route.toRouteKey()
    return bottomNavRouteKeySet.contains(routeKey)
}

private class NavigationPerformanceLogger {
    private var lastTimestampNanos: Long = SystemClock.elapsedRealtimeNanos()
    private var lastRoute: String? = null

    fun onRouteChanged(route: String?) {
        if (route == lastRoute) return
        val now = SystemClock.elapsedRealtimeNanos()
        val deltaMs = (now - lastTimestampNanos) / 1_000_000.0
        val formatted = String.format(Locale.US, "%.1f", deltaMs)
        Log.d("KoraNavigation", "Route change to $route took $formatted ms")
        lastRoute = route
        lastTimestampNanos = now
    }
}

private data class BottomNavItem(
    val route: String,
    val routeKey: RouteKey,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

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
