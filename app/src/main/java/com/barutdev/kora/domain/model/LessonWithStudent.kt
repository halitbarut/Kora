package com.barutdev.kora.domain.model

/**
 * Represents a lesson combined with its associated student data.
 *
 * This immutable data class is used to fetch lesson and student information
 * together for notification building. The BroadcastReceiver uses this to get
 * fresh data at notification time rather than caching stale data in Intent extras.
 *
 * @property lesson The lesson entity with schedule, status, and notes
 * @property student The associated student entity with name and identification
 */
data class LessonWithStudent(
    val lesson: Lesson,
    val student: Student
)
