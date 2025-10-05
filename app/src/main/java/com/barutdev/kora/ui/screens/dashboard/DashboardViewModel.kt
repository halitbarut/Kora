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
import com.barutdev.kora.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    val completedLessonsAwaitingPayment: List<Lesson> = emptyList(),
    val lastPaymentDate: Long? = null,
    val isAddLessonDialogVisible: Boolean = false,
    val upcomingLessons: List<Lesson> = emptyList(),
    val pastLessonsToLog: List<Lesson> = emptyList(),
    val isLogLessonDialogVisible: Boolean = false,
    val lessonToLog: Lesson? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    private val addLessonDialogVisibility = MutableStateFlow(false)
    private val logLessonDialogVisibility = MutableStateFlow(false)
    private val selectedLessonForLogging = MutableStateFlow<Lesson?>(null)

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

    val uiState: StateFlow<DashboardUiState> = combine(
        student,
        lessons,
        addLessonDialogVisibility,
        logLessonDialogVisibility,
        selectedLessonForLogging
    ) { student, lessons, isAddLessonDialogVisible, isLogDialogVisible, lessonToLog ->
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val upcomingEndDate = today.plusDays(7)

        val completedLessons = lessons
            .filter { it.status == LessonStatus.COMPLETED }
            .sortedByDescending { it.date }
        val upcomingLessons = lessons
            .filter { lesson ->
                if (lesson.status != LessonStatus.SCHEDULED) return@filter false
                val lessonDate = Instant.ofEpochMilli(lesson.date).atZone(zoneId).toLocalDate()
                !lessonDate.isBefore(today) && !lessonDate.isAfter(upcomingEndDate)
            }
            .sortedBy { it.date }
        val pastLessonsToLog = lessons
            .filter { lesson ->
                if (lesson.status != LessonStatus.SCHEDULED) return@filter false
                val lessonDate = Instant.ofEpochMilli(lesson.date).atZone(zoneId).toLocalDate()
                lessonDate.isBefore(today)
            }
            .sortedByDescending { it.date }
        val totalHours = completedLessons.mapNotNull { it.durationInHours }.sum()
        val hourlyRate = student?.hourlyRate ?: 0.0
        DashboardUiState(
            studentId = student?.id,
            studentName = student?.fullName.orEmpty(),
            hourlyRate = hourlyRate,
            totalHours = totalHours,
            totalAmountDue = totalHours * hourlyRate,
            completedLessonsAwaitingPayment = completedLessons,
            lastPaymentDate = student?.lastPaymentDate,
            isAddLessonDialogVisible = isAddLessonDialogVisible,
            upcomingLessons = upcomingLessons,
            pastLessonsToLog = pastLessonsToLog,
            isLogLessonDialogVisible = isLogDialogVisible,
            lessonToLog = lessonToLog
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

    fun onLogLessonClicked(lesson: Lesson) {
        selectedLessonForLogging.value = lesson
        logLessonDialogVisibility.value = true
    }

    fun dismissLogLessonDialog() {
        clearLogLessonSelection()
    }

    fun onLogLessonComplete(duration: String, notes: String) {
        val lessonId = selectedLessonForLogging.value?.id ?: return
        viewModelScope.launch {
            completeLesson(lessonId, duration, notes)
        }
    }

    fun onLogLessonMarkNotDone(notes: String) {
        val lessonId = selectedLessonForLogging.value?.id ?: return
        viewModelScope.launch {
            markLessonNotDone(lessonId, notes)
        }
    }

    suspend fun markCurrentCycleAsPaid() {
        val hasLessonsToMark = lessons.value.any { it.status == LessonStatus.COMPLETED }
        if (!hasLessonsToMark) return
        lessonRepository.markCompletedLessonsAsPaid(studentId)
    }

    private suspend fun completeLesson(lessonId: Int, duration: String, notes: String) {
        val normalizedDuration = duration.trim().replace(',', '.')
        val durationValue = normalizedDuration.toDoubleOrNull()
        if (durationValue == null || durationValue <= 0.0) {
            return
        }
        val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: return
        val updatedLesson = lesson.copy(
            status = LessonStatus.COMPLETED,
            durationInHours = durationValue,
            notes = notes.trim().takeIf { it.isNotBlank() }
        )
        lessonRepository.updateLesson(updatedLesson)
        alarmScheduler.cancelAllLessonReminders(lessonId)
        clearLogLessonSelection()
    }

    private suspend fun markLessonNotDone(lessonId: Int, notes: String) {
        val lesson = lessons.value.firstOrNull { it.id == lessonId } ?: return
        val updatedLesson = lesson.copy(
            status = LessonStatus.CANCELLED,
            durationInHours = null,
            notes = notes.trim().takeIf { it.isNotBlank() }
        )
        lessonRepository.updateLesson(updatedLesson)
        alarmScheduler.cancelAllLessonReminders(lessonId)
        clearLogLessonSelection()
    }

    private fun clearLogLessonSelection() {
        logLessonDialogVisibility.value = false
        selectedLessonForLogging.value = null
    }
}
