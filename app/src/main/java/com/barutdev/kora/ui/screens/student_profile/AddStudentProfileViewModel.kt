package com.barutdev.kora.ui.screens.student_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddStudentProfileViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StudentProfileEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<StudentProfileEvent> = _events.asSharedFlow()

    private val initialSnapshot = StudentProfileSnapshot(
        fullName = "",
        parentName = "",
        parentContact = "",
        notes = "",
        hourlyRateInput = ""
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { preferences ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        defaultHourlyRate = preferences.defaultHourlyRate,
                        currencyCode = preferences.currencyCode
                    ).withComputedSaveState(initialSnapshot)
                }
            }
        }
    }

    fun onFullNameChanged(value: String) {
        val trimmed = value.trim()
        val error = if (trimmed.isEmpty()) R.string.student_profile_full_name_error else null
        updateState {
            it.copy(
                fullName = value,
                fullNameError = error
            )
        }
    }

    fun onParentNameChanged(value: String) {
        updateState { it.copy(parentName = value) }
    }

    fun onParentContactChanged(value: String) {
        updateState { it.copy(parentContact = value) }
    }

    fun onHourlyRateChanged(value: String) {
        val error = if (value.isBlank() || isHourlyRateInputValid(value)) {
            null
        } else {
            R.string.student_profile_hourly_rate_error
        }
        updateState {
            it.copy(
                hourlyRateInput = value,
                hourlyRateError = error
            )
        }
    }

    fun onNotesChanged(value: String) {
        updateState { it.copy(notes = value) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) {
            val fullNameError = if (state.fullName.trim().isEmpty()) {
                R.string.student_profile_full_name_error
            } else {
                state.fullNameError
            }
            val hourlyRateError = if (!isHourlyRateInputValid(state.hourlyRateInput)) {
                R.string.student_profile_hourly_rate_error
            } else {
                state.hourlyRateError
            }
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    hourlyRateError = hourlyRateError
                ).withComputedSaveState(initialSnapshot)
            }
            return
        }

        val fullName = state.fullName.trim()
        val parentName = state.parentName.trim().ifEmpty { null }
        val parentContact = state.parentContact.trim().ifEmpty { null }
        val notes = state.notes.trim().ifEmpty { null }
        val customHourlyRate = parseHourlyRateInput(state.hourlyRateInput)
        val effectiveHourlyRate = customHourlyRate ?: state.defaultHourlyRate

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            val newStudent = Student(
                id = 0,
                fullName = fullName,
                hourlyRate = effectiveHourlyRate,
                parentName = parentName,
                parentContact = parentContact,
                notes = notes,
                customHourlyRate = customHourlyRate
            )
            val success = try {
                studentRepository.addStudent(newStudent)
                true
            } catch (exception: Exception) {
                false
            }
            if (success) {
                _events.emit(StudentProfileEvent.ProfileSaved)
            } else {
                _events.emit(StudentProfileEvent.SaveFailed)
            }
            updateState { it.copy(isSaving = false) }
        }
    }

    private fun updateState(transform: (StudentProfileUiState) -> StudentProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.withComputedSaveState(initialSnapshot)
        }
    }
}
