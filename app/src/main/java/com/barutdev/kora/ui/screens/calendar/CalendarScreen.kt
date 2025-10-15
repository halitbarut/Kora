package com.barutdev.kora.ui.screens.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import com.barutdev.kora.ui.navigation.FabConfig
import com.barutdev.kora.ui.navigation.LocalKoraScaffoldController
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.screens.dashboard.components.LogLessonDialog
import com.barutdev.kora.ui.screens.common.StudentNameUiStatus
import com.barutdev.kora.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.ui.theme.StatusBlue
import com.barutdev.kora.ui.theme.StatusGreen
import com.barutdev.kora.ui.theme.StatusRed
import com.barutdev.kora.ui.theme.StatusYellow
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch


@Composable
fun CalendarScreen(
    onNavigateToStudentList: () -> Unit,
    expectedStudentId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(
        key = expectedStudentId?.let { "calendar-$it" } ?: "calendar-default"
    )
) {
    val scaffoldController = LocalKoraScaffoldController.current
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isLogLessonDialogVisible by viewModel.isLogLessonDialogVisible.collectAsStateWithLifecycle()
    val lessonToLog by viewModel.lessonToLog.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val zoneId = remember { ZoneId.systemDefault() }
    val snackbarHostState = scaffoldController.snackbarHostState

    LaunchedEffect(expectedStudentId) {
        Log.d("CalendarScreen", "Composing for expectedStudentId=$expectedStudentId")
    }
    LaunchedEffect(viewModel.studentId) {
        viewModel.studentId?.let { id ->
            Log.d("CalendarScreen", "Rendering calendar for studentId=$id")
        }
    }

    val studentNameStatus = deriveStudentNameUiStatus(
        studentName = studentName,
        hasStudentReference = viewModel.hasStudentReference
    )
    val calendarLabel = koraStringResource(id = R.string.calendar_title)
    val resolvedStudentName = studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    } ?: calendarLabel
    val topBarTitle = if (studentNameStatus == StudentNameUiStatus.Ready && studentName.isNotBlank()) {
        koraStringResource(
            id = R.string.calendar_top_bar_title,
            resolvedStudentName,
            calendarLabel
        )
    } else {
        calendarLabel
    }
    val navigateToListDescription = koraStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val addLessonDescription = koraStringResource(
        id = R.string.calendar_add_lesson_fab_content_description
    )
    val scheduledMessage = koraStringResource(id = R.string.calendar_lesson_scheduled_message)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    LogLessonDialog(
        showDialog = isLogLessonDialogVisible,
        lesson = lessonToLog,
        onDismiss = viewModel::dismissLogLessonDialog,
        onComplete = { duration, notes ->
            viewModel.onLogLessonComplete(duration, notes)
        },
        onMarkNotDone = { notes ->
            viewModel.onLogLessonMarkNotDone(notes)
        }
    )

    val topBarConfig = remember(
        topBarTitle,
        navigateToListDescription,
        onNavigateToStudentList
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigateToListDescription,
                onClick = onNavigateToStudentList
            )
        )
    }
    val fabConfig = remember(
        selectedDate,
        addLessonDescription,
        containerColor,
        contentColor,
        scheduledMessage,
        coroutineScope,
        viewModel,
        zoneId,
        snackbarHostState
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addLessonDescription,
            onClick = {
                val epochMillis = selectedDate
                    .atStartOfDay(zoneId)
                    .toInstant()
                    .toEpochMilli()
                coroutineScope.launch {
                    viewModel.scheduleLesson(epochMillis)
                    snackbarHostState.showSnackbar(message = scheduledMessage)
                }
            },
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    CalendarScreenContent(
        modifier = modifier.fillMaxSize(),
        currentMonth = currentMonth,
        selectedDate = selectedDate,
        lessons = lessons,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onSelectDate = viewModel::onSelectDate,
        onLogLessonClick = viewModel::onLogLessonClicked
    )
}

@Composable
private fun CalendarScreenContent(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onLogLessonClick: (Lesson) -> Unit
) {
    val currentLocale = LocalLocale.current
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zoneId) }

    val lessonsByDate = remember(lessons, zoneId) {
        lessons.groupBy { lesson ->
            Instant.ofEpochMilli(lesson.date)
                .atZone(zoneId)
                .toLocalDate()
        }
    }
    val selectedDateLessons = remember(selectedDate, lessonsByDate) {
        lessonsByDate[selectedDate].orEmpty()
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        MonthlyCalendarView(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            today = today,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onDaySelected = onSelectDate,
            lessonsByDate = lessonsByDate,
            locale = currentLocale
        )
        LessonDetailsSection(
            selectedDate = selectedDate,
            lessons = selectedDateLessons,
            today = today,
            locale = currentLocale,
            onLessonActionClick = onLogLessonClick
        )
    }
}


@Composable
private fun MonthlyCalendarView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    lessonsByDate: Map<LocalDate, List<Lesson>>,
    locale: Locale
) {
    val monthName = remember(currentMonth, locale) {
        currentMonth.month.getDisplayName(TextStyle.FULL, locale)
    }
    val formattedMonthName = monthName.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
    val monthTitle = koraStringResource(
        id = R.string.calendar_month_year_title,
        formattedMonthName,
        currentMonth.year
    )

    val daysOfWeek = remember(locale) { daysOfWeekFromLocale(locale) }
    val weekFields = remember(locale) { WeekFields.of(locale) }
    val daysInWeek = DayOfWeek.values().size
    val firstOfMonth = remember(currentMonth) { currentMonth.atDay(1) }
    val leadingDays = ((firstOfMonth.dayOfWeek.value - weekFields.firstDayOfWeek.value) + daysInWeek) % daysInWeek
    val totalDays = currentMonth.lengthOfMonth()
    val trailingDays = (daysInWeek - (leadingDays + totalDays) % daysInWeek) % daysInWeek
    val calendarDays = remember(currentMonth, leadingDays, trailingDays) {
        buildList {
            repeat(leadingDays) { add(null) }
            repeat(totalDays) { dayIndex -> add(currentMonth.atDay(dayIndex + 1)) }
            repeat(trailingDays) { add(null) }
        }
    }
    val calendarRows = remember(calendarDays) {
        calendarDays.chunked(daysInWeek)
    }

    Column(modifier = modifier) {
        CalendarHeader(
            monthTitle = monthTitle,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Spacer(modifier = Modifier.height(16.dp))
        DaysOfWeekRow(daysOfWeek = daysOfWeek, locale = locale)
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            calendarRows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            val lessonsForDate = lessonsByDate[date].orEmpty()
                            CalendarDayCell(
                                date = date,
                                isSelected = selectedDate == date,
                                indicatorColor = statusDotColorForLessons(
                                    lessons = lessonsForDate,
                                    date = date,
                                    today = today
                                ),
                                onClick = { onDaySelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    monthTitle: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = koraStringResource(id = R.string.calendar_previous_month_content_description)
            )
        }
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = koraStringResource(id = R.string.calendar_next_month_content_description)
            )
        }
    }
}

@Composable
private fun DaysOfWeekRow(
    daysOfWeek: List<DayOfWeek>,
    locale: Locale
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        daysOfWeek.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    indicatorColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textAlign = TextAlign.Center
            )
            if (indicatorColor != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}

private fun statusDotColorForLessons(
    lessons: List<Lesson>,
    date: LocalDate,
    today: LocalDate
): Color? {
    if (lessons.isEmpty()) return null
    return when {
        lessons.any { it.status == LessonStatus.PAID } -> StatusGreen
        lessons.any { it.status == LessonStatus.COMPLETED } -> StatusYellow
        lessons.any { it.status == LessonStatus.SCHEDULED } -> if (date.isBefore(today)) {
            StatusRed
        } else {
            StatusBlue
        }
        lessons.any { it.status == LessonStatus.CANCELLED } -> StatusRed
        else -> null
    }
}

private data class LessonStatusDisplay(
    val text: String,
    val color: Color
)

@Composable
private fun LessonDetailsSection(
    selectedDate: LocalDate,
    lessons: List<Lesson>,
    today: LocalDate,
    locale: Locale,
    onLessonActionClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }
    val formattedSelectedDate = remember(selectedDate, dateFormatter) {
        selectedDate.format(dateFormatter)
    }
    val lessonsSorted = remember(lessons) {
        lessons.sortedBy { lesson -> lesson.date }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = koraStringResource(
                id = R.string.calendar_day_details_title,
                formattedSelectedDate
            ),
            style = MaterialTheme.typography.titleMedium
        )
        if (lessonsSorted.isEmpty()) {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = koraStringResource(id = R.string.calendar_no_lessons_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                lessonsSorted.forEach { lesson ->
                    LessonDetailCard(
                        lesson = lesson,
                        dateFormatter = dateFormatter,
                        today = today,
                        locale = locale,
                        onActionClick = onLessonActionClick
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonDetailCard(
    lesson: Lesson,
    dateFormatter: DateTimeFormatter,
    today: LocalDate,
    locale: Locale,
    onActionClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessonDate = remember(lesson.date) {
        Instant.ofEpochMilli(lesson.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    val formattedDate = remember(lessonDate, dateFormatter) {
        lessonDate.format(dateFormatter)
    }
    val statusDisplay = lessonStatusDisplay(lesson, lessonDate, today)
    val durationText = remember(lesson.durationInHours, locale) {
        lesson.durationInHours?.let { duration ->
            NumberFormat.getNumberInstance(locale).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(duration)
        }
    }
    val actionTextRes = if (
        lesson.status == LessonStatus.SCHEDULED && lessonDate.isBefore(today)
    ) {
        R.string.calendar_lesson_action_log_details
    } else {
        R.string.calendar_lesson_action_edit
    }

    Card(
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = koraStringResource(
                    id = R.string.calendar_lesson_details_title,
                    formattedDate
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = koraStringResource(
                    id = R.string.calendar_lesson_details_status,
                    statusDisplay.text
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = statusDisplay.color
            )
            durationText?.let { duration ->
                Text(
                    text = koraStringResource(
                        id = R.string.calendar_lesson_details_duration,
                        duration
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            lesson.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = koraStringResource(
                        id = R.string.calendar_lesson_details_notes,
                        notes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { onActionClick(lesson) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = koraStringResource(id = actionTextRes))
            }
        }
    }
}

@Composable
private fun lessonStatusDisplay(
    lesson: Lesson,
    lessonDate: LocalDate,
    today: LocalDate
): LessonStatusDisplay {
    val statusTextRes = when (lesson.status) {
        LessonStatus.PAID -> R.string.calendar_status_paid
        LessonStatus.COMPLETED -> R.string.calendar_status_completed
        LessonStatus.SCHEDULED -> if (lessonDate.isBefore(today)) {
            R.string.calendar_status_scheduled_needs_logging
        } else {
            R.string.calendar_status_scheduled
        }
        LessonStatus.CANCELLED -> R.string.calendar_status_cancelled
    }
    val color = when (lesson.status) {
        LessonStatus.PAID -> StatusGreen
        LessonStatus.COMPLETED -> StatusYellow
        LessonStatus.SCHEDULED -> if (lessonDate.isBefore(today)) {
            StatusRed
        } else {
            StatusBlue
        }
        LessonStatus.CANCELLED -> StatusRed
    }
    return LessonStatusDisplay(
        text = koraStringResource(id = statusTextRes),
        color = color
    )
}

private fun daysOfWeekFromLocale(locale: Locale): List<DayOfWeek> {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val days = DayOfWeek.values()
    val firstIndex = firstDay.ordinal
    return List(days.size) { index ->
        days[(firstIndex + index) % days.size]
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val previewLessons = listOf(
        Lesson(
            id = 1,
            studentId = 1,
            date = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null
        ),
        Lesson(
            id = 2,
            studentId = 1,
            date = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.COMPLETED,
            durationInHours = 1.5,
            notes = "Worked on algebra problems."
        ),
        Lesson(
            id = 3,
            studentId = 1,
            date = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.PAID,
            durationInHours = 2.0,
            notes = null
        )
    )
    val selectedDate = today
    val currentMonth = YearMonth.from(selectedDate)
    KoraTheme {
        CalendarScreenContent(
            modifier = Modifier.fillMaxWidth(),
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            lessons = previewLessons,
            onPreviousMonth = {},
            onNextMonth = {},
            onSelectDate = {},
            onLogLessonClick = {}
        )
    }
}
