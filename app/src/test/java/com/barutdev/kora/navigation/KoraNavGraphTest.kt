package com.barutdev.kora.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KoraNavGraphTest {

    @Test
    fun resolveSlideDirection_returnsLeft_whenNavigatingForwardBetweenDashboardAndCalendar() {
        val direction = resolveSlideDirection(
            initialRoute = KoraDestination.Dashboard.route,
            targetRoute = KoraDestination.Calendar.route
        )

        assertEquals(
            AnimatedContentTransitionScope.SlideDirection.Left,
            direction
        )
    }

    @Test
    fun resolveSlideDirection_returnsLeft_forConcreteParameterizedRoutes() {
        val direction = resolveSlideDirection(
            initialRoute = "dashboard/12",
            targetRoute = "homework/12"
        )

        assertEquals(
            AnimatedContentTransitionScope.SlideDirection.Left,
            direction
        )
    }

    @Test
    fun resolveSlideDirection_returnsRight_whenNavigatingBackwardBetweenHomeworkAndDashboard() {
        val direction = resolveSlideDirection(
            initialRoute = KoraDestination.Homework.route,
            targetRoute = KoraDestination.Calendar.route
        )

        assertEquals(
            AnimatedContentTransitionScope.SlideDirection.Right,
            direction
        )
    }

    @Test
    fun resolveSlideDirection_returnsNull_forRoutesOutsideBottomNavigation() {
        val direction = resolveSlideDirection(
            initialRoute = KoraDestination.StudentList.route,
            targetRoute = KoraDestination.Settings.route
        )

        assertNull(direction)
    }

    @Test
    fun routeMatchesBottomNav_acceptsParameterizedRoutes() {
        assertTrue(routeMatchesBottomNav("calendar/42"))
        assertTrue(routeMatchesBottomNav("homework/0"))
        assertTrue(routeMatchesBottomNav("dashboard/100"))
    }

    @Test
    fun routeMatchesBottomNav_rejectsOtherDestinations() {
        assertFalse(routeMatchesBottomNav("student_list"))
        assertFalse(routeMatchesBottomNav("settings"))
        assertFalse(routeMatchesBottomNav(null))
    }

    @Test
    fun routeMatchesDestination_handlesParameterizedRoutes() {
        assertTrue(routeMatchesDestination("dashboard/7", KoraDestination.Dashboard))
        assertTrue(routeMatchesDestination("calendar/3", KoraDestination.Calendar))
        assertTrue(routeMatchesDestination("homework/12", KoraDestination.Homework))
    }

    @Test
    fun routeMatchesDestination_acceptsRouteKeyOverload() {
        val calendarKey = "calendar/42?tab=week".toRouteKey()

        assertTrue(routeMatchesDestination(calendarKey, KoraDestination.Calendar))
        assertFalse(routeMatchesDestination(calendarKey, KoraDestination.Homework))
    }

    @Test
    fun routeMatchesDestination_returnsFalseForDifferentScreens() {
        assertFalse(routeMatchesDestination("calendar/1", KoraDestination.Dashboard))
        assertFalse(routeMatchesDestination("settings", KoraDestination.StudentList))
    }

    @Test
    fun toRouteKey_normalizesQueryAndFragments() {
        val routeKey = "dashboard/77?tab=summary#top".toRouteKey()
        assertEquals(RouteKey("dashboard"), routeKey)
    }


    @Test
    fun shouldShowBottomBar_returnsTrue_forBottomNavRouteWithStudent() {
        assertTrue(shouldShowBottomBar(route = "dashboard/1", hasStudentId = true))
    }

    @Test
    fun shouldShowBottomBar_returnsFalse_whenStudentMissing() {
        assertFalse(shouldShowBottomBar(route = "homework/2", hasStudentId = false))
    }

    @Test
    fun shouldShowBottomBar_returnsFalse_forNonBottomNavRoute() {
        assertFalse(shouldShowBottomBar(route = KoraDestination.StudentList.route, hasStudentId = true))
    }

    @Test
    fun toRouteKey_returnsUnknownForNullOrBlank() {
        assertEquals(RouteKey.Unknown, null.toRouteKey())
        assertEquals(RouteKey.Unknown, "".toRouteKey())
    }
}
