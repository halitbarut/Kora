package com.barutdev.kora.ui.screens.student_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _students: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.getAllStudents().collect { studentList ->
                _students.value = studentList
            }
        }
    }

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
}
