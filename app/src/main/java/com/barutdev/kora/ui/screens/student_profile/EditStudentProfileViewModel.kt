package com.barutdev.kora.ui.screens.student_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.StudentProfileUpdate
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import com.barutdev.kora.ui.screens.student_profile.StudentProfileEvent.ProfileSaved
import com.barutdev.kora.ui.screens.student_profile.StudentProfileEvent.SaveFailed
import com.barutdev.kora.ui.screens.student_profile.StudentProfileEvent.StudentMissing
import com.barutdev.kora.ui.screens.student_profile.formatRate
import com.barutdev.kora.ui.screens.student_profile.isHourlyRateInputValid
import com.barutdev.kora.ui.screens.student_profile.normalizeRateInput
import com.barutdev.kora.ui.screens.student_profile.parseHourlyRateInput
import com.barutdev.kora.ui.screens.student_profile.StudentProfileEvent
import com.barutdev.kora.ui.screens.student_profile.StudentProfileSnapshot
import com.barutdev.kora.ui.screens.student_profile.StudentProfileUiState
import com.barutdev.kora.ui.screens.student_profile.withComputedSaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditStudentProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val studentId: Int = checkNotNull(savedStateHandle[STUDENT_ID_ARG])

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StudentProfileEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<StudentProfileEvent> = _events.asSharedFlow()

    private var hasLoadedInitialState = false
    private var initialSnapshot: StudentProfileSnapshot? = null

    init {
        viewModelScope.launch {
            combine(
                studentRepository.getStudentById(studentId),
                userPreferencesRepository.userPreferences
            ) { student, preferences ->
                student to preferences
            }.collect { (student, preferences) ->
                if (student == null) {
                    _events.tryEmit(StudentMissing)
                    return@collect
                }
                if (!hasLoadedInitialState) {
                    val initialRateInput = student.customHourlyRate?.formatRate().orEmpty()
                    val normalizedRateInput = normalizeRateInput(initialRateInput)
                    initialSnapshot = StudentProfileSnapshot(
                        fullName = student.fullName,
                        parentName = student.parentName.orEmpty(),
                        parentContact = student.parentContact.orEmpty(),
                        notes = student.notes.orEmpty(),
                        hourlyRateInput = normalizedRateInput
                    )
                    _uiState.update { state ->
                        val updated = state.copy(
                            isLoading = false,
                            fullName = student.fullName,
                            parentName = student.parentName.orEmpty(),
                            parentContact = student.parentContact.orEmpty(),
                            hourlyRateInput = normalizedRateInput,
                            notes = student.notes.orEmpty(),
                            defaultHourlyRate = preferences.defaultHourlyRate,
                            currencyCode = preferences.currencyCode,
                            fullNameError = null,
                            hourlyRateError = null
                        )
                        updated.withComputedSaveState(initialSnapshot)
                    }
                    hasLoadedInitialState = true
                } else {
                    _uiState.update { state ->
                        state.copy(
                            defaultHourlyRate = preferences.defaultHourlyRate,
                            currencyCode = preferences.currencyCode
                        ).withComputedSaveState(initialSnapshot)
                    }
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

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            val update = StudentProfileUpdate(
                id = studentId,
                fullName = fullName,
                parentName = parentName,
                parentContact = parentContact,
                notes = notes,
                customHourlyRate = customHourlyRate
            )
            try {
                studentRepository.updateStudentProfile(update)
                _events.emit(ProfileSaved)
            } catch (exception: Exception) {
                _events.emit(SaveFailed)
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }

    private fun updateState(transform: (StudentProfileUiState) -> StudentProfileUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.withComputedSaveState(initialSnapshot)
        }
    }
}
