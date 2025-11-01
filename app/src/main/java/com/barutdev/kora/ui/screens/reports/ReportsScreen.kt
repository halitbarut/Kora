package com.barutdev.kora.ui.screens.reports

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.domain.model.reports.MonthlyEarningsPoint
import com.barutdev.kora.domain.model.reports.ReportRange
import com.barutdev.kora.domain.model.reports.ReportSummary
import com.barutdev.kora.domain.model.reports.TopStudentEntry
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.preferences.LocalUserPreferences
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.util.koraStringResource
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Duration
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun ReportsScreen(
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(
        key = expectedStudentId?.let { "reports-$it" } ?: "reports-default"
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = LocalLocale.current
    val userPreferences = LocalUserPreferences.current
    val currencyFormatter = rememberCurrencyFormatter(
        locale = locale,
        currencyCode = userPreferences.currencyCode
    )
    val topBarTitle = koraStringResource(id = R.string.reports_title)
    val topBarConfig = remember(topBarTitle) { TopBarConfig(title = topBarTitle) }

    ScreenScaffoldConfig(topBarConfig = topBarConfig)

    ReportsScreenContent(
        uiState = uiState,
        locale = locale,
        currencyFormatter = currencyFormatter,
        onRangeSelected = viewModel::onRangeSelected,
        onRetry = viewModel::refresh,
        modifier = modifier.fillMaxSize()
    )
}

@VisibleForTesting
@Composable
internal fun ReportsScreenContent(
    uiState: ReportsUiState,
    locale: Locale,
    currencyFormatter: NumberFormat,
    onRangeSelected: (ReportRange) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            ReportsError(
                messageResId = uiState.errorMessage.messageResId,
                onRetry = onRetry,
                modifier = modifier
            )
        }

        else -> {
            ReportsContent(
                availableRanges = uiState.availableRanges,
                selectedRange = uiState.selectedRange,
                onRangeSelected = onRangeSelected,
                summary = uiState.summary,
                monthlyEarnings = uiState.monthlyEarnings,
                topStudents = uiState.topStudents,
                locale = locale,
                currencyFormatter = currencyFormatter,
                isEmptyPeriod = uiState.monthlyEarnings.all { it.earnings.compareTo(BigDecimal.ZERO) == 0 },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ReportsContent(
    availableRanges: List<ReportRange>,
    selectedRange: ReportRange,
    onRangeSelected: (ReportRange) -> Unit,
    summary: ReportSummary,
    monthlyEarnings: List<MonthlyEarningsPoint>,
    topStudents: List<TopStudentEntry>,
    locale: Locale,
    currencyFormatter: NumberFormat,
    isEmptyPeriod: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RangeFilterSection(
            ranges = availableRanges,
            selectedRange = selectedRange,
            onRangeSelected = onRangeSelected
        )
        SummaryCards(
            summary = summary,
            locale = locale,
            currencyFormatter = currencyFormatter
        )
        EarningsChart(
            points = monthlyEarnings,
            locale = locale,
            currencyFormatter = currencyFormatter,
            isEmptyPeriod = isEmptyPeriod
        )
        TopStudentsSection(
            students = topStudents,
            locale = locale
        )
    }
}

@Composable
private fun RangeFilterSection(
    ranges: List<ReportRange>,
    selectedRange: ReportRange,
    onRangeSelected: (ReportRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = koraStringResource(id = R.string.reports_filters_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(
                items = ranges,
                key = { it.labelResId }
            ) { range ->
                val label = koraStringResource(id = range.labelResId)
                FilterChip(
                    selected = range == selectedRange,
                    onClick = { if (range != selectedRange) onRangeSelected(range) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.testTag("reports-range-${range.labelResId}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(
    summary: ReportSummary,
    locale: Locale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val earningsValue = remember(summary.totalEarnings, currencyFormatter) {
        currencyFormatter.format(summary.totalEarnings)
    }
    val formattedHours = remember(summary.totalHours, locale) {
        val totalHours = summary.totalHours.toMinutes() / 60.0
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }.format(totalHours)
    }
    val hoursValue = koraStringResource(
        id = R.string.reports_summary_hours_unit,
        formattedHours
    )
    val activeStudentsValue = remember(summary.activeStudents, locale) {
        NumberFormat.getIntegerInstance(locale).format(summary.activeStudents)
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = koraStringResource(id = R.string.reports_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item(key = "total_earnings") {
                SummaryCard(
                    title = koraStringResource(id = R.string.reports_summary_total_earnings),
                    value = earningsValue,
                    modifier = Modifier.width(160.dp)
                )
            }
            item(key = "total_hours") {
                SummaryCard(
                    title = koraStringResource(id = R.string.reports_summary_total_hours),
                    value = hoursValue,
                    modifier = Modifier.width(160.dp)
                )
            }
            item(key = "active_students") {
                SummaryCard(
                    title = koraStringResource(id = R.string.reports_summary_active_students),
                    value = activeStudentsValue,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Clip
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun EarningsChart(
    points: List<MonthlyEarningsPoint>,
    locale: Locale,
    currencyFormatter: NumberFormat,
    isEmptyPeriod: Boolean,
    modifier: Modifier = Modifier
) {
    val title = koraStringResource(id = R.string.reports_chart_title)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (points.isEmpty() || isEmptyPeriod) {
            EmptyState(
                icon = Icons.Outlined.BarChart,
                message = koraStringResource(id = R.string.reports_message_empty_period)
            )
        } else {
            val monthFormatter = rememberMonthLabelFormatter(locale)
            val bars = remember(points, locale, currencyFormatter) {
                points.map { point ->
                    val label = monthFormatter.format(point.month)
                    val amount = point.earnings
                    ChartBar(
                        label = label,
                        value = amount.toDouble(),
                        formattedValue = currencyFormatter.format(amount)
                    )
                }
            }
            BarChart(bars = bars, locale = locale)
        }
    }
}

private data class ChartBar(
    val label: String,
    val value: Double,
    val formattedValue: String
)

private const val MONTH_LABEL_PATTERN = "LLL"
private val ReportsChartHeight = 160.dp
private val ReportsChartBarWidth = 28.dp
private val ReportsChartBarSpacing = 16.dp
private val ReportsChartLabelSpacing = 8.dp
private val ReportsChartMinBarHeight = 8.dp
private val ReportsChartBarCornerRadius = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
private val ReportsChartMinLabelPadding = 4.dp

@Composable
private fun rememberCurrencyFormatter(
    locale: Locale,
    currencyCode: String
): NumberFormat {
    return remember(locale, currencyCode) {
        NumberFormat.getCurrencyInstance(locale).apply {
            runCatching { Currency.getInstance(currencyCode) }
                .getOrNull()
                ?.let { desiredCurrency ->
                    currency = desiredCurrency
                }
        }
    }
}

@Composable
private fun rememberMonthLabelFormatter(locale: Locale): DateTimeFormatter {
    return remember(locale) {
        DateTimeFormatter.ofPattern(MONTH_LABEL_PATTERN)
            .withLocale(locale)
    }
}

@Composable
private fun BarChart(
    bars: List<ChartBar>,
    locale: Locale,
    modifier: Modifier = Modifier,
    chartHeight: Dp = ReportsChartHeight
) {
    // Measure label height to reserve space at bottom of chart
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall
    val density = LocalDensity.current
    val labelHeight = remember(labelStyle, density) {
        val textLayoutResult = textMeasurer.measure(
            text = "MMM",
            style = labelStyle
        )
        with(density) { textLayoutResult.size.height.toDp() }
    }

    // Calculate available height for bars by reserving space for labels
    val availableBarHeight = chartHeight - labelHeight - ReportsChartMinLabelPadding - ReportsChartLabelSpacing

    val maxValue = bars.maxOfOrNull { it.value }.takeUnless { it == null || it == 0.0 } ?: 1.0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(ReportsChartBarSpacing)
    ) {
        bars.forEach { bar ->
            val heightFraction = (bar.value / maxValue).coerceIn(0.0, 1.0).toFloat()
            val barDescription = koraStringResource(
                id = R.string.reports_chart_accessibility_bar,
                bar.label,
                bar.formattedValue
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val targetHeight = (availableBarHeight.value * heightFraction).dp
                val resolvedHeight = if (targetHeight < ReportsChartMinBarHeight) {
                    ReportsChartMinBarHeight
                } else {
                    targetHeight
                }
                Box(
                    modifier = Modifier
                        .width(ReportsChartBarWidth)
                        .height(resolvedHeight)
                        .clip(ReportsChartBarCornerRadius)
                        .background(MaterialTheme.colorScheme.primary)
                        .semantics {
                            contentDescription = barDescription
                        }
                )
                Spacer(modifier = Modifier.height(ReportsChartLabelSpacing))
                Text(
                    text = bar.label.uppercase(locale = locale),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TopStudentsSection(
    students: List<TopStudentEntry>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = koraStringResource(id = R.string.reports_top_students_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = koraStringResource(id = R.string.reports_top_students_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (students.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Group,
                message = koraStringResource(id = R.string.reports_top_students_empty)
            )
        } else {
            students.forEachIndexed { index, entry ->
                TopStudentRow(
                    rank = index + 1,
                    entry = entry,
                    locale = locale
                )
            }
        }
    }
}

@Composable
private fun TopStudentRow(
    rank: Int,
    entry: TopStudentEntry,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val hours = remember(entry.hoursTaught, locale) {
        val totalHours = entry.hoursTaught.toMinutes() / 60.0
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }.format(totalHours)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = koraStringResource(id = R.string.reports_summary_hours_unit, hours),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportsError(
    messageResId: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BarChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = koraStringResource(id = messageResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = koraStringResource(id = R.string.reports_action_retry))
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(locale = "en", showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    val preferences = UserPreferences(
        isDarkMode = false,
        languageCode = "en",
        currencyCode = "USD",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0
    )
    PreviewReportsScreen(locale = Locale.US, preferences = preferences)
}

@Preview(locale = "de", showBackground = true)
@Composable
private fun ReportsScreenGermanPreview() {
    val preferences = UserPreferences(
        isDarkMode = false,
        languageCode = "de",
        currencyCode = "EUR",
        defaultHourlyRate = 0.0,
        lessonRemindersEnabled = false,
        logReminderEnabled = false,
        lessonReminderHour = 9,
        lessonReminderMinute = 0,
        logReminderHour = 20,
        logReminderMinute = 0
    )
    PreviewReportsScreen(locale = Locale.GERMANY, preferences = preferences)
}

@Composable
private fun PreviewReportsScreen(
    locale: Locale,
    preferences: UserPreferences
) {
    KoraTheme {
        CompositionLocalProvider(
            LocalLocale provides locale,
            LocalUserPreferences provides preferences
        ) {
            val currencyFormatter = rememberCurrencyFormatter(locale, preferences.currencyCode)
            val sampleState = remember { previewReportsUiState() }
            ReportsScreenContent(
                uiState = sampleState,
                locale = locale,
                currencyFormatter = currencyFormatter,
                onRangeSelected = {},
                onRetry = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun previewReportsUiState(): ReportsUiState {
    val sampleSummary = ReportSummary(
        totalEarnings = BigDecimal("1234.50"),
        totalHours = Duration.ofHours(42),
        activeStudents = 8
    )
    val sampleMonthly = (0..5).map { offset ->
        val month = YearMonth.now().minusMonths((5 - offset).toLong())
        MonthlyEarningsPoint(
            month = month,
            earnings = BigDecimal.valueOf(800 + offset * 120L)
        )
    }
    val sampleStudents = listOf(
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Alice Johnson",
            hoursTaught = Duration.ofHours(10)
        ),
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Bilal Demir",
            hoursTaught = Duration.ofHours(8)
        ),
        TopStudentEntry(
            studentId = java.util.UUID.randomUUID(),
            studentName = "Chloe Martin",
            hoursTaught = Duration.ofHours(6)
        )
    )
    return ReportsUiState(
        availableRanges = ReportRange.presets,
        selectedRange = ReportRange.default,
        summary = sampleSummary,
        monthlyEarnings = sampleMonthly,
        topStudents = sampleStudents,
        isLoading = false,
        errorMessage = null
    )
}
