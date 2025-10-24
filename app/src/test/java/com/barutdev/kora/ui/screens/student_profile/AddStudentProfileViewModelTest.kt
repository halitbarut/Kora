package com.barutdev.kora.ui.screens.student_profile

import com.barutdev.kora.MainDispatcherRule
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.StudentProfileUpdate
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddStudentProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun savingValidStudentUsesDefaultRateWhenCustomBlank() = runTest(mainDispatcherRule.dispatcher) {
        val studentRepository = RecordingStudentRepository()
        val preferencesRepository = StubUserPreferencesRepository(
            initial = UserPreferences(
                isDarkMode = false,
                languageCode = "en",
                currencyCode = "USD",
                defaultHourlyRate = 85.0,
                lessonRemindersEnabled = true,
                logReminderEnabled = true,
                lessonReminderHour = 18,
                lessonReminderMinute = 0,
                logReminderHour = 21,
                logReminderMinute = 0
            )
        )
        val viewModel = AddStudentProfileViewModel(studentRepository, preferencesRepository)

        advanceUntilIdle()
        viewModel.onFullNameChanged("Charlie Brown")
        val event = backgroundScope.async {
            viewModel.events.first()
        }

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(StudentProfileEvent.ProfileSaved, event.await())
        val savedStudent = studentRepository.addedStudents.single()
        assertEquals("Charlie Brown", savedStudent.fullName)
        assertEquals(85.0, savedStudent.hourlyRate, 0.0)
        assertNull(savedStudent.customHourlyRate)
    }

    @Test
    fun repositoryFailureEmitsSaveFailed() = runTest(mainDispatcherRule.dispatcher) {
        val studentRepository = RecordingStudentRepository(shouldFail = true)
        val preferencesRepository = StubUserPreferencesRepository(
            initial = UserPreferences(
                isDarkMode = false,
                languageCode = "en",
                currencyCode = "USD",
                defaultHourlyRate = 60.0,
                lessonRemindersEnabled = true,
                logReminderEnabled = true,
                lessonReminderHour = 18,
                lessonReminderMinute = 0,
                logReminderHour = 21,
                logReminderMinute = 0
            )
        )
        val viewModel = AddStudentProfileViewModel(studentRepository, preferencesRepository)

        advanceUntilIdle()
        viewModel.onFullNameChanged("Chris Park")
        val event = backgroundScope.async {
            viewModel.events.first()
        }

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(StudentProfileEvent.SaveFailed, event.await())
        assertEquals(0, studentRepository.addedStudents.size)
    }
}

private class RecordingStudentRepository(
    private val shouldFail: Boolean = false
) : StudentRepository {
    val addedStudents = mutableListOf<Student>()

    override fun getAllStudents(): Flow<List<Student>> = error("Not needed")

    override fun getStudentById(id: Int): Flow<Student?> = error("Not needed")

    override suspend fun addStudent(student: Student) {
        if (shouldFail) {
            throw IllegalStateException("Insert failed")
        }
        addedStudents += student
    }

    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) = error("Not needed")

    override suspend fun updateStudentProfile(update: StudentProfileUpdate) = error("Not needed")

    override suspend fun deleteStudent(studentId: Int) = error("Not needed")
}

private class StubUserPreferencesRepository(
    initial: UserPreferences
) : UserPreferencesRepository {
    override val userPreferences = MutableStateFlow(initial)

    override suspend fun isFirstRunCompleted(): Boolean = error("Not needed")
    override suspend fun setFirstRunCompleted() = error("Not needed")
    override suspend fun getSavedLanguageOrNull(): String? = error("Not needed")
    override suspend fun getSavedCurrencyOrNull(): String? = error("Not needed")
    override suspend fun isOnboardingCompleted(): Boolean = error("Not needed")
    override suspend fun setOnboardingCompleted(completed: Boolean) = error("Not needed")
    override suspend fun updateTheme(isDarkMode: Boolean) = error("Not needed")
    override suspend fun updateLanguage(languageCode: String) = error("Not needed")
    override suspend fun updateCurrency(currencyCode: String) = error("Not needed")
    override suspend fun updateDefaultHourlyRate(hourlyRate: Double) = userPreferences.emit(
        userPreferences.value.copy(defaultHourlyRate = hourlyRate)
    )
    override suspend fun updateLessonRemindersEnabled(isEnabled: Boolean) = error("Not needed")
    override suspend fun updateLogReminderEnabled(isEnabled: Boolean) = error("Not needed")
    override suspend fun updateLessonReminderTime(hour: Int, minute: Int) = error("Not needed")
    override suspend fun updateLogReminderTime(hour: Int, minute: Int) = error("Not needed")
    override suspend fun resetPreferences() = error("Not needed")
}
