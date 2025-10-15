package com.barutdev.kora.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KoraDestinationTest {

    @Test
    fun createRoute_buildsParameterizedRoute() {
        assertEquals("dashboard/42", KoraDestination.Dashboard.createRoute(42))
        assertEquals("calendar/7", KoraDestination.Calendar.createRoute(7))
        assertEquals("homework/3", KoraDestination.Homework.createRoute(3))
    }

    @Test
    fun studentScopedFromRoute_resolvesKnownRoute() {
        assertEquals(
            KoraDestination.Calendar,
            KoraDestination.studentScopedFromRoute(KoraDestination.Calendar.route)
        )
    }

    @Test
    fun studentScopedFromRoute_returnsNull_forUnknownRoute() {
        assertNull(KoraDestination.studentScopedFromRoute(KoraDestination.StudentList.route))
    }

    @Test
    fun bottomBarDestinations_preserveOrder() {
        val items = KoraDestination.bottomBarDestinations
        assertEquals(0, items.indexOf(KoraDestination.Dashboard))
        assertEquals(1, items.indexOf(KoraDestination.Calendar))
        assertEquals(2, items.indexOf(KoraDestination.Homework))
    }
}
