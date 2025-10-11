package com.barutdev.kora.ui.screens.student_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

data class StudentWithDebt(
    val student: Student,
    val currentDebt: Double
)

data class StudentListUiState(
    val students: List<StudentWithDebt> = emptyList(),
    val defaultHourlyRate: Double = 0.0
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    private val _isEditStudentDialogVisible = MutableStateFlow(false)
    val isEditStudentDialogVisible: StateFlow<Boolean> = _isEditStudentDialogVisible.asStateFlow()

    private val _studentToEdit = MutableStateFlow<Student?>(null)
    val studentToEdit: StateFlow<Student?> = _studentToEdit.asStateFlow()

    private val studentsFlow = studentRepository.getAllStudents()
    private val lessonsFlow = lessonRepository.getAllLessons()

    val uiState: StateFlow<StudentListUiState> = combine(
        studentsFlow,
        lessonsFlow,
        userPreferencesRepository.userPreferences.map { preferences -> preferences.defaultHourlyRate }
    ) { students, lessons, hourlyRate ->
        val completedLessonsByStudent = lessons
            .filter { it.status == LessonStatus.COMPLETED }
            .groupBy { lesson -> lesson.studentId }

        val studentsWithDebt = students.map { student ->
            val totalHours = completedLessonsByStudent[student.id]
                ?.sumOf { lesson -> lesson.durationInHours ?: 0.0 }
                ?: 0.0
            StudentWithDebt(
                student = student,
                currentDebt = totalHours * student.hourlyRate
            )
        }

        StudentListUiState(
            students = studentsWithDebt,
            defaultHourlyRate = hourlyRate
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StudentListUiState()
    )

    fun onAddStudentClick() {
        _showAddStudentDialog.value = true
    }

    fun onAddStudentDismiss() {
        _showAddStudentDialog.value = false
    }

    fun onSaveStudent(fullName: String, hourlyRate: String) {
        viewModelScope.launch {
            val success = addStudent(fullName = fullName, hourlyRate = hourlyRate)
            if (success) {
                _showAddStudentDialog.value = false
            }
        }
    }

    suspend fun addStudent(fullName: String, hourlyRate: String): Boolean {
        val trimmedName = fullName.trim()
        val rate = hourlyRate.toDoubleOrNull() ?: return false

        if (trimmedName.isEmpty()) return false

        val newStudent = Student(
            id = 0,
            fullName = trimmedName,
            hourlyRate = rate
        )

        return try {
            studentRepository.addStudent(newStudent)
            true
        } catch (exception: Exception) {
            false
        }
    }

    fun onEditStudentClicked(student: Student) {
        _studentToEdit.value = student
        _isEditStudentDialogVisible.value = true
    }

    fun onEditStudentDismiss() {
        _isEditStudentDialogVisible.value = false
        _studentToEdit.value = null
    }

    fun onUpdateStudent(fullName: String, hourlyRate: String) {
        val student = _studentToEdit.value ?: return
        viewModelScope.launch {
            val success = updateStudent(student.id, fullName, hourlyRate)
            if (success) {
                onEditStudentDismiss()
            }
        }
    }

    suspend fun updateStudent(studentId: Int, newName: String, newRate: String): Boolean {
        val trimmedName = newName.trim()
        val rate = newRate.toDoubleOrNull() ?: return false

        if (trimmedName.isEmpty()) return false

        return try {
            studentRepository.updateStudent(studentId, trimmedName, rate)
            true
        } catch (exception: Exception) {
            false
        }
    }
}
