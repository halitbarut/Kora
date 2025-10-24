package com.barutdev.kora.ui.screens.student_list

import com.barutdev.kora.MainDispatcherRule
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.StudentProfileUpdate
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun searchQueryFiltersStudentsCaseInsensitive() = runTest(dispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.first { it.hasAnyStudents }
        viewModel.onSearchQueryChange("ali")
        val state = viewModel.uiState.first { it.isSearchActive }
        assertEquals(1, state.students.size)
        assertEquals("Alice Johnson", state.students.single().student.fullName)
        assertTrue(state.isSearchActive)
        advanceUntilIdle()
    }

    @Test
    fun clearingSearchRestoresFullList() = runTest(dispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.first { it.hasAnyStudents }
        viewModel.onSearchQueryChange("ali")
        viewModel.uiState.first { it.isSearchActive }
        viewModel.onClearSearchQuery()
        val state = viewModel.uiState.first { !it.isSearchActive }
        assertEquals(3, state.students.size)
        assertFalse(state.isSearchActive)
        advanceUntilIdle()
    }

    @Test
    fun unmatchedQueryProducesEmptyFilteredListWhileKeepingAllStudentsFlag() = runTest(dispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.first { it.hasAnyStudents }
        viewModel.onSearchQueryChange("zzz")
        val state = viewModel.uiState.first { it.isSearchActive }
        assertTrue(state.isSearchActive)
        assertTrue(state.hasAnyStudents)
        assertTrue(state.students.isEmpty())
        advanceUntilIdle()
    }

    private fun createViewModel(): StudentListViewModel {
        val students = listOf(
            Student(id = 1, fullName = "Alice Johnson", hourlyRate = 120.0),
            Student(id = 2, fullName = "Bob Stone", hourlyRate = 110.0),
            Student(id = 3, fullName = "Carla Brant", hourlyRate = 130.0)
        )
        val studentRepository = FakeStudentRepository(students)
        val lessons = listOf(
            Lesson(
                id = 1,
                studentId = 1,
                date = 0L,
                status = LessonStatus.COMPLETED,
                durationInHours = 1.5,
                notes = null
            )
        )
        val lessonRepository = FakeLessonRepository(lessons)
        val userPreferencesRepository = FakeUserPreferencesRepository(defaultHourlyRate = 90.0)
        return StudentListViewModel(
            studentRepository = studentRepository,
            lessonRepository = lessonRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }
}

private class FakeStudentRepository(
    initialStudents: List<Student>
) : StudentRepository {

    private val students = MutableStateFlow(initialStudents)

    override fun getAllStudents(): Flow<List<Student>> = students

    override fun getStudentById(id: Int): Flow<Student?> =
        students.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addStudent(student: Student) {
        students.value = students.value + student
    }

    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) {
        students.value = students.value.map { student ->
            if (student.id == studentId) {
                student.copy(hourlyRate = newRate, customHourlyRate = newRate)
            } else {
                student
            }
        }
    }

    override suspend fun updateStudentProfile(update: StudentProfileUpdate) {
        students.value = students.value.map { student ->
            if (student.id == update.id) {
                student.copy(
                    fullName = update.fullName,
                    parentName = update.parentName,
                    parentContact = update.parentContact,
                    notes = update.notes,
                    hourlyRate = update.customHourlyRate ?: student.hourlyRate,
                    customHourlyRate = update.customHourlyRate
                )
            } else {
                student
            }
        }
    }

    override suspend fun deleteStudent(studentId: Int) {
        students.value = students.value.filterNot { it.id == studentId }
    }
}

private class FakeLessonRepository(
    initialLessons: List<Lesson>
) : LessonRepository {

    private val lessons = MutableStateFlow(initialLessons)

    override fun getAllLessons(): Flow<List<Lesson>> = lessons

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> =
        lessons.map { list -> list.filter { it.studentId == studentId } }

    override suspend fun insertLesson(lesson: Lesson): Int = error("Not needed in tests")

    override suspend fun updateLesson(lesson: Lesson) = error("Not needed in tests")

    override suspend fun markCompletedLessonsAsPaid(studentId: Int) = error("Not needed in tests")
}

private class FakeUserPreferencesRepository(
    defaultHourlyRate: Double
) : UserPreferencesRepository {

    private val preferencesFlow = MutableStateFlow(
        UserPreferences(
            isDarkMode = false,
            languageCode = "en",
            currencyCode = "USD",
            defaultHourlyRate = defaultHourlyRate,
            lessonRemindersEnabled = false,
            logReminderEnabled = false,
            lessonReminderHour = 9,
            lessonReminderMinute = 0,
            logReminderHour = 18,
            logReminderMinute = 0
        )
    )

    override val userPreferences: Flow<UserPreferences> = preferencesFlow

    override suspend fun isFirstRunCompleted(): Boolean = error("Not needed in tests")

    override suspend fun setFirstRunCompleted() = error("Not needed in tests")

    override suspend fun getSavedLanguageOrNull(): String? = error("Not needed in tests")

    override suspend fun getSavedCurrencyOrNull(): String? = error("Not needed in tests")

    override suspend fun isOnboardingCompleted(): Boolean = error("Not needed in tests")

    override suspend fun setOnboardingCompleted(completed: Boolean) = error("Not needed in tests")

    override suspend fun updateTheme(isDarkMode: Boolean) = error("Not needed in tests")

    override suspend fun updateLanguage(languageCode: String) = error("Not needed in tests")

    override suspend fun updateCurrency(currencyCode: String) = error("Not needed in tests")

    override suspend fun updateDefaultHourlyRate(hourlyRate: Double) {
        preferencesFlow.value = preferencesFlow.value.copy(defaultHourlyRate = hourlyRate)
    }

    override suspend fun updateLessonRemindersEnabled(isEnabled: Boolean) = error("Not needed in tests")

    override suspend fun updateLogReminderEnabled(isEnabled: Boolean) = error("Not needed in tests")

    override suspend fun updateLessonReminderTime(hour: Int, minute: Int) = error("Not needed in tests")

    override suspend fun updateLogReminderTime(hour: Int, minute: Int) = error("Not needed in tests")

    override suspend fun resetPreferences() = error("Not needed in tests")
}
