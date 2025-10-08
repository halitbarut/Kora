package com.barutdev.kora.domain.model.ai

import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.Student
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object AiInsightsSignatureBuilder {

    fun build(
        focus: AiInsightsFocus,
        student: Student?,
        lessons: List<Lesson>,
        homework: List<Homework>,
        locale: Locale
    ): String {
        val zoneId = ZoneId.systemDefault()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale)

        fun sanitize(value: String?): String {
            if (value.isNullOrBlank()) return ""
            return value.replace('\n', ' ').replace('\r', ' ').trim()
        }

        fun Lesson.toSummary(): String {
            val dateText = Instant.ofEpochMilli(date)
                .atZone(zoneId)
                .toLocalDate()
                .format(dateFormatter)
            val statusText = status.name
            val durationPart = durationInHours?.let {
                "dur=${String.format(locale, "%.2f", it)}"
            } ?: ""
            val notesPart = sanitize(notes).takeIf { it.isNotEmpty() }?.let { "n=$it" } ?: ""
            return listOf(dateText, statusText, durationPart, notesPart)
                .filter { it.isNotEmpty() }
                .joinToString(separator = ";")
        }

        fun Homework.toSummary(): String {
            val dueDateText = Instant.ofEpochMilli(dueDate)
                .atZone(zoneId)
                .toLocalDate()
                .format(dateFormatter)
            val statusText = status.name
            val performance = sanitize(performanceNotes)
            val description = sanitize(description)
            return buildList {
                add("id=$id")
                add("title=${sanitize(title)}")
                add("due=$dueDateText")
                add("status=$statusText")
                if (performance.isNotEmpty()) add("perf=$performance")
                if (description.isNotEmpty()) add("desc=$description")
            }.joinToString(separator = ";")
        }

        return buildString {
            append(focus.name)
            append('|')
            append(locale.toLanguageTag())
            append('|')
        if (student != null) {
            append(student.id)
            append(';')
            append(sanitize(student.fullName))
        } else {
            append("student_missing")
        }
            append('|')
            when (focus) {
                AiInsightsFocus.DASHBOARD -> {
                    val completedLessons = lessons
                        .filter { it.status == LessonStatus.COMPLETED }
                        .sortedBy { it.id }
                        .joinToString(separator = "|") { it.toSummary() }
                    val upcomingLessons = lessons
                        .filter { it.status == LessonStatus.SCHEDULED }
                        .sortedBy { it.id }
                        .joinToString(separator = "|") { it.toSummary() }
                    append(completedLessons)
                    append("||")
                    append(upcomingLessons)
                    append("||")
                    append(
                        homework
                            .sortedBy { it.id }
                            .joinToString(separator = "|") { it.toSummary() }
                    )
                }
                AiInsightsFocus.HOMEWORK -> {
                    append(
                        homework
                            .sortedBy { it.id }
                            .joinToString(separator = "|") { it.toSummary() }
                    )
                }
            }
        }
    }
}
