package com.barutdev.kora.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE students ADD COLUMN parentName TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN parentContact TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN notes TEXT")
        database.execSQL("ALTER TABLE students ADD COLUMN customHourlyRate REAL")
        database.execSQL("UPDATE students SET customHourlyRate = hourlyRate")
    }
}
