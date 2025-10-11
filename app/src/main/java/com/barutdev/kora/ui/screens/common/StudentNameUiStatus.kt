package com.barutdev.kora.ui.screens.common

import androidx.annotation.VisibleForTesting

enum class StudentNameUiStatus {
    Loaded,
    Loading,
    Missing
}

@VisibleForTesting
internal fun deriveStudentNameUiStatus(
    studentName: String,
    hasStudentReference: Boolean
): StudentNameUiStatus {
    return if (studentName.isBlank()) {
        if (hasStudentReference) {
            StudentNameUiStatus.Loading
        } else {
            StudentNameUiStatus.Missing
        }
    } else {
        StudentNameUiStatus.Loaded
    }
}
