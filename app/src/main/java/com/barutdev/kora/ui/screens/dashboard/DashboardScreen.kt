package com.barutdev.kora.ui.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.navigation.KoraDestination
import com.barutdev.kora.ui.screens.dashboard.components.AddLessonDialog
import com.barutdev.kora.ui.theme.KoraTheme
import java.text.NumberFormat

private data class DashboardNavItem(
    val destination: KoraDestination,
    val icon: ImageVector,
    @StringRes val labelRes: Int
)

@Composable
fun DashboardScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddLessonDialog(
        showDialog = uiState.isAddLessonDialogVisible,
        onDismiss = viewModel::dismissAddLessonDialog,
        onSave = { duration, notes ->
            viewModel.onSaveLesson(duration, notes)
        }
    )

    DashboardScreenContent(
        currentRoute = currentRoute,
        uiState = uiState,
        onNavigate = onNavigate,
        onNavigateToSettings = onNavigateToSettings,
        onAddLessonClick = viewModel::showAddLessonDialog,
        modifier = modifier
    )
}

@Composable
private fun DashboardScreenContent(
    currentRoute: String?,
    uiState: DashboardUiState,
    onNavigate: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddLessonClick: () -> Unit,
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
        stringResource(id = R.string.dashboard_student_name)
    }
    val studentId = uiState.studentId

    Scaffold(
        modifier = modifier,
        topBar = {
            DashboardTopBar(
                studentName = studentName,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val label = stringResource(id = item.labelRes)
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
            onAddLessonClick = onAddLessonClick,
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
    onNavigateToSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.dashboard_student_label, studentName),
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.dashboard_settings_icon_description)
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
    onAddLessonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaymentTrackingCard(
            totalHours = totalHours,
            hourlyRate = hourlyRate,
            totalAmountDue = totalAmountDue
        )
        UpcomingLessonsCard(
            onAddLessonClick = onAddLessonClick
        )
        AiAssistantCard()
    }
}

@Composable
private fun PaymentTrackingCard(
    totalHours: Double,
    hourlyRate: Double,
    totalAmountDue: Double,
    modifier: Modifier = Modifier
) {
    val amountText = remember(totalAmountDue) {
        NumberFormat.getCurrencyInstance().format(totalAmountDue)
    }
    val hourlyRateText = remember(hourlyRate) {
        NumberFormat.getCurrencyInstance().format(hourlyRate)
    }
    val hoursText = remember(totalHours) {
        NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(totalHours)
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
                text = stringResource(id = R.string.dashboard_payment_cycle_title),
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        id = R.string.dashboard_payment_amount_value,
                        amountText
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(
                        id = R.string.dashboard_payment_rate_info,
                        hoursText,
                        hourlyRateText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(id = R.string.dashboard_payment_since_last),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.dashboard_payment_mark_paid))
            }
        }
    }
}

@Composable
private fun UpcomingLessonsCard(
    onAddLessonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = stringArrayResource(id = R.array.dashboard_upcoming_lessons)
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = stringResource(id = R.string.dashboard_upcoming_lessons_title),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAddLessonClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.dashboard_add_lesson_button_content_description)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lessons.forEach { lesson ->
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
                            text = lesson,
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                    text = stringResource(id = R.string.dashboard_ai_insights_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(id = R.string.dashboard_ai_insights_body),
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
        totalAmountDue = 360.0
    )
    KoraTheme {
        DashboardScreenContent(
            currentRoute = KoraDestination.Dashboard.route,
            uiState = previewUiState,
            onNavigate = {},
            onNavigateToSettings = {},
            onAddLessonClick = {}
        )
    }
}
