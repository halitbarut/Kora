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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barutdev.kora.R
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleted: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val consentChecked by viewModel.consentChecked.collectAsState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> PageWelcome()
                1 -> FeaturePage(
                    icon = Icons.Outlined.Groups,
                    title = koraStringResource(id = R.string.onboarding_feature1_title),
                    body = koraStringResource(id = R.string.onboarding_feature1_body)
                )
                2 -> FeaturePage(
                    icon = Icons.Outlined.Payments,
                    title = koraStringResource(id = R.string.onboarding_feature2_title),
                    body = koraStringResource(id = R.string.onboarding_feature2_body)
                )
                3 -> FeaturePage(
                    icon = Icons.Outlined.AutoAwesome,
                    title = koraStringResource(id = R.string.onboarding_feature3_title),
                    body = koraStringResource(id = R.string.onboarding_feature3_body)
                )
                else -> PageLegal(
                    checked = consentChecked,
                    onCheckedChange = viewModel::onConsentCheckedChange
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            PagerIndicators(
                total = 5,
                current = pagerState.currentPage
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage < 4) {
                    Button(onClick = {
                        val next = (pagerState.currentPage + 1).coerceAtMost(4)
                        scope.launch {
                            pagerState.animateScrollToPage(next)
                        }
                    }) {
                        Text(text = koraStringResource(id = R.string.onboarding_next))
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeOnboarding(onCompleted) },
                        enabled = consentChecked
                    ) {
                        Text(text = koraStringResource(id = R.string.onboarding_get_started))
                    }
                }
            }
        }
    }
}

@Composable
private fun PageWelcome() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_kora_logo),
            contentDescription = koraStringResource(id = R.string.app_name),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = koraStringResource(id = R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeaturePage(icon: ImageVector, title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun PageLegal(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val privacyLabel = koraStringResource(id = R.string.settings_privacy_policy_label)
    val termsLabel = koraStringResource(id = R.string.settings_terms_label)
    val consentText = koraStringResource(id = R.string.onboarding_consent_template, privacyLabel, termsLabel)

    val primary = MaterialTheme.colorScheme.primary
    val annotated: AnnotatedString = remember(consentText, primary) {
        buildAnnotatedString {
            val privacyUrl = "https://gist.github.com/halitbarut/b6b011b0d3cca23bd36781b9465a3cef"
            val termsUrl = "https://gist.github.com/halitbarut/5a56f975637a6e815feaea41539854a2"
            val privacyStart = consentText.indexOf(privacyLabel)
            val termsStart = consentText.indexOf(termsLabel)

            append(consentText)
            if (privacyStart >= 0) {
                addStyle(SpanStyle(color = primary), privacyStart, privacyStart + privacyLabel.length)
                addStringAnnotation(tag = "link", annotation = privacyUrl, start = privacyStart, end = privacyStart + privacyLabel.length)
            }
            if (termsStart >= 0) {
                addStyle(SpanStyle(color = primary), termsStart, termsStart + termsLabel.length)
                addStringAnnotation(tag = "link", annotation = termsUrl, start = termsStart, end = termsStart + termsLabel.length)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(modifier = Modifier.width(8.dp))
            val onSurface = MaterialTheme.colorScheme.onSurface
            ClickableText(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                onClick = { offset ->
                    annotated.getStringAnnotations(tag = "link", start = offset, end = offset)
                        .firstOrNull()?.let { ann ->
                            uriHandler.openUri(ann.item)
                        }
                }
            )
        }
    }
}

@Composable
private fun PagerIndicators(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(total) { index ->
            val color = if (index == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
