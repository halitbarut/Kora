package com.barutdev.kora.ui.screens.dashboard

import android.util.Log

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.domain.model.PaymentRecord
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.ui.preferences.LocalUserPreferences
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.screens.common.StudentNameUiStatus
import com.barutdev.kora.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.kora.ui.screens.dashboard.components.AddLessonDialog
import com.barutdev.kora.ui.screens.dashboard.components.LogLessonDialog
import com.barutdev.kora.ui.theme.KoraAnimationSpecs
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.ui.model.AiInsightsUiState
import com.barutdev.kora.ui.model.AiStatus
import com.barutdev.kora.util.formatCurrency
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration

private data class CompletedLessonUiModel(
    val dateText: String,
    val durationText: String?,
    val notes: String?
)

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(
        key = expectedStudentId?.let { "dashboard-$it" } ?: "dashboard-default"
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val aiInsightsState by viewModel.aiInsightsState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = LocalUserPreferences.current
    val locale = LocalLocale.current
    val context = LocalContext.current
    val localizedContext = remember(context, locale) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }

    LaunchedEffect(expectedStudentId) {
        Log.d("DashboardScreen", "Composing for expectedStudentId=$expectedStudentId")
    }
    LaunchedEffect(viewModel.boundStudentId) {
        Log.d("DashboardScreen", "ViewModel bound to studentId=${viewModel.boundStudentId}")
    }
    LaunchedEffect(uiState.studentId) {
        uiState.studentId?.let { resolvedId ->
            Log.d("DashboardScreen", "Rendering dashboard for studentId=$resolvedId")
        }
    }

    LaunchedEffect(uiState.studentId, locale) {
        if (uiState.studentId != null) {
            viewModel.ensureAiInsights(locale)
        }
    }

LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DashboardEvent.StudentRemoved -> onNavigateToStudentList()
is DashboardEvent.ShowToast -> {
                    Toast.makeText(
                        localizedContext,
                        localizedContext.getString(event.messageRes),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val studentNameStatus = deriveStudentNameUiStatus(
        studentName = uiState.studentName,
        hasStudentReference = uiState.studentId != null
    )
    val fallbackTitle = koraStringResource(id = R.string.dashboard_title)
    val resolvedStudentName = uiState.studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    }
    val topBarTitle = resolvedStudentName?.let {
        koraStringResource(id = R.string.dashboard_student_label, it)
    } ?: fallbackTitle
    val navigateToListDescription = koraStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val settingsDescription = koraStringResource(id = R.string.dashboard_settings_icon_description)

    val topBarConfig = remember(
        topBarTitle,
        navigateToListDescription,
        settingsDescription,
        onNavigateToStudentList,
        onNavigateToSettings
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigateToListDescription,
                onClick = onNavigateToStudentList
            ),
            actions = listOf(
                TopBarAction(
                    icon = Icons.Filled.Settings,
                    contentDescription = settingsDescription,
                    onClick = onNavigateToSettings
                )
            )
        )
    }
    ScreenScaffoldConfig(topBarConfig = topBarConfig)

    AddLessonDialog(
        showDialog = uiState.isAddLessonDialogVisible,
        onDismiss = viewModel::dismissAddLessonDialog,
        onSave = { duration, notes ->
            viewModel.onSaveLesson(duration, notes)
        }
    )

    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
PaymentHistoryDialog(
        showDialog = uiState.isPaymentHistoryDialogVisible,
        records = paymentHistory,
        currencyCode = userPreferences.currencyCode,
        onDismiss = viewModel::dismissPaymentHistoryDialog
    )

    MarkAsPaidConfirmDialog(
        showDialog = uiState.isMarkAsPaidDialogVisible,
        onConfirm = { viewModel.confirmMarkAsPaidAndDismiss() },
        onDismiss = viewModel::dismissMarkAsPaidDialog
    )

    LogLessonDialog(
        showDialog = uiState.isLogLessonDialogVisible,
        lesson = uiState.lessonToLog,
        onDismiss = viewModel::dismissLogLessonDialog,
        onComplete = { duration, notes ->
            viewModel.onLogLessonComplete(duration, notes)
        },
        onMarkNotDone = { notes ->
            viewModel.onLogLessonMarkNotDone(notes)
        }
    )

    DashboardBody(
        totalHours = uiState.totalHours,
        hourlyRate = uiState.hourlyRate,
        totalAmountDue = uiState.totalAmountDue,
        completedLessonsAwaitingPayment = uiState.completedLessonsAwaitingPayment,
        lastPaymentDate = uiState.lastPaymentDate,
        upcomingLessons = uiState.upcomingLessons,
        pastLessonsToLog = uiState.pastLessonsToLog,
        onLogLessonClick = viewModel::onLogLessonClicked,
onMarkCurrentCycleAsPaid = viewModel::showMarkAsPaidDialog,
        onShowPaymentHistory = viewModel::showPaymentHistoryDialog,
        currencyCode = userPreferences.currencyCode,
        aiInsightsState = aiInsightsState,
        onGenerateAiInsights = { viewModel.retryAiInsights(locale) },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun DashboardBody(
    totalHours: Double,
    hourlyRate: Double,
    totalAmountDue: Double,
    completedLessonsAwaitingPayment: List<Lesson>,
    lastPaymentDate: Long?,
    upcomingLessons: List<Lesson>,
    pastLessonsToLog: List<Lesson>,
    onLogLessonClick: (Lesson) -> Unit,
    onMarkCurrentCycleAsPaid: () -> Unit,
    onShowPaymentHistory: () -> Unit,
    currencyCode: String,
    aiInsightsState: AiInsightsUiState,
    onGenerateAiInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val noDebtMessage = koraStringResource(id = R.string.no_debt_to_pay_toast)
        val context = LocalContext.current
        PaymentTrackingCard(
            totalHours = totalHours,
            hourlyRate = hourlyRate,
            totalAmountDue = totalAmountDue,
            lastPaymentDate = lastPaymentDate,
            onMarkPaidClick = {
                if (totalAmountDue > 0.0) {
                    onMarkCurrentCycleAsPaid()
                } else {
                    Toast.makeText(context, noDebtMessage, Toast.LENGTH_SHORT).show()
                }
            },
            onShowPaymentHistory = onShowPaymentHistory,
            currencyCode = currencyCode,
            locale = locale
        )
        CompletedLessonsCard(
            lessons = completedLessonsAwaitingPayment,
            locale = locale
        )
        UpcomingLessonsCard(
            upcomingLessons = upcomingLessons,
            locale = locale
        )
        LogPastLessonsCard(
            pastLessons = pastLessonsToLog,
            onLessonClick = onLogLessonClick,
            locale = locale
        )
        AiAssistantCard(
            aiState = aiInsightsState,
            onGenerateAgain = onGenerateAiInsights
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun LogPastLessonsCard(
    pastLessons: List<Lesson>,
    onLessonClick: (Lesson) -> Unit,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val lessonsWithDates = remember(pastLessons, locale) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", locale)
        pastLessons.map { lesson ->
            val formattedDate = Instant.ofEpochMilli(lesson.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
            lesson to formattedDate
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = KoraAnimationSpecs.contentSizeSpec),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = koraStringResource(id = R.string.dashboard_log_past_lessons_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (lessonsWithDates.isEmpty()) {
                Text(
                    text = koraStringResource(id = R.string.dashboard_log_past_lessons_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = koraStringResource(id = R.string.dashboard_log_past_lessons_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    lessonsWithDates.forEach { (lesson, formattedDate) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { onLessonClick(lesson) }, role = Role.Button),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTrackingCard(
    totalHours: Double,
    hourlyRate: Double,
    totalAmountDue: Double,
    lastPaymentDate: Long?,
    onMarkPaidClick: () -> Unit,
    onShowPaymentHistory: () -> Unit,
    currencyCode: String,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val amountText = remember(totalAmountDue, currencyCode) {
        formatCurrency(totalAmountDue, currencyCode)
    }
    val hourlyRateText = remember(hourlyRate, currencyCode) {
        formatCurrency(hourlyRate, currencyCode)
    }
    val hoursText = remember(totalHours, locale) {
        NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(totalHours)
    }
    val formattedLastPaymentDate = remember(lastPaymentDate, locale) {
        lastPaymentDate?.let { timestamp ->
            DateTimeFormatter
                .ofLocalizedDate(FormatStyle.LONG)
                .withLocale(locale)
                .format(
                    Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                )
        }
    }
    val sinceLastPaymentText = if (formattedLastPaymentDate != null) {
        koraStringResource(
            id = R.string.dashboard_payment_since_last_with_date,
            formattedLastPaymentDate
        )
    } else {
        koraStringResource(id = R.string.dashboard_payment_since_last_unknown)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = KoraAnimationSpecs.contentSizeSpec),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = koraStringResource(id = R.string.dashboard_payment_cycle_title),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onShowPaymentHistory) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = koraStringResource(id = R.string.dashboard_payment_history_icon_description)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = koraStringResource(
                        id = R.string.dashboard_payment_amount_value,
                        amountText
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = koraStringResource(
                        id = R.string.dashboard_payment_rate_info,
                        hoursText,
                        hourlyRateText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = sinceLastPaymentText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onMarkPaidClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = koraStringResource(id = R.string.dashboard_payment_mark_paid))
            }
        }
    }
}

@Composable
fun PaymentHistoryDialog(
    showDialog: Boolean,
    records: List<PaymentRecord>,
    currencyCode: String,
    onDismiss: () -> Unit
) {
    if (!showDialog) return
val title = koraStringResource(id = R.string.payment_history_title)
    val empty = koraStringResource(id = R.string.no_payment_history_toast)
    val close = koraStringResource(id = R.string.close_button)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            if (records.isEmpty()) {
                Text(text = empty)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    records.forEach { rec ->
                        val amountText = formatCurrency(rec.amountMinor / 100.0, currencyCode)
                        val dateText = DateTimeFormatter
                            .ofLocalizedDate(FormatStyle.LONG)
                            .format(Instant.ofEpochMilli(rec.paidAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = dateText, style = MaterialTheme.typography.bodyLarge)
                            Text(text = amountText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = close)
            }
        }
    )
}

@Composable
fun MarkAsPaidConfirmDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return
    val title = koraStringResource(id = R.string.dashboard_mark_paid_confirm_title)
    val message = koraStringResource(id = R.string.dashboard_mark_paid_confirm_message)
    val confirm = koraStringResource(id = R.string.dashboard_mark_paid_confirm)
    val cancel = koraStringResource(id = R.string.dashboard_mark_paid_cancel)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = confirm) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = cancel) }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedLessonsCard(
    lessons: List<Lesson>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val items = remember(lessons, locale) {
        if (lessons.isEmpty()) {
            emptyList()
        } else {
            val dateFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.LONG)
                .withLocale(locale)
            val durationFormatter = NumberFormat.getNumberInstance(locale).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }
            lessons.map { lesson ->
                val dateText = Instant.ofEpochMilli(lesson.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(dateFormatter)
                val durationText = lesson.durationInHours?.let { duration ->
                    durationFormatter.format(duration)
                }
                CompletedLessonUiModel(
                    dateText = dateText,
                    durationText = durationText,
                    notes = lesson.notes
                )
            }
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = KoraAnimationSpecs.contentSizeSpec),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = koraStringResource(id = R.string.dashboard_completed_lessons_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (items.isEmpty()) {
                Text(
                    text = koraStringResource(id = R.string.dashboard_completed_lessons_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = item.dateText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                item.durationText?.let { duration ->
                                    Text(
                                        text = koraStringResource(
                                            id = R.string.dashboard_completed_lessons_duration,
                                            duration
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                item.notes?.let { notes ->
                                    Text(
                                        text = koraStringResource(
                                            id = R.string.dashboard_completed_lessons_notes,
                                            notes
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpcomingLessonsCard(
    upcomingLessons: List<Lesson>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val formattedLessons = remember(upcomingLessons, locale) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", locale)
        upcomingLessons
            .sortedBy { lesson -> lesson.date }
            .map { lesson ->
                Instant.ofEpochMilli(lesson.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(formatter)
            }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = koraStringResource(id = R.string.dashboard_upcoming_lessons_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (formattedLessons.isEmpty()) {
                Text(
                    text = koraStringResource(id = R.string.dashboard_upcoming_lessons_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    formattedLessons.forEach { formattedDate ->
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AiAssistantCard(
    aiState: AiInsightsUiState,
    onGenerateAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = KoraAnimationSpecs.contentSizeSpec),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = koraStringResource(id = R.string.dashboard_ai_insights_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            AnimatedContent(
                targetState = aiState,
                transitionSpec = { KoraAnimationSpecs.cardContentTransform() },
                label = "dashboard_ai_card"
            ) { state ->
                when (state.status) {
                    AiStatus.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.insight?.let { insight ->
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(onClick = onGenerateAgain) {
                                Text(text = koraStringResource(id = R.string.ai_generate_again_action))
                            }
                        }
                    }
                    AiStatus.Error -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val message = state.messageRes?.let { koraStringResource(id = it) }
                                ?: koraStringResource(id = R.string.ai_generic_error_message)
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onGenerateAgain) {
                                Text(text = koraStringResource(id = R.string.ai_retry_action))
                            }
                        }
                    }
                    AiStatus.NoData -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val message = state.messageRes?.let { koraStringResource(id = it) }
                                ?: koraStringResource(id = R.string.ai_generic_no_data_message)
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onGenerateAgain) {
                                Text(text = koraStringResource(id = R.string.ai_generate_again_action))
                            }
                        }
                    }
                    AiStatus.Loading, AiStatus.Idle -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = koraStringResource(id = R.string.ai_loading_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    val previewUiState = DashboardUiState(
        studentId = 1,
        studentName = "Elif Yılmaz",
        hourlyRate = 80.0,
        totalHours = 4.5,
        totalAmountDue = 360.0,
        completedLessonsAwaitingPayment = listOf(
            Lesson(
                id = 3,
                studentId = 1,
                date = System.currentTimeMillis() - 172_800_000L,
                status = LessonStatus.COMPLETED,
                durationInHours = 2.0,
                notes = "Exam prep"
            ),
            Lesson(
                id = 4,
                studentId = 1,
                date = System.currentTimeMillis() - 259_200_000L,
                status = LessonStatus.COMPLETED,
                durationInHours = 2.5,
                notes = null
            )
        ),
        lastPaymentDate = System.currentTimeMillis() - 604_800_000L,
        upcomingLessons = listOf(
            Lesson(
                id = 1,
                studentId = 1,
                date = System.currentTimeMillis(),
                status = LessonStatus.SCHEDULED,
                durationInHours = null,
                notes = null
            )
        ),
        pastLessonsToLog = listOf(
            Lesson(
                id = 2,
                studentId = 1,
                date = System.currentTimeMillis() - 86_400_000L,
                status = LessonStatus.SCHEDULED,
                durationInHours = null,
                notes = null
            )
        )
    )
    val previewAiState = AiInsightsUiState(
        status = AiStatus.Success,
        insight = "Elif's performance in Algebra has improved while Geometry homework needs reinforcement. Plan targeted triangle exercises next."
    )
    KoraTheme {
        DashboardBody(
            totalHours = previewUiState.totalHours,
            hourlyRate = previewUiState.hourlyRate,
            totalAmountDue = previewUiState.totalAmountDue,
            completedLessonsAwaitingPayment = previewUiState.completedLessonsAwaitingPayment,
            lastPaymentDate = previewUiState.lastPaymentDate,
            upcomingLessons = previewUiState.upcomingLessons,
            pastLessonsToLog = previewUiState.pastLessonsToLog,
            onLogLessonClick = {},
            onMarkCurrentCycleAsPaid = {},
            onShowPaymentHistory = {},
            currencyCode = "TRY",
            aiInsightsState = previewAiState,
            onGenerateAiInsights = {}
        )
    }
}
