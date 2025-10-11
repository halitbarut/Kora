package com.barutdev.kora.ui.screens.common

import org.junit.Assert.assertEquals
import org.junit.Test

class StudentNameUiStatusTest {

    @Test
    fun deriveStudentNameUiStatus_returnsLoaded_whenNamePresent() {
        val status = deriveStudentNameUiStatus(
            studentName = "Elif",
            hasStudentReference = true
        )

        assertEquals(StudentNameUiStatus.Loaded, status)
    }

    @Test
    fun deriveStudentNameUiStatus_returnsLoading_whenNameMissingButReferencePresent() {
        val status = deriveStudentNameUiStatus(
            studentName = "",
            hasStudentReference = true
        )

        assertEquals(StudentNameUiStatus.Loading, status)
    }

    @Test
    fun deriveStudentNameUiStatus_returnsMissing_whenNameMissingAndNoReference() {
        val status = deriveStudentNameUiStatus(
            studentName = "   ",
            hasStudentReference = false
        )

        assertEquals(StudentNameUiStatus.Missing, status)
    }
}
