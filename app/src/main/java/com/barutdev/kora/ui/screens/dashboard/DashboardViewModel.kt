package com.barutdev.kora.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val studentId: Int? = null,
    val studentName: String = "",
    val hourlyRate: Double = 0.0,
    val totalHours: Double = 0.0,
    val totalAmountDue: Double = 0.0,
    val isAddLessonDialogVisible: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    private val addLessonDialogVisibility = MutableStateFlow(false)

    val student: StateFlow<Student?> = studentRepository.getStudentById(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val lessons: StateFlow<List<Lesson>> = lessonRepository.getLessonsForStudent(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<DashboardUiState> = combine(student, lessons, addLessonDialogVisibility) { student, lessons, isDialogVisible ->
        val totalHours = lessons.mapNotNull { it.durationInHours }.sum()
        val hourlyRate = student?.hourlyRate ?: 0.0
        DashboardUiState(
            studentId = student?.id,
            studentName = student?.fullName.orEmpty(),
            hourlyRate = hourlyRate,
            totalHours = totalHours,
            totalAmountDue = totalHours * hourlyRate,
            isAddLessonDialogVisible = isDialogVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun showAddLessonDialog() {
        addLessonDialogVisibility.value = true
    }

    fun dismissAddLessonDialog() {
        addLessonDialogVisibility.value = false
    }

    suspend fun addLesson(duration: String, notes: String) {
        val normalizedDuration = duration.trim().replace(',', '.')
        val durationValue = normalizedDuration.toDoubleOrNull()
        if (durationValue == null || durationValue <= 0.0) {
            return
        }

        val lesson = Lesson(
            id = 0,
            studentId = studentId,
            date = System.currentTimeMillis(),
            status = LessonStatus.COMPLETED,
            durationInHours = durationValue,
            notes = notes.trim().takeIf { it.isNotBlank() }
        )

        lessonRepository.insertLesson(lesson)
        addLessonDialogVisibility.value = false
    }

    fun onSaveLesson(duration: String, notes: String) {
        viewModelScope.launch {
            addLesson(duration, notes)
        }
    }
}
