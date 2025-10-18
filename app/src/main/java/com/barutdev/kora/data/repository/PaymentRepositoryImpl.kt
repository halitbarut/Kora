package com.barutdev.kora.data.repository

import androidx.room.withTransaction
import com.barutdev.kora.data.local.KoraDatabase
import com.barutdev.kora.data.local.LessonDao
import com.barutdev.kora.data.local.PaymentRecordDao
import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.local.entity.PaymentRecordEntity
import com.barutdev.kora.data.mapper.toDomain
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.domain.model.PaymentRecord
import com.barutdev.kora.domain.repository.PaymentRepository
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentRepositoryImpl @Inject constructor(
    private val paymentRecordDao: PaymentRecordDao,
    private val lessonDao: LessonDao,
    private val studentDao: StudentDao,
    private val database: KoraDatabase
) : PaymentRepository {

    override fun observePaymentHistory(studentId: Int): Flow<List<PaymentRecord>> =
        paymentRecordDao.observeByStudent(studentId).map { it.toDomain() }

    override suspend fun markStudentAsPaid(studentId: Int) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            val students = studentDao.getStudentsSnapshot()
            val student = students.firstOrNull { it.id == studentId } ?: return@withTransaction
            val lessons = lessonDao.getLessonsSnapshot()
            val completed = lessons.filter { it.studentId == studentId && it.status == LessonStatus.COMPLETED }
            val totalHours = completed.mapNotNull { it.durationInHours }.sum()
            val amount = totalHours * student.hourlyRate
            val amountMinor = (amount * 100.0).roundToLong()
            if (amountMinor > 0L) {
                val record = PaymentRecordEntity(
                    studentId = studentId,
                    amountMinor = amountMinor,
                    paidAtEpochMs = now
                )
                paymentRecordDao.insert(record)
            }
            lessonDao.markCompletedLessonsAsPaid(
                studentId = studentId,
                paymentTimestamp = now
            )
            studentDao.updateLastPaymentDate(
                studentId = studentId,
                lastPaymentDate = now
            )
        }
    }
}