package com.barutdev.kora.domain.usecase

import android.util.Log
import com.barutdev.kora.domain.exception.AiException
import com.barutdev.kora.domain.exception.EmptyAiResponseException
import com.barutdev.kora.domain.exception.MissingAiApiKeyException
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.ai.AiInsightsComputation
import com.barutdev.kora.domain.model.ai.AiInsightsFocus
import com.barutdev.kora.domain.model.ai.AiInsightsResult
import com.barutdev.kora.domain.model.ai.AiInsightsSignatureBuilder
import com.barutdev.kora.domain.repository.AiRepository
import com.barutdev.kora.domain.repository.HomeworkRepository
import com.barutdev.kora.domain.repository.LessonRepository
import com.barutdev.kora.domain.repository.StudentRepository
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GenerateAiInsightsUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val lessonRepository: LessonRepository,
    private val homeworkRepository: HomeworkRepository,
    private val aiRepository: AiRepository
) {

    suspend operator fun invoke(
        studentId: Int,
        locale: Locale,
        focus: AiInsightsFocus
    ): AiInsightsComputation {
        val student = studentRepository.getStudentById(studentId).first()
        val lessons = lessonRepository.getLessonsForStudent(studentId).first()
        val homework = homeworkRepository.getHomeworkForStudent(studentId).first()

        val signature = AiInsightsSignatureBuilder.build(
            focus = focus,
            student = student,
            lessons = lessons,
            homework = homework,
            locale = locale
        )

        if (student == null || !hasEnoughData(focus, lessons, homework)) {
            return AiInsightsComputation(
                signature = signature,
                result = AiInsightsResult.NotEnoughData
            )
        }

        val prompt = buildPrompt(student, lessons, homework, focus, locale)

        val result = try {
            val insight = aiRepository.generateInsights(prompt).trim()
            if (insight.isBlank()) {
                AiInsightsResult.EmptyResponse
            } else {
                AiInsightsResult.Success(insight)
            }
        } catch (exception: MissingAiApiKeyException) {
            AiInsightsResult.MissingApiKey
        } catch (exception: EmptyAiResponseException) {
            AiInsightsResult.EmptyResponse
        } catch (exception: AiException) {
            AiInsightsResult.Error(exception)
        } catch (throwable: Throwable) {
            AiInsightsResult.Error(throwable)
        }

        return AiInsightsComputation(
            signature = signature,
            result = result
        )
    }

    private fun hasEnoughData(
        focus: AiInsightsFocus,
        lessons: List<Lesson>,
        homework: List<Homework>
    ): Boolean = when (focus) {
        AiInsightsFocus.DASHBOARD -> lessons.isNotEmpty() || homework.isNotEmpty()
        AiInsightsFocus.HOMEWORK -> homework.isNotEmpty()
    }

    private fun buildPrompt(
        student: Student,
        lessons: List<Lesson>,
        homework: List<Homework>,
        focus: AiInsightsFocus,
        locale: Locale
    ): String {
        val nowMillis = System.currentTimeMillis()
        val languageTag = locale.toLanguageTag()
        val zoneId = ZoneId.systemDefault()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale)
        val numberFormatter = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 2
        }

        val sortedLessons = lessons.sortedBy { it.date }
        val (historicalLessonsRaw, upcomingLessonsRaw) =
            sortedLessons.partition { it.date < nowMillis }
        val historicalLessons = historicalLessonsRaw
            .sortedByDescending { it.date }
            .take(10)
        val upcomingLessons = upcomingLessonsRaw
            .sortedBy { it.date }
            .take(10)
        val homeworkSummaries = homework
            .sortedByDescending { it.dueDate }
            .take(10)

        val earliestLesson = lessons.minByOrNull { it.date }?.date
        val latestLesson = lessons.maxByOrNull { it.date }?.date
        val earliestLessonText = earliestLesson?.let {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().format(dateFormatter)
        } ?: "n/a"
        val latestLessonText = latestLesson?.let {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().format(dateFormatter)
        } ?: "n/a"

        Log.d(
            TAG,
            "Building AI prompt for student=${student.id} (${student.fullName}). " +
                "Lessons total=${lessons.size}, historical=${historicalLessonsRaw.size}, " +
                "upcoming=${upcomingLessonsRaw.size}, range=$earliestLessonText-$latestLessonText"
        )

        fun sanitize(value: String?): String? {
            if (value.isNullOrBlank()) return null
            return value.replace('\n', ' ').replace('\r', ' ').trim()
        }

        fun Lesson.toSummary(): String {
            val dateText = Instant.ofEpochMilli(date)
                .atZone(zoneId)
                .toLocalDate()
                .format(dateFormatter)
            val statusText = status.name.lowercase(locale)
            val durationText = durationInHours?.let { numberFormatter.format(it) }
            val notesText = sanitize(notes)?.let { "notes=\"${it}\"" }
            val durationPart = durationText?.let { ", duration_hours=$it" } ?: ""
            val notesPart = notesText?.let { ", $it" } ?: ""
            return "date=$dateText, status=$statusText$durationPart$notesPart"
        }

        fun Homework.toSummary(): String {
            val dueDateText = Instant.ofEpochMilli(dueDate)
                .atZone(zoneId)
                .toLocalDate()
                .format(dateFormatter)
            val statusText = status.name.lowercase(locale)
            val performance = sanitize(performanceNotes)?.let { "performance_notes=\"${it}\"" }
            val descriptionText = sanitize(description)?.let { "description=\"${it}\"" }
            val parts = buildList {
                add("title=\"${title}\"")
                add("status=$statusText")
                add("due_date=$dueDateText")
                performance?.let { add(it) }
                descriptionText?.let { add(it) }
            }
            return parts.joinToString(separator = ", ")
        }

        val focusDescription = when (focus) {
            AiInsightsFocus.DASHBOARD ->
                "recent lesson engagement trends, historical progress, and upcoming priorities"
            AiInsightsFocus.HOMEWORK -> "homework performance, completion trends, and suggested next steps"
        }

        return buildString {
            appendLine("You are an AI assistant helping a private tutor prepare personalised insights.")
            appendLine("Respond in $languageTag using a professional, encouraging tone.")
            appendLine("Keep insights concise (no more than 2 sentences) and include at least one actionable recommendation for the next session.")
            appendLine("Reference both historical lesson performance and upcoming plans when relevant.")
            appendLine("Do not reference payments, fees, pricing, or hourly rates in your response.")
            appendLine()
            appendLine("Student profile:")
            appendLine("- name: ${student.fullName}")
            appendLine()
            appendLine("Focus: $focusDescription")
            appendLine()
            appendLine("Historical lessons (latest first):")
            if (historicalLessons.isEmpty()) {
                appendLine("- none")
            } else {
                historicalLessons.forEach { appendLine("- ${it.toSummary()}") }
            }
            appendLine()
            appendLine("Upcoming lessons (chronological):")
            if (upcomingLessons.isEmpty()) {
                appendLine("- none")
            } else {
                upcomingLessons.forEach { appendLine("- ${it.toSummary()}") }
            }
            appendLine()
            appendLine("Homework items (latest due dates first):")
            if (homeworkSummaries.isEmpty()) {
                appendLine("- none")
            } else {
                homeworkSummaries.forEach { appendLine("- ${it.toSummary()}") }
            }
            appendLine()
            appendLine("Return only the insight text without bullet points or preambles.")
        }
    }

    companion object {
        private const val TAG = "GenerateAiInsights"
    }
}
