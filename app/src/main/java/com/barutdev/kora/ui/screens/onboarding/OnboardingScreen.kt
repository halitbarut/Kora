package com.barutdev.kora.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barutdev.kora.R
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.School

// Theme colors are derived from MaterialTheme.colorScheme

private val PillShape = RoundedCornerShape(50)
private val IllustrationShape = RoundedCornerShape(24.dp)

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val consentChecked by viewModel.consentChecked.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
.background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
when (page) {
                0 -> IllustratedPage(
                    title = koraStringResource(id = R.string.onboarding_welcome_title),
                    description = "",
                    isActive = pagerState.currentPage == 0
                ) {
                    WelcomeIllustration(isActive = pagerState.currentPage == 0)
                }
                1 -> IllustratedPage(
                    title = koraStringResource(id = R.string.onboarding_feature1_title),
                    description = koraStringResource(id = R.string.onboarding_feature1_body),
                    isActive = pagerState.currentPage == 1
                ) {
                    StudentsIllustration(isActive = pagerState.currentPage == 1)
                }
                2 -> IllustratedPage(
                    title = koraStringResource(id = R.string.onboarding_feature2_title),
                    description = koraStringResource(id = R.string.onboarding_feature2_body),
                    isActive = pagerState.currentPage == 2
                ) {
                    PaymentsIllustration(isActive = pagerState.currentPage == 2)
                }
                3 -> IllustratedPage(
                    title = koraStringResource(id = R.string.onboarding_feature3_title),
                    description = koraStringResource(id = R.string.onboarding_feature3_body),
                    isActive = pagerState.currentPage == 3
                ) {
                    AiIllustration(isActive = pagerState.currentPage == 3)
                }
                else -> LegalConsentPage(
                    title = koraStringResource(id = R.string.onboarding_legal_title),
                    body = koraStringResource(id = R.string.onboarding_legal_body),
                    checked = consentChecked,
                    onCheckedChange = viewModel::onConsentCheckedChange
                )
            }
        }

        BottomBar(
            currentPage = pagerState.currentPage,
            totalPages = 5,
            onNextClick = {
                val next = (pagerState.currentPage + 1).coerceAtMost(4)
                scope.launch { pagerState.animateScrollToPage(next) }
            },
            onGetStartedClick = { viewModel.completeOnboarding(onCompleted) },
            isGetStartedEnabled = consentChecked
        )
    }
}

@Composable
private fun IllustratedPage(
    title: String,
    description: String,
    isActive: Boolean,
    illustration: @Composable () -> Unit
) {
    // Staggered entrance progress values
    val illus = remember { androidx.compose.animation.core.Animatable(0f) }
    val titleAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    val descAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(isActive) {
        if (isActive) {
            illus.snapTo(0f); titleAnim.snapTo(0f); descAnim.snapTo(0f)
            illus.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            kotlinx.coroutines.delay(90)
            titleAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            kotlinx.coroutines.delay(90)
            descAnim.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(320, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        } else {
            illus.snapTo(0f); titleAnim.snapTo(0f); descAnim.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Illustration container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(IllustrationShape)
.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val tY = (1f - illus.value) * 20f
            Box(modifier = Modifier.graphicsLayer(alpha = illus.value, translationY = tY)) {
                illustration()
            }
        }
        TitleAndBody(title = title, description = description, titleProgress = titleAnim.value, descProgress = descAnim.value)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WelcomeIllustration(isActive: Boolean) {
    // Abstract composition with cap icon
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) 1f else 0.8f,
        animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    val offsetY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isActive) 0.dp else 10.dp,
        animationSpec = androidx.compose.animation.core.tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    Box(modifier = Modifier.size(180.dp).graphicsLayer { this.alpha = alpha; translationY = offsetY.toPx() }) {
        // Soft background shapes
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp)
                .clip(CircleShape)
.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = 12.dp)
                .size(80.dp, 24.dp)
                .clip(RoundedCornerShape(12.dp))
.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 12.dp, y = (-8).dp)
                .size(56.dp)
                .clip(CircleShape)
.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Center).size(64.dp)
        )
    }
}

@Composable
private fun StudentsIllustration(isActive: Boolean) {
    // Reveal-and-spread animation from center to a centered 3x3 grid
    val containerSize = 180.dp
    val dotSize = 14.dp
    val spacing = 36.dp
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Compute centered grid coordinates (top-left positions for each dot)
    val grid = remember(containerSize, dotSize, spacing) {
        val cols = 3
        val rows = 3
        val gridWidth = dotSize + spacing * (cols - 1)
        val gridHeight = dotSize + spacing * (rows - 1)
        val startX = (containerSize - gridWidth) / 2
        val startY = (containerSize - gridHeight) / 2
        List(rows * cols) { i ->
            val row = i / cols
            val col = i % cols
            startX + spacing * col to startY + spacing * row
        }
    }

    // Single progress driving all dots from center to targets
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(isActive) {
        if (isActive) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 520,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        } else {
            progress.snapTo(0f)
        }
    }

    // Center point (top-left) for dots when progress = 0
    val centerPos = remember(containerSize, dotSize) {
        val center = (containerSize - dotSize) / 2
        center to center
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(containerSize),
            contentAlignment = Alignment.TopStart
        ) {
            repeat(9) { idx ->
                val (targetX, targetY) = grid[idx]
                val currX = with(density) {
                    (centerPos.first.toPx() + (targetX.toPx() - centerPos.first.toPx()) * progress.value).toDp()
                }
                val currY = with(density) {
                    (centerPos.second.toPx() + (targetY.toPx() - centerPos.second.toPx()) * progress.value).toDp()
                }
                val scale = 0.2f + 0.8f * progress.value
val isGold = idx == 4 // center dot uses secondary accent
                val color = if (isGold) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .offset(x = currX, y = currY)
                        .size(dotSize * scale)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun PaymentsIllustration(isActive: Boolean) {
val lightPrimary = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val walletAngle by androidx.compose.animation.core.animateFloatAsState(
        if (isActive) -8f else -18f,
        animationSpec = androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    val calAngle by androidx.compose.animation.core.animateFloatAsState(
        if (isActive) 8f else 18f,
        animationSpec = androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(CircleShape)
            .background(lightPrimary)
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Payments,
            contentDescription = null,
tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .size(56.dp)
                .rotate(walletAngle)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.CalendarToday,
            contentDescription = null,
tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(28.dp)
                .size(56.dp)
                .rotate(calAngle)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp)
                .size(44.dp)
                .clip(CircleShape)
.background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun AiIllustration(isActive: Boolean) {
    val sparkleScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActive) 1f else 0.85f,
        animationSpec = androidx.compose.animation.core.tween(420, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    Box(modifier = Modifier.size(180.dp)) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .size(140.dp)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-24).dp, y = (-28).dp)
                .size((42f * sparkleScale).dp)
        )
    }
}

@Composable
private fun TitleAndBody(title: String, description: String, titleProgress: Float, descProgress: Float) {
    val titleTY = (1f - titleProgress) * 16f
    Text(
        text = title,
color = MaterialTheme.colorScheme.onBackground,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .graphicsLayer { alpha = titleProgress; translationY = titleTY }
    )
    if (description.isNotBlank()) {
        val descTY = (1f - descProgress) * 12f
        Text(
            text = description,
color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                .graphicsLayer { alpha = descProgress; translationY = descTY }
        )
    } else {
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LegalConsentPage(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val privacyLabel = koraStringResource(id = R.string.settings_privacy_policy_label)
    val termsLabel = koraStringResource(id = R.string.settings_terms_label)
    val consentText = koraStringResource(id = R.string.onboarding_consent_template, privacyLabel, termsLabel)

    val primaryColor = MaterialTheme.colorScheme.primary
    val annotated: AnnotatedString = remember(consentText, primaryColor) {
        buildAnnotatedString {
            val privacyUrl = "https://gist.github.com/halitbarut/b6b011b0d3cca23bd36781b9465a3cef"
            val termsUrl = "https://gist.github.com/halitbarut/5a56f975637a6e815feaea41539854a2"
            val privacyStart = consentText.indexOf(privacyLabel)
            val termsStart = consentText.indexOf(termsLabel)

            append(consentText)
            if (privacyStart >= 0) {
                addStyle(SpanStyle(color = primaryColor), privacyStart, privacyStart + privacyLabel.length)
                addStringAnnotation(tag = "link", annotation = privacyUrl, start = privacyStart, end = privacyStart + privacyLabel.length)
            }
            if (termsStart >= 0) {
                addStyle(SpanStyle(color = primaryColor), termsStart, termsStart + termsLabel.length)
                addStringAnnotation(tag = "link", annotation = termsUrl, start = termsStart, end = termsStart + termsLabel.length)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = body,
color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(8.dp))
            ClickableText(
                text = annotated,
style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                onClick = { offset ->
                    annotated.getStringAnnotations(tag = "link", start = offset, end = offset)
                        .firstOrNull()?.let { ann -> uriHandler.openUri(ann.item) }
                }
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentPage: Int,
    totalPages: Int,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    isGetStartedEnabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedPagerIndicators(total = totalPages, current = currentPage)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage < totalPages - 1) {
                Button(
                    onClick = onNextClick,
                    shape = PillShape,
colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = koraStringResource(id = R.string.onboarding_next))
                }
            } else {
val bgColor by androidx.compose.animation.animateColorAsState(
                    targetValue = if (isGetStartedEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    animationSpec = androidx.compose.animation.core.tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
                Button(
                    onClick = onGetStartedClick,
                    enabled = isGetStartedEnabled,
                    shape = PillShape,
colors = ButtonDefaults.buttonColors(
                        containerColor = bgColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(text = koraStringResource(id = R.string.onboarding_get_started))
                }
            }
        }
    }
}

@Composable
private fun AnimatedPagerIndicators(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { index ->
            val isActive = index == current
            val size by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isActive) 14.dp else 8.dp,
                animationSpec = androidx.compose.animation.core.tween(220)
            )
            val color by androidx.compose.animation.animateColorAsState(
targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = androidx.compose.animation.core.tween(220)
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
