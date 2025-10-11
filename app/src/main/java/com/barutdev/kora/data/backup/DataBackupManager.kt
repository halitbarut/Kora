package com.barutdev.kora.data.backup

import androidx.room.withTransaction
import com.barutdev.kora.data.local.AiInsightDao
import com.barutdev.kora.data.local.HomeworkDao
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.local.entity.HomeworkEntity
import com.barutdev.kora.data.local.entity.LessonEntity
import com.barutdev.kora.data.local.entity.StudentEntity
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.domain.model.LessonStatus
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DataBackupManager @Inject constructor(
    private val studentDao: StudentDao,
    private val lessonDao: LessonDao,
    private val homeworkDao: HomeworkDao,
    private val aiInsightDao: AiInsightDao,
    private val database: KoraDatabase
) {

    suspend fun exportToCsv(): String = withContext(Dispatchers.IO) {
        database.withTransaction {
            val students = studentDao.getStudentsSnapshot()
            val lessons = lessonDao.getLessonsSnapshot()
            val homework = homeworkDao.getHomeworkSnapshot()
            buildString {
                appendLine(STUDENT_HEADER)
                students.forEach { appendLine(formatStudent(it)) }
                appendLine(LESSON_HEADER)
                lessons.forEach { appendLine(formatLesson(it)) }
                appendLine(HOMEWORK_HEADER)
                homework.forEach { appendLine(formatHomework(it)) }
            }
        }
    }

    suspend fun importFromCsv(csv: String) = withContext(Dispatchers.IO) {
        val studentLines = mutableListOf<String>()
        val lessonLines = mutableListOf<String>()
        val homeworkLines = mutableListOf<String>()

        var currentSection: Section? = null
        csv.lineSequence()
            .map { it.trimEnd() }
            .forEachIndexed { index, rawLine ->
                if (rawLine.isEmpty()) return@forEachIndexed
                when (rawLine) {
                    STUDENT_HEADER -> currentSection = Section.STUDENTS
                    LESSON_HEADER -> currentSection = Section.LESSONS
                    HOMEWORK_HEADER -> currentSection = Section.HOMEWORK
                    else -> when (currentSection) {
                        Section.STUDENTS -> studentLines.add(rawLine)
                        Section.LESSONS -> lessonLines.add(rawLine)
                        Section.HOMEWORK -> homeworkLines.add(rawLine)
                        null -> throw MalformedBackupException("Unknown CSV section at line ${index + 1}")
                    }
                }
            }

        val students = studentLines.mapIndexed { lineIndex, line ->
            parseStudent(line, lineIndex + 2)
        }
        val lessons = lessonLines.mapIndexed { lineIndex, line ->
            parseLesson(line, lineIndex + students.size + 3)
        }
        val homework = homeworkLines.mapIndexed { lineIndex, line ->
            parseHomework(line, lineIndex + students.size + lessons.size + 4)
        }

        database.withTransaction {
            aiInsightDao.deleteAll()
            homeworkDao.deleteAll()
            lessonDao.deleteAll()
            studentDao.deleteAll()
            if (students.isNotEmpty()) studentDao.insertAll(students)
            if (lessons.isNotEmpty()) lessonDao.insertAll(lessons)
            if (homework.isNotEmpty()) homeworkDao.insertAll(homework)
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.withTransaction {
            aiInsightDao.deleteAll()
            homeworkDao.deleteAll()
            lessonDao.deleteAll()
            studentDao.deleteAll()
        }
    }

    private fun formatStudent(entity: StudentEntity): String = buildCsvRecord(
        entity.id.toString(),
        entity.fullName,
        entity.hourlyRate.toString(),
        entity.lastPaymentDate?.toString() ?: ""
    )

    private fun formatLesson(entity: LessonEntity): String = buildCsvRecord(
        entity.id.toString(),
        entity.studentId.toString(),
        entity.date.toString(),
        entity.status.name,
        entity.durationInHours?.toString() ?: "",
        entity.notes ?: "",
        entity.paymentTimestamp?.toString() ?: ""
    )

    private fun formatHomework(entity: HomeworkEntity): String = buildCsvRecord(
        entity.id.toString(),
        entity.studentId.toString(),
        entity.title,
        entity.description,
        entity.creationDate.toString(),
        entity.dueDate.toString(),
        entity.status.name,
        entity.performanceNotes ?: ""
    )

    private fun parseStudent(line: String, lineNumber: Int): StudentEntity {
        val fields = parseCsvLine(line, lineNumber)
        if (fields.size < 4) {
            throw MalformedBackupException("Invalid student record at line $lineNumber")
        }
        val id = fields[0].toIntOrNull()
            ?: throw MalformedBackupException("Invalid student id at line $lineNumber")
        val fullName = fields[1]
        val hourlyRate = fields[2].toDoubleOrNull()
            ?: throw MalformedBackupException("Invalid hourly rate at line $lineNumber")
        val lastPaymentDate = fields[3].takeIf { it.isNotBlank() }?.toLongOrNull()
        return StudentEntity(
            id = id,
            fullName = fullName,
            hourlyRate = hourlyRate,
            lastPaymentDate = lastPaymentDate
        )
    }

    private fun parseLesson(line: String, lineNumber: Int): LessonEntity {
        val fields = parseCsvLine(line, lineNumber)
        if (fields.size < 7) {
            throw MalformedBackupException("Invalid lesson record at line $lineNumber")
        }
        val id = fields[0].toIntOrNull()
            ?: throw MalformedBackupException("Invalid lesson id at line $lineNumber")
        val studentId = fields[1].toIntOrNull()
            ?: throw MalformedBackupException("Invalid lesson studentId at line $lineNumber")
        val date = fields[2].toLongOrNull()
            ?: throw MalformedBackupException("Invalid lesson date at line $lineNumber")
        val status = runCatching { LessonStatus.valueOf(fields[3]) }
            .getOrElse { throw MalformedBackupException("Invalid lesson status at line $lineNumber") }
        val duration = fields[4].takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val notes = fields[5].takeIf { it.isNotBlank() }
        val paymentTimestamp = fields.getOrNull(6)?.takeIf { it.isNotBlank() }?.toLongOrNull()
        return LessonEntity(
            id = id,
            studentId = studentId,
            date = date,
            status = status,
            durationInHours = duration,
            notes = notes,
            paymentTimestamp = paymentTimestamp
        )
    }

    private fun parseHomework(line: String, lineNumber: Int): HomeworkEntity {
        val fields = parseCsvLine(line, lineNumber)
        if (fields.size < 8) {
            throw MalformedBackupException("Invalid homework record at line $lineNumber")
        }
        val id = fields[0].toIntOrNull()
            ?: throw MalformedBackupException("Invalid homework id at line $lineNumber")
        val studentId = fields[1].toIntOrNull()
            ?: throw MalformedBackupException("Invalid homework studentId at line $lineNumber")
        val title = fields[2]
        val description = fields[3]
        val creationDate = fields[4].toLongOrNull()
            ?: throw MalformedBackupException("Invalid homework creation date at line $lineNumber")
        val dueDate = fields[5].toLongOrNull()
            ?: throw MalformedBackupException("Invalid homework due date at line $lineNumber")
        val status = runCatching { HomeworkStatus.valueOf(fields[6]) }
            .getOrElse { throw MalformedBackupException("Invalid homework status at line $lineNumber") }
        val performanceNotes = fields[7].takeIf { it.isNotBlank() }
        return HomeworkEntity(
            id = id,
            studentId = studentId,
            title = title,
            description = description,
            creationDate = creationDate,
            dueDate = dueDate,
            status = status,
            performanceNotes = performanceNotes
        )
    }

    private fun buildCsvRecord(vararg fields: String): String = fields.joinToString(separator = ",") { value ->
        escapeCsv(value)
    }

    private fun parseCsvLine(line: String, lineNumber: Int): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                inQuotes && char == '"' -> {
                    if (index + 1 < line.length && line[index + 1] == '"') {
                        current.append('"')
                        index++
                    } else {
                        inQuotes = false
                    }
                }
                char == '"' && !inQuotes -> inQuotes = true
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }
        if (inQuotes) {
            throw MalformedBackupException("Unterminated quotes at line $lineNumber")
        }
        result.add(current.toString())
        return result
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.any { it == ',' || it == '\n' || it == '\r' || it == '"' }
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private enum class Section {
        STUDENTS,
        LESSONS,
        HOMEWORK
    }

    class MalformedBackupException(message: String) : IOException(message)

    companion object {
        private const val STUDENT_HEADER = "[students]"
        private const val LESSON_HEADER = "[lessons]"
        private const val HOMEWORK_HEADER = "[homework]"
    }
}
