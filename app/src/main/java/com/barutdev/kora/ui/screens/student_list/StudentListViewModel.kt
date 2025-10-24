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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StudentWithDebt(
    val student: Student,
    val currentDebt: Double
)

private data class StudentHours(
    val student: Student,
    val totalHours: Double
)

data class StudentListUiState(
    val students: List<StudentWithDebt> = emptyList(),
    val defaultHourlyRate: Double = 0.0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val hasAnyStudents: Boolean = false
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val studentsFlow = studentRepository.getAllStudents()
    private val lessonsFlow = lessonRepository.getAllLessons()
    private val searchQuery = MutableStateFlow("")

    private val studentHoursFlow: Flow<List<StudentHours>> = combine(
        studentsFlow,
        lessonsFlow
    ) { students, lessons ->
        val completedLessonsByStudent = lessons
            .filter { it.status == LessonStatus.COMPLETED }
            .groupBy { lesson -> lesson.studentId }

        students.map { student ->
            val totalHours = completedLessonsByStudent[student.id]
                ?.sumOf { lesson -> lesson.durationInHours ?: 0.0 }
                ?: 0.0
            StudentHours(
                student = student,
                totalHours = totalHours
            )
        }
    }.flowOn(Dispatchers.Default)

    private val defaultHourlyRateFlow: Flow<Double> = userPreferencesRepository.userPreferences
        .map { preferences -> preferences.defaultHourlyRate }

    val uiState: StateFlow<StudentListUiState> = combine(
        studentHoursFlow,
        defaultHourlyRateFlow,
        searchQuery
    ) { studentHours, defaultHourlyRate, query ->
        val studentsWithDebt = studentHours.map { (student, totalHours) ->
            val effectiveRate = student.customHourlyRate ?: defaultHourlyRate
            StudentWithDebt(
                student = student,
                currentDebt = totalHours * effectiveRate
            )
        }
        val trimmedQuery = query.trim()
        val filteredStudents = if (trimmedQuery.isEmpty()) {
            studentsWithDebt
        } else {
            studentsWithDebt.filter { studentWithDebt ->
                studentWithDebt.student.fullName.contains(trimmedQuery, ignoreCase = true)
            }
        }

        StudentListUiState(
            students = filteredStudents,
            defaultHourlyRate = defaultHourlyRate,
            searchQuery = query,
            isSearchActive = trimmedQuery.isNotEmpty(),
            hasAnyStudents = studentsWithDebt.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StudentListUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onClearSearchQuery() {
        searchQuery.value = ""
    }

}
