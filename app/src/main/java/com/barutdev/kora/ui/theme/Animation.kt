package com.barutdev.kora.ui.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.togetherWith

object KoraAnimationSpecs {

    private val easeOut = CubicBezierEasing(0.16f, 0.0f, 0.2f, 1.0f)
    private val easeInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

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
}
