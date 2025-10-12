package com.barutdev.kora.ui.screens.common

import androidx.annotation.VisibleForTesting

enum class StudentNameUiStatus {
    Ready,
    Pending,
    Unavailable
}

@VisibleForTesting
internal fun deriveStudentNameUiStatus(
    studentName: String,
    hasStudentReference: Boolean
): StudentNameUiStatus {
    return if (studentName.isBlank()) {
        if (hasStudentReference) {
            StudentNameUiStatus.Pending
        } else {
            StudentNameUiStatus.Unavailable
        }
    } else {
        StudentNameUiStatus.Ready
    }
}
