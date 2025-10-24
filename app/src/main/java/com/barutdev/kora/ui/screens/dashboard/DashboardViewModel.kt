package com.barutdev.kora.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.PaymentRecord
import com.barutdev.kora.domain.model.ai.AiInsightsComputation
import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.AiInsightsSignatureBuilder
import com.barutdev.kora.domain.model.ai.AiInsightsResult
import com.barutdev.kora.domain.model.ai.AiInsightsRequestKey
import com.barutdev.kora.domain.model.ai.CachedAiInsight
import com.barutdev.kora.domain.repository.AiInsightsCacheRepository
import com.barutdev.kora.domain.repository.AiInsightsGenerationTracker
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.PaymentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import com.barutdev.kora.domain.usecase.GenerateAiInsightsUseCase
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import com.barutdev.kora.notifications.AlarmScheduler
import com.barutdev.kora.ui.model.AiInsightsUiState
import com.barutdev.kora.ui.model.AiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOn

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
    val lessonToLog: Lesson? = null,
    val isPaymentHistoryDialogVisible: Boolean = false,
    val isMarkAsPaidDialogVisible: Boolean = false
)

sealed interface DashboardEvent {
    data object StudentRemoved : DashboardEvent
    data class ShowToast(val messageRes: Int) : DashboardEvent
}

private data class DashboardComputation(
    val student: Student?,
    val completedLessons: List<Lesson>,
    val upcomingLessons: List<Lesson>,
    val pastLessonsToLog: List<Lesson>,
    val totalHours: Double,
    val lastPaymentDate: Long?,
    val isAddLessonDialogVisible: Boolean,
    val isLogLessonDialogVisible: Boolean,
    val lessonToLog: Lesson?
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository,
    private val aiInsightsCacheRepository: AiInsightsCacheRepository,
    private val aiInsightsGenerationTracker: AiInsightsGenerationTracker,
    private val alarmScheduler: AlarmScheduler,
    private val generateAiInsightsUseCase: GenerateAiInsightsUseCase,
    private val paymentRepository: PaymentRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    val boundStudentId: Int = studentId

    private val _aiInsightsState = MutableStateFlow(AiInsightsUiState())
    val aiInsightsState: StateFlow<AiInsightsUiState> = _aiInsightsState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private var lastRequestedLocale: Locale? = null
    private var lastAiInputSignature: String? = null
    private var cachedAiInsight: CachedAiInsight? = null
    private var latestStudentSnapshot: Student? = null
    private var latestLessonsSnapshot: List<Lesson> = emptyList()
    private var latestHomeworkSnapshot: List<Homework> = emptyList()
    private var latestComputedSignature: String? = null
    private var aiInsightsJob: Job? = null
    private var activeRequestSignature: String? = null
    private var isCacheFetchInProgress: Boolean = false
    private var hasReceivedInitialStudentEmission = false

    private val addLessonDialogVisibility = MutableStateFlow(false)
    private val logLessonDialogVisibility = MutableStateFlow(false)
    private val selectedLessonForLogging = MutableStateFlow<Lesson?>(null)

    private val paymentHistoryDialogVisibility = MutableStateFlow(false)
    private val markAsPaidDialogVisibility = MutableStateFlow(false)

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

    private val homework: StateFlow<List<Homework>> =
        homeworkRepository.getHomeworkForStudent(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val paymentHistory: StateFlow<List<PaymentRecord>> =
        paymentRepository.observePaymentHistory(studentId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        Log.d("DashboardViewModel", "Created for studentId=$studentId")
        viewModelScope.launch {
            combine(student, lessons, homework) { studentSnapshot, lessonsSnapshot, homeworkSnapshot ->
                Triple(studentSnapshot, lessonsSnapshot, homeworkSnapshot)
            }.collect { (studentSnapshot, lessonsSnapshot, homeworkSnapshot) ->
                val previousStudentSnapshot = latestStudentSnapshot
                latestStudentSnapshot = studentSnapshot
                latestLessonsSnapshot = lessonsSnapshot
                latestHomeworkSnapshot = homeworkSnapshot
                val locale = lastRequestedLocale ?: return@collect
                val localeTag = locale.toLanguageTag()
                val signature = AiInsightsSignatureBuilder.build(
                    focus = AiInsightsFocus.DASHBOARD,
                    student = studentSnapshot,
                    lessons = lessonsSnapshot,
                    homework = homeworkSnapshot,
                    locale = locale
                )
                latestComputedSignature = signature
                val cached = cachedAiInsight
                val requestKey = AiInsightsRequestKey(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                )
                val runningSignature = aiInsightsGenerationTracker.getRunningSignature(requestKey)
                val isPendingForSignature =
                    runningSignature == signature || activeRequestSignature == signature
                if (isCacheFetchInProgress && cached == null) {
                    return@collect
                }
                if (cached != null && cached.signature == signature) {
                    if (isPendingForSignature) {
                        if (_aiInsightsState.value.status != AiStatus.Loading) {
                            _aiInsightsState.value = AiInsightsUiState(status = AiStatus.Loading)
                        }
                    } else if (
                        _aiInsightsState.value.status != AiStatus.Success ||
                        _aiInsightsState.value.insight != cached.insight
                    ) {
                        if (activeRequestSignature == signature) {
                            activeRequestSignature = null
                        }
                        _aiInsightsState.value = AiInsightsUiState(
                            status = AiStatus.Success,
                            insight = cached.insight
                        )
                    }
                    if (!isPendingForSignature) {
                        lastAiInputSignature = signature
                    }
                } else {
                    triggerAiInsightsGeneration(
                        locale = locale,
                        signature = signature,
                        force = false
                    )
                }
                if (!hasReceivedInitialStudentEmission) {
                    hasReceivedInitialStudentEmission = true
                    if (studentSnapshot == null) {
                        _events.tryEmit(DashboardEvent.StudentRemoved)
                    }
                } else if (previousStudentSnapshot != null && studentSnapshot == null) {
                    _events.tryEmit(DashboardEvent.StudentRemoved)
                }
            }
        }
    }

    private val defaultHourlyRateFlow = userPreferencesRepository.userPreferences
        .map { preferences -> preferences.defaultHourlyRate }

    private val dashboardComputation: kotlinx.coroutines.flow.Flow<DashboardComputation> = combine(
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
        DashboardComputation(
            student = student,
            completedLessons = completedLessons,
            upcomingLessons = upcomingLessons,
            pastLessonsToLog = pastLessonsToLog,
            totalHours = totalHours,
            lastPaymentDate = student?.lastPaymentDate,
            isAddLessonDialogVisible = isAddLessonDialogVisible,
            isLogLessonDialogVisible = isLogDialogVisible,
            lessonToLog = lessonToLog
        )
    }.flowOn(Dispatchers.Default)

    private val baseUiState: kotlinx.coroutines.flow.Flow<DashboardUiState> = combine(
        dashboardComputation,
        defaultHourlyRateFlow
    ) { computation, defaultHourlyRate ->
        val student = computation.student
        val hourlyRate = student?.customHourlyRate ?: defaultHourlyRate
        DashboardUiState(
            studentId = student?.id,
            studentName = student?.fullName.orEmpty(),
            hourlyRate = hourlyRate,
            totalHours = computation.totalHours,
            totalAmountDue = computation.totalHours * hourlyRate,
            completedLessonsAwaitingPayment = computation.completedLessons,
            lastPaymentDate = computation.lastPaymentDate,
            isAddLessonDialogVisible = computation.isAddLessonDialogVisible,
            upcomingLessons = computation.upcomingLessons,
            pastLessonsToLog = computation.pastLessonsToLog,
            isLogLessonDialogVisible = computation.isLogLessonDialogVisible,
            lessonToLog = computation.lessonToLog
        )
    }.flowOn(Dispatchers.Default)

    val uiState: StateFlow<DashboardUiState> = combine(
        baseUiState,
        paymentHistoryDialogVisibility,
        markAsPaidDialogVisibility
    ) { base, isPaymentHistoryVisible, isMarkAsPaidVisible ->
        base.copy(
            isPaymentHistoryDialogVisible = isPaymentHistoryVisible,
            isMarkAsPaidDialogVisible = isMarkAsPaidVisible
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

    fun ensureAiInsights(locale: Locale) {
        lastRequestedLocale = locale
        val signature = computeSignature(locale)
        latestComputedSignature = signature
        isCacheFetchInProgress = true
        viewModelScope.launch {
            try {
                val localeTag = locale.toLanguageTag()
                val requestKey = AiInsightsRequestKey(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                )
                val cached = aiInsightsCacheRepository.getInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                )
                cachedAiInsight = cached
                val runningSignature = aiInsightsGenerationTracker.getRunningSignature(requestKey)
                val isPendingForSignature =
                    runningSignature == signature || activeRequestSignature == signature
                if (cached != null) {
                    if (isPendingForSignature) {
                        _aiInsightsState.value = AiInsightsUiState(status = AiStatus.Loading)
                    } else {
                        if (activeRequestSignature == signature) {
                            activeRequestSignature = null
                        }
                        lastAiInputSignature = cached.signature
                        _aiInsightsState.value = AiInsightsUiState(
                            status = AiStatus.Success,
                            insight = cached.insight
                        )
                        if (cached.signature == signature) {
                            return@launch
                        }
                    }
                } else if (isPendingForSignature) {
                    _aiInsightsState.value = AiInsightsUiState(status = AiStatus.Loading)
                }
                triggerAiInsightsGeneration(
                    locale = locale,
                    signature = signature,
                    force = false
                )
            } finally {
                isCacheFetchInProgress = false
            }
        }
    }

    fun retryAiInsights(locale: Locale) {
        lastRequestedLocale = locale
        val signature = computeSignature(locale)
        latestComputedSignature = signature
        triggerAiInsightsGeneration(
            locale = locale,
            signature = signature,
            force = true
        )
    }

    private fun computeSignature(locale: Locale): String {
        val studentSnapshot = latestStudentSnapshot ?: student.value
        val lessonsSnapshot = if (latestComputedSignature != null) {
            latestLessonsSnapshot
        } else {
            lessons.value
        }
        val homeworkSnapshot = if (latestComputedSignature != null) {
            latestHomeworkSnapshot
        } else {
            homework.value
        }
        return AiInsightsSignatureBuilder.build(
            focus = AiInsightsFocus.DASHBOARD,
            student = studentSnapshot,
            lessons = lessonsSnapshot,
            homework = homeworkSnapshot,
            locale = locale
        )
    }

    private fun triggerAiInsightsGeneration(
        locale: Locale,
        signature: String,
        force: Boolean
    ) {
        val localeTag = locale.toLanguageTag()
        val requestKey = AiInsightsRequestKey(
            studentId = studentId,
            focus = AiInsightsFocus.DASHBOARD,
            localeTag = localeTag
        )
        val runningSignature = aiInsightsGenerationTracker.getRunningSignature(requestKey)
        if (!force && runningSignature == signature) {
            if (activeRequestSignature != signature) {
                waitForExistingGenerationResult(
                    requestKey = requestKey,
                    signature = signature,
                    localeTag = localeTag
                )
            }
            return
        }
        val hasFinalResultForSignature = hasFinalResultForSignature(signature)
        if (!force && hasFinalResultForSignature) {
            return
        }
        aiInsightsJob?.cancel()
        activeRequestSignature = signature
        aiInsightsGenerationTracker.markGenerationStarted(requestKey, signature)
        _aiInsightsState.value = AiInsightsUiState(status = AiStatus.Loading)
        aiInsightsJob = viewModelScope.launch {
            try {
                val cached = cachedAiInsight ?: aiInsightsCacheRepository.getInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                ).also { cachedAiInsight = it }
                if (!force && cached != null && cached.signature == signature) {
                    if (activeRequestSignature == signature) {
                        activeRequestSignature = null
                    }
                    lastAiInputSignature = signature
                    _aiInsightsState.value = AiInsightsUiState(
                        status = AiStatus.Success,
                        insight = cached.insight
                    )
                    return@launch
                }
                val computation = generateAiInsightsUseCase(
                    studentId = studentId,
                    locale = locale,
                    focus = AiInsightsFocus.DASHBOARD
                )
                handleAiComputationResult(
                    computation = computation,
                    localeTag = localeTag
                )
            } finally {
                aiInsightsGenerationTracker.markGenerationFinished(requestKey)
                if (activeRequestSignature == signature) {
                    activeRequestSignature = null
                }
            }
        }
    }

    private suspend fun handleAiComputationResult(
        computation: AiInsightsComputation,
        localeTag: String
    ) {
        when (val result = computation.result) {
            is AiInsightsResult.Success -> {
                val cached = CachedAiInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag,
                    insight = result.insight,
                    signature = computation.signature,
                    updatedAt = System.currentTimeMillis()
                )
                aiInsightsCacheRepository.saveInsight(cached)
                cachedAiInsight = cached
                lastAiInputSignature = computation.signature
                if (activeRequestSignature == computation.signature) {
                    activeRequestSignature = null
                }
                _aiInsightsState.value = AiInsightsUiState(
                    status = AiStatus.Success,
                    insight = result.insight
                )
            }
            AiInsightsResult.NotEnoughData -> {
                aiInsightsCacheRepository.clearInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                )
                cachedAiInsight = null
                lastAiInputSignature = computation.signature
                if (activeRequestSignature == computation.signature) {
                    activeRequestSignature = null
                }
                _aiInsightsState.value = mapAiResultToUiState(result)
            }
            else -> {
                lastAiInputSignature = computation.signature
                if (activeRequestSignature == computation.signature) {
                    activeRequestSignature = null
                }
                _aiInsightsState.value = mapAiResultToUiState(result)
            }
        }
    }

    private fun waitForExistingGenerationResult(
        requestKey: AiInsightsRequestKey,
        signature: String,
        localeTag: String
    ) {
        aiInsightsJob?.cancel()
        activeRequestSignature = signature
        _aiInsightsState.value = AiInsightsUiState(status = AiStatus.Loading)
        aiInsightsJob = viewModelScope.launch {
            try {
                aiInsightsGenerationTracker.runningGenerations
                    .map { it[requestKey] }
                    .filter { it != signature }
                    .first()
                val cached = aiInsightsCacheRepository.getInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.DASHBOARD,
                    localeTag = localeTag
                )
                cachedAiInsight = cached
                if (cached != null && cached.signature == signature) {
                    lastAiInputSignature = cached.signature
                    _aiInsightsState.value = AiInsightsUiState(
                        status = AiStatus.Success,
                        insight = cached.insight
                    )
                } else {
                    lastAiInputSignature = signature
                    _aiInsightsState.value = AiInsightsUiState(
                        status = AiStatus.NoData,
                        messageRes = R.string.dashboard_ai_no_data_message
                    )
                }
            } finally {
                if (activeRequestSignature == signature) {
                    activeRequestSignature = null
                }
            }
        }
    }

    private fun hasFinalResultForSignature(signature: String): Boolean {
        if (lastAiInputSignature != signature) return false
        return when (_aiInsightsState.value.status) {
            AiStatus.Success,
            AiStatus.Error,
            AiStatus.NoData -> true
            AiStatus.Idle,
            AiStatus.Loading -> false
        }
    }

    private fun mapAiResultToUiState(result: AiInsightsResult): AiInsightsUiState = when (result) {
        is AiInsightsResult.Success -> AiInsightsUiState(
            status = AiStatus.Success,
            insight = result.insight
        )
        AiInsightsResult.MissingApiKey -> AiInsightsUiState(
            status = AiStatus.Error,
            messageRes = R.string.ai_missing_api_key_message
        )
        AiInsightsResult.NotEnoughData -> AiInsightsUiState(
            status = AiStatus.NoData,
            messageRes = R.string.dashboard_ai_no_data_message
        )
        AiInsightsResult.EmptyResponse -> AiInsightsUiState(
            status = AiStatus.Error,
            messageRes = R.string.ai_empty_response_message
        )
        is AiInsightsResult.Error -> AiInsightsUiState(
            status = AiStatus.Error,
            messageRes = R.string.ai_generic_error_message
        )
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
        paymentRepository.markStudentAsPaid(studentId)
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

fun showPaymentHistoryDialog() {
        val history = paymentHistory.value
        if (history.isEmpty()) {
            _events.tryEmit(DashboardEvent.ShowToast(R.string.no_payment_history_toast))
        } else {
            paymentHistoryDialogVisibility.value = true
        }
    }

    fun dismissPaymentHistoryDialog() {
        paymentHistoryDialogVisibility.value = false
    }

    fun showMarkAsPaidDialog() {
        markAsPaidDialogVisibility.value = true
    }

    fun dismissMarkAsPaidDialog() {
        markAsPaidDialogVisibility.value = false
    }

    fun confirmMarkAsPaidAndDismiss() {
        viewModelScope.launch {
            try {
                markCurrentCycleAsPaid()
            } finally {
                markAsPaidDialogVisibility.value = false
            }
        }
    }
}
