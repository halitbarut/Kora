package com.barutdev.kora.ui.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.navigation.KoraDestination
import com.barutdev.kora.ui.preferences.LocalUserPreferences
import com.barutdev.kora.ui.screens.dashboard.components.AddLessonDialog
import com.barutdev.kora.ui.screens.dashboard.components.LogLessonDialog
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.util.formatCurrency
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch

private data class DashboardNavItem(
    val destination: KoraDestination,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

private data class CompletedLessonUiModel(
    val dateText: String,
    val durationText: String?,
    val notes: String?
)

@Composable
fun DashboardScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = LocalUserPreferences.current

    AddLessonDialog(
        showDialog = uiState.isAddLessonDialogVisible,
        onDismiss = viewModel::dismissAddLessonDialog,
        onSave = { duration, notes ->
            viewModel.onSaveLesson(duration, notes)
        }
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

    DashboardScreenContent(
        currentRoute = currentRoute,
        uiState = uiState,
        onNavigate = onNavigate,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToStudentList = onNavigateToStudentList,
        onLogLessonClick = viewModel::onLogLessonClicked,
        onMarkCurrentCycleAsPaid = {
            coroutineScope.launch {
                viewModel.markCurrentCycleAsPaid()
            }
        },
        currencyCode = userPreferences.currencyCode,
        modifier = modifier
    )
}

@Composable
private fun DashboardScreenContent(
    currentRoute: String?,
    uiState: DashboardUiState,
    onNavigate: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    onLogLessonClick: (Lesson) -> Unit,
    onMarkCurrentCycleAsPaid: () -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val navigationItems = remember {
        listOf(
            DashboardNavItem(
                destination = KoraDestination.Dashboard,
                icon = Icons.Outlined.Dashboard,
                labelRes = R.string.dashboard_title
            ),
            DashboardNavItem(
                destination = KoraDestination.Calendar,
                icon = Icons.Outlined.CalendarMonth,
                labelRes = R.string.calendar_title
            ),
            DashboardNavItem(
                destination = KoraDestination.Homework,
                icon = Icons.Outlined.Assignment,
                labelRes = R.string.homework_title
            )
        )
    }
    val studentName = if (uiState.studentName.isNotBlank()) {
        uiState.studentName
    } else {
        koraStringResource(id = R.string.dashboard_student_name)
    }
    val studentId = uiState.studentId

    Scaffold(
        modifier = modifier,
        topBar = {
            DashboardTopBar(
                studentName = studentName,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToStudentList = onNavigateToStudentList
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val label = koraStringResource(id = item.labelRes)
                    NavigationBarItem(
                        selected = currentRoute == item.destination.route,
                        onClick = {
                            if (currentRoute == item.destination.route) {
                                return@NavigationBarItem
                            }
                            when (item.destination) {
                                KoraDestination.Dashboard -> studentId?.let { id ->
                                    onNavigate(KoraDestination.Dashboard.createRoute(id))
                                }
                                KoraDestination.Calendar -> studentId?.let { id ->
                                    onNavigate(KoraDestination.Calendar.createRoute(id))
                                }
                                KoraDestination.Homework -> studentId?.let { id ->
                                    onNavigate(KoraDestination.Homework.createRoute(id))
                                }
                                else -> onNavigate(item.destination.route)
                            }
                        },
                        enabled = when (item.destination) {
                            KoraDestination.Dashboard,
                            KoraDestination.Calendar,
                            KoraDestination.Homework -> studentId != null
                            else -> true
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label
                            )
                        },
                        label = { Text(text = label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        DashboardBody(
            totalHours = uiState.totalHours,
            hourlyRate = uiState.hourlyRate,
            totalAmountDue = uiState.totalAmountDue,
            completedLessonsAwaitingPayment = uiState.completedLessonsAwaitingPayment,
            lastPaymentDate = uiState.lastPaymentDate,
            upcomingLessons = uiState.upcomingLessons,
            pastLessonsToLog = uiState.pastLessonsToLog,
            onLogLessonClick = onLogLessonClick,
            onMarkCurrentCycleAsPaid = onMarkCurrentCycleAsPaid,
            currencyCode = currencyCode,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    studentName: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToStudentList: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = koraStringResource(id = R.string.dashboard_student_label, studentName),
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToStudentList) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = koraStringResource(
                        id = R.string.top_bar_navigate_to_student_list_content_description
                    )
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = koraStringResource(id = R.string.dashboard_settings_icon_description)
                )
            }
        }
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
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val locale = LocalLocale.current
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaymentTrackingCard(
            totalHours = totalHours,
            hourlyRate = hourlyRate,
            totalAmountDue = totalAmountDue,
            lastPaymentDate = lastPaymentDate,
            onMarkPaidClick = onMarkCurrentCycleAsPaid,
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
        AiAssistantCard()
    }
}

@Composable
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
        modifier = modifier.fillMaxWidth(),
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
private fun PaymentTrackingCard(
    totalHours: Double,
    hourlyRate: Double,
    totalAmountDue: Double,
    lastPaymentDate: Long?,
    onMarkPaidClick: () -> Unit,
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
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = koraStringResource(id = R.string.dashboard_payment_cycle_title),
                style = MaterialTheme.typography.titleMedium
            )
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
        modifier = modifier.fillMaxWidth(),
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
                            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun AiAssistantCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Text(
                text = koraStringResource(id = R.string.dashboard_ai_insights_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    KoraTheme {
        DashboardScreenContent(
            currentRoute = KoraDestination.Dashboard.route,
            uiState = previewUiState,
            onNavigate = {},
            onNavigateToSettings = {},
            onNavigateToStudentList = {},
            onLogLessonClick = {},
            onMarkCurrentCycleAsPaid = {},
            currencyCode = "TRY"
        )
    }
}
