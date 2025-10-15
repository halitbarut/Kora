package com.barutdev.kora.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.navigation.NavDestination

internal class BottomNavTransitionState {

    private var hasPrimedBottomNav by mutableStateOf(false)

    val isPrimed: Boolean
        @VisibleForTesting get() = hasPrimedBottomNav

    /**
     * Awaits a couple of frames before marking the bottom navigation transitions as primed so
     * the first visible transition happens on a settled UI tree.
     */
    suspend fun markPrimedAfterFirstFrames() {
        if (hasPrimedBottomNav) return
        withFrameNanos { _ -> }
        withFrameNanos { _ -> }
        hasPrimedBottomNav = true
    }

    /**
     * Determines whether the slide transition between the provided routes should run. Until the
     * pre-warm step completes we avoid animating between bottom navigation destinations to remove
     * the cold start hitch that occurs while screens finish their initial load.
     */
    fun shouldAnimate(initialDestination: NavDestination?, targetDestination: NavDestination?): Boolean {
        if (hasPrimedBottomNav) return true
        val initialIsBottom = KoraDestination.studentScopedFromRoute(initialDestination?.route) != null
        val targetIsBottom = KoraDestination.studentScopedFromRoute(targetDestination?.route) != null
        return !(initialIsBottom || targetIsBottom)
    }
}
