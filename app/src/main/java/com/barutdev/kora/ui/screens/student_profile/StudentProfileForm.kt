package com.barutdev.kora.ui.screens.student_profile

data class StudentProfileUiState(
    val fullName: String = "",
    val parentName: String = "",
    val parentContact: String = "",
    val hourlyRateInput: String = "",
    val notes: String = "",
    val fullNameError: Int? = null,
    val hourlyRateError: Int? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val defaultHourlyRate: Double = 0.0,
    val currencyCode: String = "",
    val canSave: Boolean = false
)

data class StudentProfileSnapshot(
    val fullName: String,
    val parentName: String,
    val parentContact: String,
    val notes: String,
    val hourlyRateInput: String
)

sealed interface StudentProfileEvent {
    data object ProfileSaved : StudentProfileEvent
    data object SaveFailed : StudentProfileEvent
    data object StudentMissing : StudentProfileEvent
}

internal fun StudentProfileUiState.withComputedSaveState(
    snapshot: StudentProfileSnapshot?
): StudentProfileUiState {
    val trimmedFullName = fullName.trim()
    val trimmedParentName = parentName.trim()
    val trimmedParentContact = parentContact.trim()
    val trimmedNotes = notes.trim()
    val normalizedRate = normalizeRateInput(hourlyRateInput)
    val hasChanges = snapshot?.let { initial ->
        val snapshotFullName = initial.fullName.trim()
        val snapshotParentName = initial.parentName.trim()
        val snapshotParentContact = initial.parentContact.trim()
        val snapshotNotes = initial.notes.trim()
        val snapshotRate = initial.hourlyRateInput
        trimmedFullName != snapshotFullName ||
            trimmedParentName != snapshotParentName ||
            trimmedParentContact != snapshotParentContact ||
            trimmedNotes != snapshotNotes ||
            normalizedRate != snapshotRate
    } ?: (
        trimmedFullName.isNotEmpty() ||
            trimmedParentName.isNotEmpty() ||
            trimmedParentContact.isNotEmpty() ||
            trimmedNotes.isNotEmpty() ||
            normalizedRate.isNotEmpty()
        )
    val canSaveNow = !isSaving && !isLoading &&
        trimmedFullName.isNotEmpty() &&
        fullNameError == null &&
        hourlyRateError == null &&
        hasChanges
    return copy(canSave = canSaveNow)
}

internal fun normalizeRateInput(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    val normalizedDecimal = trimmed.replace(',', '.')
    return normalizedDecimal.trimEnd('.')
}

internal fun isHourlyRateInputValid(input: String): Boolean {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return true
    val normalized = trimmed.replace(',', '.')
    if (normalized == ".") return false
    if (normalized.count { it == '.' } > 1) return false
    return normalized.all { it.isDigit() || it == '.' }
}

internal fun parseHourlyRateInput(input: String): Double? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null
    if (!isHourlyRateInputValid(trimmed)) return null
    val normalized = trimmed.replace(',', '.')
    val sanitized = normalized.trimEnd('.')
    if (sanitized.isEmpty()) return null
    return sanitized.toDoubleOrNull()
}

internal fun Double.formatRate(): String = if (this % 1.0 == 0.0) {
    toInt().toString()
} else {
    this.toString()
}
