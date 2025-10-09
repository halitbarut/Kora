package com.barutdev.kora.ui.screens.homework

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.ai.AiInsightsComputation
import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.AiInsightsResult
import com.barutdev.kora.domain.model.ai.AiInsightsSignatureBuilder
import com.barutdev.kora.domain.model.ai.AiInsightsRequestKey
import com.barutdev.kora.domain.model.ai.CachedAiInsight
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.AiInsightsCacheRepository
import com.barutdev.kora.domain.repository.AiInsightsGenerationTracker
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.usecase.GenerateAiInsightsUseCase
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import com.barutdev.kora.ui.model.AiInsightsUiState
import com.barutdev.kora.ui.model.AiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeworkRepository: HomeworkRepository,
    private val studentRepository: StudentRepository,
    private val aiInsightsCacheRepository: AiInsightsCacheRepository,
    private val aiInsightsGenerationTracker: AiInsightsGenerationTracker,
    private val generateAiInsightsUseCase: GenerateAiInsightsUseCase
) : ViewModel() {

    val studentId: Int = checkNotNull(
        savedStateHandle[STUDENT_ID_ARG]
    )

    private val student: StateFlow<Student?> = studentRepository.getStudentById(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val studentName: StateFlow<String> = student
        .map { studentSnapshot -> studentSnapshot?.fullName ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val homework: StateFlow<List<Homework>> = homeworkRepository.getHomeworkForStudent(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val dialogVisibility = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = dialogVisibility.asStateFlow()

    private val editingHomeworkState = MutableStateFlow<Homework?>(null)
    val editingHomework: StateFlow<Homework?> = editingHomeworkState.asStateFlow()

    private val _aiInsightsState = MutableStateFlow(AiInsightsUiState())
    val aiInsightsState: StateFlow<AiInsightsUiState> = _aiInsightsState.asStateFlow()

    private var lastRequestedLocale: Locale? = null
    private var lastAiInputSignature: String? = null
    private var cachedAiInsight: CachedAiInsight? = null
    private var latestStudentSnapshot: Student? = null
    private var latestHomeworkSnapshot: List<Homework> = emptyList()
    private var latestComputedSignature: String? = null
    private var aiInsightsJob: Job? = null
    private var activeRequestSignature: String? = null
    private var isCacheFetchInProgress: Boolean = false

    init {
        viewModelScope.launch {
            combine(student, homework) { studentSnapshot, homeworkSnapshot ->
                studentSnapshot to homeworkSnapshot
            }.collect { (studentSnapshot, homeworkSnapshot) ->
                latestStudentSnapshot = studentSnapshot
                latestHomeworkSnapshot = homeworkSnapshot
                val locale = lastRequestedLocale ?: return@collect
                val localeTag = locale.toLanguageTag()
                val signature = AiInsightsSignatureBuilder.build(
                    focus = AiInsightsFocus.HOMEWORK,
                    student = studentSnapshot,
                    lessons = emptyList(),
                    homework = homeworkSnapshot,
                    locale = locale
                )
                latestComputedSignature = signature
                val cached = cachedAiInsight
                val requestKey = AiInsightsRequestKey(
                    studentId = studentId,
                    focus = AiInsightsFocus.HOMEWORK,
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
            }
        }
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
                    focus = AiInsightsFocus.HOMEWORK,
                    localeTag = localeTag
                )
                val cached = aiInsightsCacheRepository.getInsight(
                    studentId = studentId,
                    focus = AiInsightsFocus.HOMEWORK,
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
        val homeworkSnapshot = if (latestComputedSignature != null) {
            latestHomeworkSnapshot
        } else {
            homework.value
        }
        return AiInsightsSignatureBuilder.build(
            focus = AiInsightsFocus.HOMEWORK,
            student = studentSnapshot,
            lessons = emptyList(),
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
            focus = AiInsightsFocus.HOMEWORK,
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
                    focus = AiInsightsFocus.HOMEWORK,
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
                    focus = AiInsightsFocus.HOMEWORK
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
                    focus = AiInsightsFocus.HOMEWORK,
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
                    focus = AiInsightsFocus.HOMEWORK,
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
                    focus = AiInsightsFocus.HOMEWORK,
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
                        messageRes = R.string.homework_ai_no_data_message
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
            messageRes = R.string.homework_ai_no_data_message
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

    fun showAddHomeworkDialog() {
        editingHomeworkState.value = null
        dialogVisibility.value = true
    }

    fun showEditHomeworkDialog(homework: Homework) {
        editingHomeworkState.value = homework
        dialogVisibility.value = true
    }

    fun dismissHomeworkDialog() {
        dialogVisibility.value = false
        editingHomeworkState.value = null
    }

    fun onSubmitHomework(
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val editingHomework = editingHomeworkState.value
        viewModelScope.launch {
            if (editingHomework == null) {
                addHomework(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = status,
                    performanceNotes = performanceNotes
                )
            } else {
                updateHomework(
                    homework = editingHomework,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = status,
                    performanceNotes = performanceNotes
                )
            }
            dismissHomeworkDialog()
        }
    }

    private suspend fun addHomework(
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val homework = Homework(
            id = 0,
            studentId = studentId,
            title = title.trim(),
            description = description.trim(),
            creationDate = System.currentTimeMillis(),
            dueDate = dueDate,
            status = status,
            performanceNotes = performanceNotes?.trim().takeIf { it?.isNotBlank() == true }
        )
        homeworkRepository.insertHomework(homework)
    }

    private suspend fun updateHomework(
        homework: Homework,
        title: String,
        description: String,
        dueDate: Long,
        status: HomeworkStatus,
        performanceNotes: String?
    ) {
        val updated = homework.copy(
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            status = status,
            performanceNotes = performanceNotes?.trim().takeIf { it?.isNotBlank() == true }
        )
        homeworkRepository.updateHomework(updated)
    }
}
