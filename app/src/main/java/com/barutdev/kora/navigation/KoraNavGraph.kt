package com.barutdev.kora.navigation

import android.os.SystemClock
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.barutdev.kora.R
import com.barutdev.kora.ui.navigation.BottomNavPreloadViewModel
import com.barutdev.kora.ui.navigation.KoraScaffoldController
import com.barutdev.kora.ui.navigation.LocalKoraScaffoldController
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.navigation.rememberKoraScaffoldController
import com.barutdev.kora.ui.screens.calendar.CalendarScreen
import com.barutdev.kora.ui.screens.dashboard.DashboardScreen
import com.barutdev.kora.ui.screens.homework.HomeworkScreen
import com.barutdev.kora.ui.screens.reports.ReportsScreen
import com.barutdev.kora.ui.screens.settings.SettingsScreen
import com.barutdev.kora.ui.screens.onboarding.OnboardingScreen
import com.barutdev.kora.ui.screens.student_list.StudentListScreen
import com.barutdev.kora.ui.screens.student_profile.AddStudentProfileScreen
import com.barutdev.kora.ui.screens.student_profile.EditStudentProfileScreen
import com.barutdev.kora.util.koraStringResource
import java.util.Locale

private const val SLIDE_DURATION_MS = 240
private const val FADE_DURATION_MS = 180
private const val NAVIGATION_LOG_TAG = "KoraNavigation"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KoraNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scaffoldController: KoraScaffoldController = rememberKoraScaffoldController()
) {
    val startDestinationViewModel: StartDestinationViewModel = hiltViewModel()
    val startRoute: String? by startDestinationViewModel.startRoute.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentStudentId = navBackStackEntry.studentIdOrNull()
    val preloadViewModel: BottomNavPreloadViewModel = hiltViewModel()
    val bottomNavTransitionState = remember { BottomNavTransitionState() }
    val performanceLogger = remember { NavigationPerformanceLogger() }
    val bottomBarState = remember { BottomBarState() }

    LaunchedEffect(currentStudentId) {
        if (currentStudentId != null) {
            preloadViewModel.prime(currentStudentId)
        }
    }

    LaunchedEffect(currentDestination) {
        if (!bottomNavTransitionState.isPrimed && currentDestination.isBottomBarDestination()) {
            bottomNavTransitionState.markPrimedAfterFirstFrames()
        }
        performanceLogger.onRouteChanged(currentDestination?.route)
    }

    LaunchedEffect(currentDestination, currentStudentId) {
        val studentScoped = currentDestination.asStudentScopedDestination()
        val studentId = currentStudentId
        if (studentScoped != null && studentId != null) {
            bottomBarState.record(studentScoped, studentId)
        }
    }

    val bottomBarVisible = shouldShowBottomBar(currentDestination)

    val onNavigateToStudentList = remember(navController) {
        {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != KoraDestination.StudentList.route) {
                navController.navigate(KoraDestination.StudentList.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
    val onNavigateToSettings = remember(navController) {
        {
            navController.navigate(KoraDestination.Settings.route) {
                launchSingleTop = true
            }
        }
    }

    val topBarConfig by scaffoldController.topBarConfig
    val navigationContentDescription = koraStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val settingsContentDescription = koraStringResource(
        id = R.string.dashboard_settings_icon_description
    )
    val studentScopedDestination = currentDestination.asStudentScopedDestination()
    val fallbackTitleRes = when {
        studentScopedDestination != null -> studentScopedDestination.labelRes
        currentDestination?.route == KoraDestination.Settings.route -> KoraDestination.Settings.labelRes
        currentDestination?.route == KoraDestination.StudentList.route -> KoraDestination.StudentList.labelRes
        else -> R.string.app_name
    }
    val fallbackTitle = koraStringResource(id = fallbackTitleRes)
    val defaultNavigationAction = remember(
        studentScopedDestination,
        currentStudentId,
        navigationContentDescription,
        onNavigateToStudentList
    ) {
        if (studentScopedDestination != null && currentStudentId != null) {
            TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigationContentDescription,
                onClick = onNavigateToStudentList
            )
        } else {
            null
        }
    }
    val defaultSettingsAction = remember(
        settingsContentDescription,
        onNavigateToSettings
    ) {
        TopBarAction(
            icon = Icons.Filled.Settings,
            contentDescription = settingsContentDescription,
            onClick = onNavigateToSettings
        )
    }
    val topBarState by remember(
        topBarConfig,
        fallbackTitle,
        defaultNavigationAction,
        defaultSettingsAction
    ) {
        derivedStateOf {
            val actions = topBarConfig?.actions
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(defaultSettingsAction)
            KoraTopBarState(
                title = topBarConfig?.title ?: fallbackTitle,
                navigationIcon = topBarConfig?.navigationIcon ?: defaultNavigationAction,
                actions = actions
            )
        }
    }

    CompositionLocalProvider(LocalKoraScaffoldController provides scaffoldController) {
        Scaffold(
            modifier = modifier,
            topBar = { KoraTopBar(state = topBarState) },
            bottomBar = {
                if (bottomBarVisible) {
                    KoraBottomNavigation(
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            when (destination) {
                                is KoraDestination.StudentScoped -> {
                                    val targetStudentId = currentStudentId
                                        ?: bottomBarState.lastStudentId(destination)
                                        ?: bottomBarState.lastKnownStudentId()
                                    if (targetStudentId != null) {
                                        navController.navigateToStudentScoped(
                                            destination = destination,
                                            studentId = targetStudentId,
                                            bottomBarState = bottomBarState
                                        )
                                    } else {
                                        Log.w(
                                            NAVIGATION_LOG_TAG,
                                            "Skipping navigation to ${destination.baseRoute} because no student context is available"
                                        )
                                    }
                                }
                                KoraDestination.Reports -> {
                                    navController.navigateToReports()
                                }
                                else -> Unit
                            }
                        }
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
            if (startRoute == null) {
                Box(
                    modifier = Modifier.padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                NavHost(
                    navController = navController,
                    startDestination = startRoute!!,
                    modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    if (bottomNavTransitionState.shouldAnimate(
                            initialState.destination,
                            targetState.destination
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
                            initialState.destination,
                            targetState.destination
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
                            initialState.destination,
                            targetState.destination
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
                            initialState.destination,
                            targetState.destination
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
                composable(
                    route = KoraDestination.Onboarding.route
                ) {
                    OnboardingScreen(
                        onCompleted = {
                            navController.navigate(KoraDestination.StudentList.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = KoraDestination.StudentList.route
                ) {
                    StudentListScreen(
                        onAddStudent = {
                            navController.navigate(KoraDestination.AddStudentProfile.route)
                        },
                        onStudentClick = { studentId ->
                            studentId.toIntOrNull()?.let { id ->
                                navController.navigateToStudentScoped(
                                    destination = KoraDestination.Dashboard,
                                    studentId = id,
                                    bottomBarState = bottomBarState
                                )
                            }
                        },
                        onEditStudentProfile = { studentId ->
                            navController.navigate(
                                KoraDestination.EditStudentProfile.createRoute(studentId)
                            )
                        }
                    )
                }

                composable(
                    route = KoraDestination.AddStudentProfile.route
                ) {
                    AddStudentProfileScreen(
                        onBack = { navController.popBackStack() },
                        onProfileSaved = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = KoraDestination.Dashboard.route,
                    arguments = KoraDestination.Dashboard.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Dashboard entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    key("dashboard-$studentId") {
                        DashboardScreen(
                            expectedStudentId = studentId,
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
                            },
                            onEditStudentProfile = { id ->
                                navController.navigate(
                                    KoraDestination.EditStudentProfile.createRoute(id)
                                )
                            }
                        )
                    }
                }

                composable(
                    route = KoraDestination.Calendar.route,
                    arguments = KoraDestination.Calendar.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Calendar entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    key("calendar-$studentId") {
                        CalendarScreen(
                            expectedStudentId = studentId,
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
                }

                composable(
                    route = KoraDestination.Homework.route,
                    arguments = KoraDestination.Homework.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Homework entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    key("homework-$studentId") {
                        HomeworkScreen(
                            expectedStudentId = studentId,
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
                }

                composable(
                    route = KoraDestination.Reports.route
                ) { backStackEntry ->
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Reports entry=${backStackEntry.id} (global)"
                    )
                    ReportsScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                composable(
                    route = KoraDestination.EditStudentProfile.route,
                    arguments = KoraDestination.EditStudentProfile.arguments()
                ) {
                    EditStudentProfileScreen(
                        onBack = { navController.popBackStack() },
                        onProfileSaved = { navController.popBackStack() }
                    )
                }

                composable(
                    route = KoraDestination.Settings.route
                ) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}

private data class KoraTopBarState(
    val title: String,
    val navigationIcon: TopBarAction?,
    val actions: List<TopBarAction>
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun KoraTopBar(state: KoraTopBarState) {
    LaunchedEffect(state.title) {
        Log.d(NAVIGATION_LOG_TAG, "KoraTopBar visible with title=${state.title}")
    }
    TopAppBar(
        title = { Text(text = state.title) },
        navigationIcon = {
            val navigation = state.navigationIcon
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
            state.actions.forEach { action ->
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
    currentDestination: NavDestination?,
    onNavigate: (KoraDestination) -> Unit
) {
    NavigationBar {
        KoraDestination.bottomBarDestinations.forEach { destination ->
            val isSelected = currentDestination.isDestination(destination)
            val label = koraStringResource(id = destination.labelRes)
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigate(destination)
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon(),
                        contentDescription = label
                    )
                },
                label = { Text(text = label) }
            )
        }
    }
}

@VisibleForTesting
internal fun shouldShowBottomBar(
    currentDestination: NavDestination?
): Boolean = currentDestination.isBottomBarDestination()

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
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = LinearOutSlowInEasing
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = LinearOutSlowInEasing
            )
        )
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
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutLinearInEasing
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutLinearInEasing
            )
        )
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
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = LinearOutSlowInEasing
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = LinearOutSlowInEasing
            )
        )
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
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutLinearInEasing
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutLinearInEasing
            )
        )
    }
}

@VisibleForTesting
internal fun resolveSlideDirection(
    initialRoute: String?,
    targetRoute: String?
): AnimatedContentTransitionScope.SlideDirection? {
    val initial = KoraDestination.studentScopedFromRoute(initialRoute)
    val target = KoraDestination.studentScopedFromRoute(targetRoute)
    if (initial == null || target == null || initial == target) return null
    val initialIndex = KoraDestination.bottomBarDestinations.indexOf(initial)
    val targetIndex = KoraDestination.bottomBarDestinations.indexOf(target)
    if (initialIndex == -1 || targetIndex == -1) return null
    return if (targetIndex > initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun NavDestination?.isBottomBarDestination(): Boolean {
    val route = this?.route ?: return false
    if (this.asStudentScopedDestination() != null) return true
    return route == KoraDestination.Reports.route
}

private fun NavDestination?.asStudentScopedDestination(): KoraDestination.StudentScoped? =
    KoraDestination.studentScopedFromRoute(this?.route)

private fun NavDestination?.isDestination(destination: KoraDestination): Boolean {
    val currentRoute = this?.route ?: return false
    if (currentRoute == destination.route) return true
    return when (destination) {
        is KoraDestination.StudentScoped -> currentRoute.startsWith(destination.baseRoute)
        else -> false
    }
}

private fun KoraDestination.icon(): ImageVector = when (this) {
    KoraDestination.Dashboard -> Icons.Outlined.Dashboard
    KoraDestination.Calendar -> Icons.Outlined.CalendarMonth
    KoraDestination.Homework -> Icons.Outlined.Assignment
    KoraDestination.Reports -> Icons.Outlined.BarChart
    KoraDestination.EditStudentProfile -> Icons.Outlined.Dashboard
    else -> Icons.Outlined.Dashboard
}

private fun NavBackStackEntry?.studentIdOrNull(): Int? {
    if (this == null) return null
    return arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG)
}

private fun NavBackStackEntry.requireStudentId(): Int {
    val resolvedId = checkNotNull(arguments?.getInt(STUDENT_ID_ARG)) {
        "Destination ${destination.route} requires $STUDENT_ID_ARG argument"
    }
    Log.d(
        NAVIGATION_LOG_TAG,
        "BackStackEntry id=$id route=${destination.route} resolvedStudentId=$resolvedId"
    )
    return resolvedId
}

private fun NavHostController.navigateToStudentScoped(
    destination: KoraDestination.StudentScoped,
    studentId: Int,
    bottomBarState: BottomBarState
) {
    if (isCurrentBottomBarDestination(destination, studentId)) {
        return
    }
    val shouldRestoreState = bottomBarState.shouldRestore(destination, studentId)
    if (!shouldRestoreState) {
        bottomBarState.clear(destination)
    }

    val route = destination.createRoute(studentId)
    val recordedStudentId = bottomBarState.lastStudentId(destination)
    val shouldLaunchSingleTop = recordedStudentId == studentId && shouldRestoreState
    Log.d(
        NAVIGATION_LOG_TAG,
        "navigateToStudentScoped route=$route restore=$shouldRestoreState " +
            "singleTop=$shouldLaunchSingleTop recorded=$recordedStudentId"
    )

    navigate(route) {
        launchSingleTop = shouldLaunchSingleTop
        restoreState = false
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
    }
}

private fun NavHostController.navigateToReports() {
    if (currentDestination?.route == KoraDestination.Reports.route) return
    Log.d(NAVIGATION_LOG_TAG, "navigateToReports route=${KoraDestination.Reports.route}")
    navigate(KoraDestination.Reports.route) {
        launchSingleTop = true
        restoreState = false
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
    }
}

private fun NavHostController.isCurrentBottomBarDestination(
    destination: KoraDestination.StudentScoped,
    studentId: Int
): Boolean {
    val currentEntry = currentBackStackEntry ?: return false
    if (!currentEntry.destination.isDestination(destination)) return false
    return currentEntry.arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG) == studentId
}

private class NavigationPerformanceLogger {
    private var lastTimestampNanos: Long = SystemClock.elapsedRealtimeNanos()
    private var lastRoute: String? = null

    fun onRouteChanged(route: String?) {
        if (route == lastRoute) return
        val now = SystemClock.elapsedRealtimeNanos()
        val deltaMs = (now - lastTimestampNanos) / 1_000_000.0
        val formatted = String.format(Locale.US, "%.1f", deltaMs)
        Log.d(NAVIGATION_LOG_TAG, "Route change to $route took $formatted ms")
        lastRoute = route
        lastTimestampNanos = now
    }
}
