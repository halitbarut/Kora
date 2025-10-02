package com.barutdev.kora.ui.screens.calendar

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.navigation.KoraDestination
import com.barutdev.kora.ui.theme.KoraTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.rememberCoroutineScope
import com.barutdev.kora.domain.model.Lesson
import com.barutdev.kora.domain.model.LessonStatus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


private data class CalendarNavItem(
    val destination: KoraDestination,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()

    CalendarScreenContent(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        studentName = studentName,
        studentId = viewModel.studentId,
        lessons = lessons,
        scheduleLesson = { epochMillis -> viewModel.scheduleLesson(epochMillis) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarScreenContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    studentName: String,
    studentId: Int,
    lessons: List<Lesson>,
    scheduleLesson: suspend (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    var currentMonth by remember { mutableStateOf(YearMonth.from(LocalDate.now())) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val displayedStudentName = if (studentName.isNotBlank()) {
        studentName
    } else {
        stringResource(id = R.string.dashboard_student_name)
    }
    val calendarLabel = stringResource(id = R.string.calendar_title)

    val navigationItems = remember {
        listOf(
            CalendarNavItem(
                destination = KoraDestination.Dashboard,
                icon = Icons.Outlined.Dashboard,
                labelRes = R.string.dashboard_title
            ),
            CalendarNavItem(
                destination = KoraDestination.Calendar,
                icon = Icons.Outlined.CalendarMonth,
                labelRes = R.string.calendar_title
            ),
            CalendarNavItem(
                destination = KoraDestination.Homework,
                icon = Icons.Outlined.Assignment,
                labelRes = R.string.homework_title
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val zoneId = remember { ZoneId.systemDefault() }
    val selectDayMessage = stringResource(id = R.string.calendar_select_day_message)
    val scheduledMessage = stringResource(id = R.string.calendar_lesson_scheduled_message)

    val lessonDates = remember(lessons, zoneId) {
        lessons
            .filter { lesson -> lesson.status != LessonStatus.CANCELLED }
            .map { lesson ->
                Instant.ofEpochMilli(lesson.date)
                    .atZone(zoneId)
                    .toLocalDate()
            }
            .toSet()
    }

    val hasStudentId = studentId >= 0

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = R.string.calendar_top_bar_title,
                            displayedStudentName,
                            calendarLabel
                        )
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val date = selectedDate
                    if (date != null) {
                        val epochMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                        coroutineScope.launch {
                            scheduleLesson(epochMillis)
                            snackbarHostState.showSnackbar(message = scheduledMessage)
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = selectDayMessage
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.calendar_add_lesson_fab_content_description)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val label = stringResource(id = item.labelRes)
                    val enabled = when (item.destination) {
                        KoraDestination.Dashboard,
                        KoraDestination.Calendar,
                        KoraDestination.Homework -> hasStudentId
                        else -> true
                    }
                    NavigationBarItem(
                        selected = currentRoute == item.destination.route,
                        onClick = {
                            if (!enabled || currentRoute == item.destination.route) {
                                return@NavigationBarItem
                            }
                            when (item.destination) {
                                KoraDestination.Dashboard -> onNavigate(
                                    KoraDestination.Dashboard.createRoute(studentId)
                                )
                                KoraDestination.Calendar -> onNavigate(
                                    KoraDestination.Calendar.createRoute(studentId)
                                )
                                KoraDestination.Homework -> onNavigate(
                                    KoraDestination.Homework.createRoute(studentId)
                                )
                                else -> onNavigate(item.destination.route)
                            }
                        },
                        enabled = enabled,
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
        MonthlyCalendarView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onPreviousMonth = {
                currentMonth = currentMonth.minusMonths(1)
                selectedDate = null
            },
            onNextMonth = {
                currentMonth = currentMonth.plusMonths(1)
                selectedDate = null
            },
            onDaySelected = { date ->
                selectedDate = date
            },
            lessonDates = lessonDates,
            locale = locale
        )
    }
}

@Composable
private fun MonthlyCalendarView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    lessonDates: Set<LocalDate>,
    locale: Locale
) {
    val monthName = remember(currentMonth, locale) {
        currentMonth.month.getDisplayName(TextStyle.FULL, locale)
    }
    val formattedMonthName = monthName.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
    val monthTitle = stringResource(
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

    Column(modifier = modifier) {
        CalendarHeader(
            monthTitle = monthTitle,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth
        )
        Spacer(modifier = Modifier.height(16.dp))
        DaysOfWeekRow(daysOfWeek = daysOfWeek, locale = locale)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(calendarDays.size) { index ->
                val date = calendarDays[index]
                if (date == null) {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                } else {
                    CalendarDayCell(
                        date = date,
                        isSelected = selectedDate == date,
                        hasLesson = lessonDates.contains(date),
                        onClick = { onDaySelected(date) }
                    )
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
                contentDescription = stringResource(id = R.string.calendar_previous_month_content_description)
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
                contentDescription = stringResource(id = R.string.calendar_next_month_content_description)
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
    hasLesson: Boolean,
    onClick: () -> Unit
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
        modifier = Modifier
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
            if (hasLesson) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
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
    val previewLessons = listOf(
        Lesson(
            id = 1,
            studentId = 1,
            date = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = LessonStatus.SCHEDULED,
            durationInHours = null,
            notes = null
        )
    )
    KoraTheme {
        CalendarScreenContent(
            currentRoute = KoraDestination.Calendar.route,
            onNavigate = {},
            studentName = "Elif Yılmaz",
            studentId = 1,
            lessons = previewLessons,
            scheduleLesson = { _ -> }
        )
    }
}
