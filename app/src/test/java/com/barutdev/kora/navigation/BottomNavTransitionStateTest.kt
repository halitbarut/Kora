package com.barutdev.kora.navigation

import androidx.compose.runtime.MonotonicFrameClock
import androidx.navigation.compose.ComposeNavigator
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
    fun shouldAnimate_isDisabledUntilPrimedForBottomNavDestinations() = runTest {
        val state = BottomNavTransitionState()
        val initialDestination = destination(KoraDestination.Dashboard.route)
        val targetDestination = destination(KoraDestination.Calendar.route)

        assertFalse(state.shouldAnimate(initialDestination, targetDestination))

        withContext(ImmediateFrameClock) {
            state.markPrimedAfterFirstFrames()
        }

        assertTrue(state.shouldAnimate(initialDestination, targetDestination))
    }

    private fun destination(route: String) =
        ComposeNavigator.Destination(ComposeNavigator()) {}.apply { this.route = route }
}
