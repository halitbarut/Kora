package com.barutdev.kora.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.barutdev.kora.data.local.migrations.MIGRATION_7_8
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration7to8Test {

    @Rule
    @JvmField
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KoraDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrateAddsStudentProfileColumns() {
        val dbName = "migration-test-db"

        helper.createDatabase(dbName, 7).apply {
            execSQL(
                "CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "fullName TEXT NOT NULL, " +
                    "hourlyRate REAL NOT NULL, " +
                    "lastPaymentDate INTEGER)"
            )
            execSQL(
                "INSERT INTO students (id, fullName, hourlyRate, lastPaymentDate) " +
                    "VALUES (1, 'Alice', 50.0, NULL)"
            )
            close()
        }

        helper.runMigrationsAndValidate(dbName, 8, true, MIGRATION_7_8).use { database ->
            database.query(
                "SELECT parentName, parentContact, notes, customHourlyRate FROM students WHERE id = 1"
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                val parentNameIndex = cursor.getColumnIndexOrThrow("parentName")
                val parentContactIndex = cursor.getColumnIndexOrThrow("parentContact")
                val notesIndex = cursor.getColumnIndexOrThrow("notes")
                val customRateIndex = cursor.getColumnIndexOrThrow("customHourlyRate")

                assertTrue(cursor.isNull(parentNameIndex))
                assertTrue(cursor.isNull(parentContactIndex))
                assertTrue(cursor.isNull(notesIndex))
                assertEquals(50.0, cursor.getDouble(customRateIndex), 0.0001)
            }
        }
    }
}
