package com.barutdev.kora.navigation

internal class BottomBarState {
    private val lastStudentIds = mutableMapOf<KoraDestination.StudentScoped, Int>()

    fun record(destination: KoraDestination.StudentScoped, studentId: Int) {
        lastStudentIds[destination] = studentId
    }

    fun shouldRestore(destination: KoraDestination.StudentScoped, studentId: Int?): Boolean {
        if (studentId == null) return false
        return lastStudentIds[destination] == studentId
    }

    fun clear(destination: KoraDestination.StudentScoped) {
        lastStudentIds.remove(destination)
    }

    fun lastStudentId(destination: KoraDestination.StudentScoped): Int? = lastStudentIds[destination]
}
