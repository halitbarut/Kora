package com.barutdev.kora.ui.screens.calendar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.notifications.AlarmScheduler
import com.barutdev.kora.notifications.LessonReminderType
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CalendarViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val studentId: Int? = savedStateHandle[STUDENT_ID_ARG]
    val hasStudentReference: Boolean = studentId != null

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val initialDate = LocalDate.now(zoneId)

    private val currentMonthState = MutableStateFlow(YearMonth.from(initialDate))
    val currentMonth: StateFlow<YearMonth> = currentMonthState.asStateFlow()

    private val selectedDateState = MutableStateFlow(initialDate)
    val selectedDate: StateFlow<LocalDate> = selectedDateState.asStateFlow()

    private val logLessonDialogVisibility = MutableStateFlow(false)
    val isLogLessonDialogVisible: StateFlow<Boolean> = logLessonDialogVisibility.asStateFlow()

    private val selectedLessonForLogging = MutableStateFlow<Lesson?>(null)
    val lessonToLog: StateFlow<Lesson?> = selectedLessonForLogging.asStateFlow()

    private val studentNameState: StateFlow<String> = studentId?.let { id ->
        studentRepository.getStudentById(id)
            .map { student -> student?.fullName ?: "" }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ""
            )
    } ?: MutableStateFlow("")
    val studentName: StateFlow<String> = studentNameState

    private val lessonsState: StateFlow<List<Lesson>> = studentId?.let { id ->
        lessonRepository.getLessonsForStudent(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    } ?: MutableStateFlow(emptyList())
    val lessons: StateFlow<List<Lesson>> = lessonsState

    init {
        Log.d("CalendarViewModel", "Created for studentId=$studentId")
    }

    fun onPreviousMonth() {
        val newMonth = currentMonthState.value.minusMonths(1)
        currentMonthState.value = newMonth
        selectedDateState.value = newMonth.atDay(1)
    }

    fun onNextMonth() {
        val newMonth = currentMonthState.value.plusMonths(1)
        currentMonthState.value = newMonth
        selectedDateState.value = newMonth.atDay(1)
    }

    fun onSelectDate(date: LocalDate) {
        selectedDateState.value = date
    }

    suspend fun scheduleLesson(date: Long) {
        val targetStudentId = studentId ?: return
        val lesson = Lesson(
            id = 0,
            studentId = targetStudentId,
            date = date,
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null
        )
        val lessonId = lessonRepository.insertLesson(lesson)
        val preferences = userPreferencesRepository.userPreferences.first()
        if (!preferences.lessonRemindersEnabled) {
            return
        }

        val studentName = studentRepository.getStudentById(targetStudentId).first()?.fullName
            ?: return

        scheduleLessonReminders(
            lessonId = lessonId,
            lessonDateMillis = date,
            studentName = studentName,
            languageCode = preferences.languageCode,
            reminderHour = preferences.lessonReminderHour,
            reminderMinute = preferences.lessonReminderMinute
        )
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

    private fun scheduleLessonReminders(
        lessonId: Int,
        lessonDateMillis: Long,
        studentName: String,
        languageCode: String,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        val lessonDateTime = Instant.ofEpochMilli(lessonDateMillis).atZone(zoneId)

        val dayOfTrigger = lessonDateTime
            .withHour(reminderHour)
            .withMinute(reminderMinute)
            .withSecond(0)
            .withNano(0)
            .toInstant()
            .toEpochMilli()

        alarmScheduler.scheduleLessonReminder(
            lessonId = lessonId,
            type = LessonReminderType.DAY_OF,
            triggerAtMillis = dayOfTrigger,
            studentName = studentName,
            languageCode = languageCode
        )

        val dayBeforeDateTime = lessonDateTime.minusDays(1)
        val dayBeforeTrigger = dayBeforeDateTime
            .withHour(reminderHour)
            .withMinute(reminderMinute)
            .withSecond(0)
            .withNano(0)
            .toInstant()
            .toEpochMilli()

        alarmScheduler.scheduleLessonReminder(
            lessonId = lessonId,
            type = LessonReminderType.DAY_BEFORE,
            triggerAtMillis = dayBeforeTrigger,
            studentName = studentName,
            languageCode = languageCode
        )
    }
}
