package com.barutdev.kora.navigation

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private object ImmediateFrameClock : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(0L)
}

class BottomNavTransitionStateTest {

    @Test
    fun shouldAnimate_isDisabledUntilPrimedForBottomNavRoutes() = runTest {
        val state = BottomNavTransitionState()
        val initialRoute = KoraDestination.Dashboard.createRoute(studentId = 11)
        val targetRoute = KoraDestination.Calendar.createRoute(studentId = 11)

        assertFalse(state.shouldAnimate(initialRoute, targetRoute))

        withContext(ImmediateFrameClock) {
            state.markPrimedAfterFirstFrames()
        }

        assertTrue(state.shouldAnimate(initialRoute, targetRoute))
    }
}
