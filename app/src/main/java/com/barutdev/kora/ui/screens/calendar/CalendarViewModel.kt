package com.barutdev.kora.ui.screens.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    val studentName: StateFlow<String> = studentRepository.getStudentById(studentId)
        .map { student -> student?.fullName ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val lessons: StateFlow<List<Lesson>> = lessonRepository.getLessonsForStudent(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    suspend fun scheduleLesson(date: Long) {
        val lesson = Lesson(
            id = 0,
            studentId = studentId,
            date = date,
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null
        )
        lessonRepository.insertLesson(lesson)
    }
}
