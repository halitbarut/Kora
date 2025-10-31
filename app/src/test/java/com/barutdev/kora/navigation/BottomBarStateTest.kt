package com.barutdev.kora.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarStateTest {

    @Test
    fun shouldRestore_returnsTrue_whenStudentMatchesRecordedValue() {
        val state = BottomBarState()
        state.record(KoraDestination.Calendar, studentId = 12)

        assertTrue(state.shouldRestore(KoraDestination.Calendar, 12))
    }

    @Test
    fun shouldRestore_returnsFalse_whenStudentDiffers() {
        val state = BottomBarState()
        state.record(KoraDestination.Calendar, studentId = 12)

        assertFalse(state.shouldRestore(KoraDestination.Calendar, 7))
    }

    @Test
    fun clear_removesStoredState() {
        val state = BottomBarState()
        state.record(KoraDestination.Homework, studentId = 5)
        state.clear(KoraDestination.Homework)

        assertFalse(state.shouldRestore(KoraDestination.Homework, 5))
        assertNull(state.lastStudentId(KoraDestination.Homework))
    }

    @Test
    fun lastStudentId_returnsMostRecentId() {
        val state = BottomBarState()
        state.record(KoraDestination.Dashboard, studentId = 1)
        state.record(KoraDestination.Dashboard, studentId = 8)

        assertEquals(8, state.lastStudentId(KoraDestination.Dashboard))
    }

    @Test
    fun lastKnownStudentId_tracksLatestStudentId() {
        val state = BottomBarState()

        assertNull(state.lastKnownStudentId())

        state.record(KoraDestination.Calendar, studentId = 4)
        assertEquals(4, state.lastKnownStudentId())

        state.record(KoraDestination.Homework, studentId = 9)
        assertEquals(9, state.lastKnownStudentId())

        state.clear(KoraDestination.Homework)
        assertEquals(9, state.lastKnownStudentId())
    }
}
