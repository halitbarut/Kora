package com.barutdev.kora.ui.screens.homework

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeworkRepository: HomeworkRepository,
    private val studentRepository: StudentRepository
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
