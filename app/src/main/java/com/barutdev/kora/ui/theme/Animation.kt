package com.barutdev.kora.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Kora Animation Specifications
 * 
 * Professional animations with:
 * - Natural spring physics
 * - Smooth easing curves
 * - Micro-interaction feedback
 * - Reduced motion accessibility support
 */
object KoraAnimationSpecs {

    // Easing curves
    private val easeOut = CubicBezierEasing(0.16f, 0.0f, 0.2f, 1.0f)
    private val easeInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    private val emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    // Duration constants (in ms)
    object Duration {
        const val INSTANT = 100
        const val FAST = 150
        const val NORMAL = 250
        const val MEDIUM = 300
        const val SLOW = 400
        const val EMPHASIS = 500
    }

    // Spring configurations
    object Springs {
        val gentle = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
        val snappy = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        val bouncy = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    // Tween specs (original specs kept for compatibility)
    val fadeInSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 260, easing = easeOut)
    val fadeOutSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 220, easing = easeOut)
    private val slideSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 320,
        easing = easeInOut
    )
    val contentSizeSpec: FiniteAnimationSpec<IntSize> = tween(
        durationMillis = 260,
        easing = easeOut
    )
    val itemPlacementSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 420,
        easing = easeInOut
    )

    // Micro-interaction specs
    val pressSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = Duration.FAST,
        easing = FastOutSlowInEasing
    )
    val releaseSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = Duration.NORMAL,
        easing = easeOut
    )

    // Button press animation values
    object PressScale {
        const val PRESSED = 0.96f
        const val RELEASED = 1f
    }

    // Card elevation animation values
    object CardElevation {
        const val DEFAULT = 2f
        const val PRESSED = 8f
        const val DRAGGING = 12f
    }

    // Stagger delay for list items
    fun staggerDelay(index: Int, baseDelay: Int = 50): Int = index * baseDelay

    @OptIn(ExperimentalAnimationApi::class)
    fun defaultContentTransform(): ContentTransform =
        (fadeIn(animationSpec = fadeInSpec) + slideInVertically(
            animationSpec = slideSpec,
            initialOffsetY = { it / 6 }
        )) togetherWith (slideOutVertically(
            animationSpec = slideSpec,
            targetOffsetY = { it / 8 }
        ) + fadeOut(animationSpec = fadeOutSpec))

    @OptIn(ExperimentalAnimationApi::class)
    fun cardContentTransform(): ContentTransform =
        (fadeIn(animationSpec = fadeInSpec) + slideInVertically(
            animationSpec = slideSpec,
            initialOffsetY = { it / 8 }
        )) togetherWith (slideOutVertically(
            animationSpec = tween<IntOffset>(
                durationMillis = 240,
                easing = easeOut
            ),
            targetOffsetY = { it / 8 }
        ) + fadeOut(animationSpec = fadeOutSpec))

    // Screen transition animations
    fun screenEnterTransition(): EnterTransition =
        fadeIn(tween(Duration.MEDIUM, easing = easeOut)) +
        slideInHorizontally(
            initialOffsetX = { it / 4 },
            animationSpec = tween(Duration.MEDIUM, easing = emphasized)
        )

    fun screenExitTransition(): ExitTransition =
        fadeOut(tween(Duration.FAST, easing = easeOut)) +
        slideOutHorizontally(
            targetOffsetX = { -it / 6 },
            animationSpec = tween(Duration.FAST, easing = easeOut)
        )

    fun screenPopEnterTransition(): EnterTransition =
        fadeIn(tween(Duration.MEDIUM, easing = easeOut)) +
        slideInHorizontally(
            initialOffsetX = { -it / 6 },
            animationSpec = tween(Duration.MEDIUM, easing = emphasized)
        )

    fun screenPopExitTransition(): ExitTransition =
        fadeOut(tween(Duration.FAST, easing = easeOut)) +
        slideOutHorizontally(
            targetOffsetX = { it / 4 },
            animationSpec = tween(Duration.FAST, easing = easeOut)
        )

    // Dialog animations
    fun dialogEnterTransition(): EnterTransition =
        fadeIn(tween(Duration.NORMAL, easing = easeOut)) +
        scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(Duration.NORMAL, easing = emphasized)
        )

    fun dialogExitTransition(): ExitTransition =
        fadeOut(tween(Duration.FAST, easing = easeOut)) +
        scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(Duration.FAST, easing = easeOut)
        )

    // FAB animations
    fun fabEnterTransition(): EnterTransition =
        fadeIn(tween(Duration.NORMAL)) +
        scaleIn(initialScale = 0.6f, animationSpec = tween(Duration.NORMAL, easing = emphasized))

    fun fabExitTransition(): ExitTransition =
        fadeOut(tween(Duration.FAST)) +
        scaleOut(targetScale = 0.6f, animationSpec = tween(Duration.FAST))
}

/**
 * Check if the user has enabled reduced motion accessibility setting
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }
}
