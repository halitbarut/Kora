package com.barutdev.kora.ui.screens.homework

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.barutdev.kora.MainDispatcherRule
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.StudentProfileUpdate
import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.AiInsightsRequestKey
import com.barutdev.kora.domain.model.ai.CachedAiInsight
import com.barutdev.kora.domain.repository.AiInsightsCacheRepository
import com.barutdev.kora.domain.repository.AiInsightsGenerationTracker
import com.barutdev.kora.domain.repository.AiRepository
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import com.barutdev.kora.domain.usecase.GenerateAiInsightsUseCase
import com.barutdev.kora.navigation.STUDENT_ID_ARG
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeworkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun switchingStudentId_invalidatesPreviousStudentData() = runTest {
        val studentRepository = FakeStudentRepository().apply {
            setStudent(student(id = 1, name = "Ahmet"))
            setStudent(student(id = 2, name = "Mehmet"))
        }
        val homeworkRepository = FakeHomeworkRepository().apply {
            setHomework(
                studentId = 1,
                homework = listOf(
                    homework(id = 11, studentId = 1, title = "Math sheet")
                )
            )
            setHomework(
                studentId = 2,
                homework = listOf(
                    homework(id = 22, studentId = 2, title = "Physics prep")
                )
            )
        }
        val generationTracker = FakeAiInsightsGenerationTracker()
        val cacheRepository = FakeAiInsightsCacheRepository()
        val aiUseCase = GenerateAiInsightsUseCase(
            studentRepository = studentRepository,
            lessonRepository = FakeLessonRepository(),
            homeworkRepository = homeworkRepository,
            aiRepository = FakeAiRepository()
        )

        val savedStateHandle = SavedStateHandle(mapOf(STUDENT_ID_ARG to 1))
        val viewModel = HomeworkViewModel(
            savedStateHandle = savedStateHandle,
            homeworkRepository = homeworkRepository,
            studentRepository = studentRepository,
            aiInsightsCacheRepository = cacheRepository,
            aiInsightsGenerationTracker = generationTracker,
            generateAiInsightsUseCase = aiUseCase
        )
        val viewModelJob = viewModel.viewModelScope.coroutineContext[Job]

        try {
            advanceUntilIdle()

            val initialName = viewModel.studentName.first { it == "Ahmet" }
            assertEquals("Ahmet", initialName)
            assertEquals(1, viewModel.studentId)
            val initialHomework = viewModel.homework.first { items ->
                items.isNotEmpty() && items.all { it.studentId == 1 }
            }
            assertEquals(1, initialHomework.single().studentId)

            savedStateHandle[STUDENT_ID_ARG] = 2
            advanceUntilIdle()

            val updatedName = viewModel.studentName.first { it == "Mehmet" }
            assertEquals("Mehmet", updatedName)
            assertEquals(2, viewModel.studentId)
            val updatedHomework = viewModel.homework.first { items ->
                items.isNotEmpty() && items.all { it.studentId == 2 }
            }
            assertEquals(2, updatedHomework.single().studentId)
            assertNotEquals(
                "Homework list should be replaced when student changes",
                initialHomework,
                updatedHomework
            )
        } finally {
            viewModelJob?.cancel()
        }
        viewModelJob?.join()
        advanceUntilIdle()
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()
    }

    private fun student(id: Int, name: String) = Student(
        id = id,
        fullName = name,
        hourlyRate = 100.0,
        lastPaymentDate = null
    )

    private fun homework(id: Int, studentId: Int, title: String) = Homework(
        id = id,
        studentId = studentId,
        title = title,
        description = "desc",
        creationDate = 0L,
        dueDate = 0L,
        status = HomeworkStatus.PENDING,
        performanceNotes = null
    )
}

private class FakeStudentRepository : StudentRepository {
    private val students = mutableMapOf<Int, MutableStateFlow<Student?>>()

    override fun getAllStudents(): Flow<List<Student>> = error("Not needed")

    override fun getStudentById(id: Int): Flow<Student?> =
        students.getOrPut(id) { MutableStateFlow(null) }

    fun setStudent(student: Student) {
        students.getOrPut(student.id) { MutableStateFlow(null) }.value = student
    }

    override suspend fun addStudent(student: Student) = error("Not needed")

    override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) = error("Not needed")

    override suspend fun updateStudentProfile(update: StudentProfileUpdate) =
        error("Not needed")

    override suspend fun deleteStudent(studentId: Int) = error("Not needed")
}

private class FakeHomeworkRepository : HomeworkRepository {
    private val homework = mutableMapOf<Int, MutableStateFlow<List<Homework>>>()

    override fun getHomeworkForStudent(studentId: Int): Flow<List<Homework>> =
        homework.getOrPut(studentId) { MutableStateFlow(emptyList()) }

    fun setHomework(studentId: Int, homework: List<Homework>) {
        this.homework.getOrPut(studentId) { MutableStateFlow(emptyList()) }.value = homework
    }

    override suspend fun insertHomework(homework: Homework) {
        val flow = this.homework.getOrPut(homework.studentId) { MutableStateFlow(emptyList()) }
        flow.value = flow.value + homework
    }

    override suspend fun updateHomework(homework: Homework) {
        val flow = this.homework.getOrPut(homework.studentId) { MutableStateFlow(emptyList()) }
        flow.value = flow.value.map { existing ->
            if (existing.id == homework.id) homework else existing
        }
    }
}

private class FakeLessonRepository : LessonRepository {
    private val lessons = MutableStateFlow(emptyList<Lesson>())

    override fun getAllLessons(): Flow<List<Lesson>> = lessons

    override fun getLessonsForStudent(studentId: Int): Flow<List<Lesson>> = lessons

    override suspend fun insertLesson(lesson: Lesson): Int = error("Not needed")

    override suspend fun updateLesson(lesson: Lesson) = error("Not needed")

    override suspend fun markCompletedLessonsAsPaid(studentId: Int) = error("Not needed")
}

private class FakeAiInsightsCacheRepository : AiInsightsCacheRepository {
    override fun observeInsight(
        studentId: Int,
        focus: AiInsightsFocus,
        localeTag: String
    ): Flow<CachedAiInsight?> = MutableStateFlow(null)

    override suspend fun getInsight(
        studentId: Int,
        focus: AiInsightsFocus,
        localeTag: String
    ): CachedAiInsight? = null

    override suspend fun saveInsight(insight: CachedAiInsight) = Unit

    override suspend fun clearInsight(studentId: Int, focus: AiInsightsFocus, localeTag: String) = Unit
}

private class FakeAiInsightsGenerationTracker : AiInsightsGenerationTracker {
    private val running = MutableStateFlow(emptyMap<AiInsightsRequestKey, String>())

    override val runningGenerations: StateFlow<Map<AiInsightsRequestKey, String>> = running

    override fun markGenerationStarted(key: AiInsightsRequestKey, signature: String) {
        running.value = running.value + (key to signature)
    }

    override fun markGenerationFinished(key: AiInsightsRequestKey) {
        running.value = running.value - key
    }

    override fun getRunningSignature(key: AiInsightsRequestKey): String? = running.value[key]
}

private class FakeAiRepository : AiRepository {
    override suspend fun generateInsights(prompt: String): String = error("Not needed")
}
