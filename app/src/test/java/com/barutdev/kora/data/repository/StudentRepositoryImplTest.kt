package com.barutdev.kora.data.repository

import com.barutdev.kora.data.local.StudentDao
import com.barutdev.kora.data.local.entity.StudentEntity
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.domain.model.StudentProfileUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudentRepositoryImplTest {

    private lateinit var dao: RecordingStudentDao
    private lateinit var repository: StudentRepositoryImpl

    @Before
    fun setUp() {
        dao = RecordingStudentDao()
        repository = StudentRepositoryImpl(dao)
    }

    @Test
    fun addStudentPersistsCustomHourlyRate() = runTest {
        val student = Student(
            id = 0,
            fullName = "Ada Lovelace",
            hourlyRate = 45.0,
            customHourlyRate = 52.5
        )

        repository.addStudent(student)

        val inserted = dao.insertedStudents.single()
        assertEquals(52.5, inserted.customHourlyRate ?: error("Expected custom rate"), 0.0001)
        assertEquals(52.5, inserted.hourlyRate, 0.0001)
        assertEquals("Ada Lovelace", inserted.fullName)
        assertTrue((inserted.lastPaymentDate ?: 0L) > 0L)
    }

    @Test
    fun updateStudentProfileClearsCustomRate() = runTest {
        val update = StudentProfileUpdate(
            id = 12,
            fullName = "Updated Name",
            parentName = "Parent",
            parentContact = "parent@example.com",
            notes = "Needs extra practice",
            customHourlyRate = null
        )

        repository.updateStudentProfile(update)

        val recorded = dao.lastProfileUpdateArgs
        assertEquals(12, recorded?.studentId)
        assertEquals("Updated Name", recorded?.fullName)
        assertNull(recorded?.customHourlyRate)
    }

    @Test
    fun updateStudentProfilePersistsCustomRate() = runTest {
        val update = StudentProfileUpdate(
            id = 34,
            fullName = "New Name",
            parentName = null,
            parentContact = null,
            notes = null,
            customHourlyRate = 88.0
        )

        repository.updateStudentProfile(update)

        val recorded = dao.lastProfileUpdateArgs
        assertEquals(88.0, recorded?.customHourlyRate ?: error("Expected custom rate"), 0.0001)
    }

    private class RecordingStudentDao : StudentDao {
        data class ProfileUpdateArgs(
            val studentId: Int,
            val fullName: String,
            val parentName: String?,
            val parentContact: String?,
            val notes: String?,
            val customHourlyRate: Double?
        )

        val insertedStudents = mutableListOf<StudentEntity>()
        var lastProfileUpdateArgs: ProfileUpdateArgs? = null

        override suspend fun insert(student: StudentEntity) {
            insertedStudents += student
        }

        override suspend fun update(student: StudentEntity) = error("Not required")

        override suspend fun delete(student: StudentEntity) = error("Not required")

        override suspend fun updateLastPaymentDate(studentId: Int, lastPaymentDate: Long) = error("Not required")

        override fun getStudentById(id: Int): Flow<StudentEntity?> = emptyFlow()

        override suspend fun getStudentByIdSnapshot(id: Int): StudentEntity? = null

        override fun getAllStudents(): Flow<List<StudentEntity>> = emptyFlow()

        override suspend fun updateStudentHourlyRate(studentId: Int, newRate: Double) {
            // no-op for tests
        }

        override suspend fun updateStudentProfile(
            studentId: Int,
            fullName: String,
            parentName: String?,
            parentContact: String?,
            notes: String?,
            customHourlyRate: Double?
        ) {
            lastProfileUpdateArgs = ProfileUpdateArgs(
                studentId = studentId,
                fullName = fullName,
                parentName = parentName,
                parentContact = parentContact,
                notes = notes,
                customHourlyRate = customHourlyRate
            )
        }

        override suspend fun getStudentsSnapshot(): List<StudentEntity> = insertedStudents

        override suspend fun deleteById(studentId: Int) = error("Not required")

        override suspend fun insertAll(students: List<StudentEntity>) = error("Not required")

        override suspend fun deleteAll() = error("Not required")
    }
}
