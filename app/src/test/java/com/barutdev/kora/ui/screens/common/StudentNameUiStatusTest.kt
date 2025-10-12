package com.barutdev.kora.ui.screens.common

import org.junit.Assert.assertEquals
import org.junit.Test

class StudentNameUiStatusTest {

    @Test
    fun deriveStudentNameUiStatus_returnsReady_whenNamePresent() {
        val status = deriveStudentNameUiStatus(
            studentName = "Elif",
            hasStudentReference = true
        )

        assertEquals(StudentNameUiStatus.Ready, status)
    }

    @Test
    fun deriveStudentNameUiStatus_returnsPending_whenNameMissingButReferencePresent() {
        val status = deriveStudentNameUiStatus(
            studentName = "",
            hasStudentReference = true
        )

        assertEquals(StudentNameUiStatus.Pending, status)
    }

    @Test
    fun deriveStudentNameUiStatus_returnsUnavailable_whenNameMissingAndNoReference() {
        val status = deriveStudentNameUiStatus(
            studentName = "   ",
            hasStudentReference = false
        )

        assertEquals(StudentNameUiStatus.Unavailable, status)
    }
}
