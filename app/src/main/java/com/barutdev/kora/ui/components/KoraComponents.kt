package com.barutdev.kora.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.barutdev.kora.ui.theme.GradientPrimaryEnd
import com.barutdev.kora.ui.theme.GradientPrimaryStart
import com.barutdev.kora.ui.theme.KoraAnimationSpecs
import com.barutdev.kora.ui.theme.ShimmerBase
import com.barutdev.kora.ui.theme.ShimmerHighlight
import kotlinx.coroutines.delay

/**
 * Kora Design System Components
 * 
 * Reusable UI components with:
 * - Press animations
 * - Smooth transitions
 * - Loading states
 * - Accessibility support
 */

// ========== ANIMATED CARD ==========

@Composable
fun KoraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) 6.dp else elevation,
        animationSpec = tween(durationMillis = KoraAnimationSpecs.Duration.FAST),
        label = "cardElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) KoraAnimationSpecs.PressScale.PRESSED else KoraAnimationSpecs.PressScale.RELEASED,
        animationSpec = KoraAnimationSpecs.pressSpec,
        label = "cardScale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                                onClick()
                            }
                        )
                    }
                } else Modifier
            ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

@Composable
fun KoraGradientCard(
    modifier: Modifier = Modifier,
    gradientStart: Color = GradientPrimaryStart,
    gradientEnd: Color = GradientPrimaryEnd,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(gradientStart, gradientEnd))
                )
        ) {
            content()
        }
    }
}

// ========== ANIMATED BUTTONS ==========

@Composable
fun KoraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) KoraAnimationSpecs.PressScale.PRESSED else KoraAnimationSpecs.PressScale.RELEASED,
        animationSpec = KoraAnimationSpecs.pressSpec,
        label = "buttonScale"
    )
    
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
            },
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun KoraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) KoraAnimationSpecs.PressScale.PRESSED else KoraAnimationSpecs.PressScale.RELEASED,
        animationSpec = KoraAnimationSpecs.pressSpec,
        label = "secondaryButtonScale"
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
            },
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text)
    }
}

@Composable
fun KoraTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text)
    }
}

// ========== ANIMATED TEXT FIELD ==========

@Composable
fun KoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(durationMillis = KoraAnimationSpecs.Duration.FAST),
        label = "textFieldBorder"
    )
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(text = it) } },
            placeholder = placeholder?.let { { Text(text = it) } },
            isError = isError,
            singleLine = singleLine,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor
            )
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ========== LOADING STATES ==========

@Composable
fun KoraLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    message: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KoraShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    var shimmerPosition by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            shimmerPosition = 0f
            delay(300)
            shimmerPosition = 1f
            delay(700)
        }
    }
    
    val shimmerAlpha by animateFloatAsState(
        targetValue = shimmerPosition,
        animationSpec = KoraAnimationSpecs.fadeInSpec,
        label = "shimmer"
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = ShimmerBase.copy(alpha = 1f - (shimmerAlpha * 0.3f))
            )
    )
}

@Composable
fun KoraSkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KoraShimmerBox(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KoraShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                    )
                    KoraShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                    )
                }
            }
            KoraShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )
        }
    }
}

@Composable
fun KoraSkeletonList(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) { index ->
            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = KoraAnimationSpecs.fadeInSpec,
                label = "skeletonFade$index"
            )
            
            KoraSkeletonCard(
                modifier = Modifier.graphicsLayer { this.alpha = alpha }
            )
        }
    }
}

// ========== ANIMATED LIST ITEM ==========

@Composable
fun AnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(KoraAnimationSpecs.staggerDelay(index).toLong())
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = KoraAnimationSpecs.fadeInSpec,
        label = "listItemAlpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = KoraAnimationSpecs.fadeInSpec,
        label = "listItemOffset"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            }
    ) {
        content()
    }
}
