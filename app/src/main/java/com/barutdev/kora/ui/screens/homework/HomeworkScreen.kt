package com.barutdev.kora.ui.screens.homework

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.ui.navigation.FabConfig
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.screens.common.StudentNameUiStatus
import com.barutdev.kora.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.kora.ui.screens.homework.components.HomeworkDialog
import com.barutdev.kora.ui.theme.KoraAnimationSpecs
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.ui.theme.StatusGreen
import com.barutdev.kora.ui.theme.StatusYellow
import com.barutdev.kora.ui.model.AiInsightsUiState
import com.barutdev.kora.ui.model.AiStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HomeworkScreen(
    onNavigateToStudentList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeworkViewModel = hiltViewModel()
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val homeworkList by viewModel.homework.collectAsStateWithLifecycle()
    val isDialogVisible by viewModel.isDialogVisible.collectAsStateWithLifecycle()
    val editingHomework by viewModel.editingHomework.collectAsStateWithLifecycle()
    val aiInsightsState by viewModel.aiInsightsState.collectAsStateWithLifecycle()
    val locale = LocalLocale.current

    LaunchedEffect(viewModel.studentId, locale) {
        if (viewModel.hasStudentReference) {
            viewModel.ensureAiInsights(locale)
        }
    }

    HomeworkDialog(
        showDialog = isDialogVisible,
        editingHomework = editingHomework,
        onDismiss = viewModel::dismissHomeworkDialog,
        onConfirm = { title, description, dueDate, status, performanceNotes ->
            viewModel.onSubmitHomework(
                title = title,
                description = description,
                dueDate = dueDate,
                status = status,
                performanceNotes = performanceNotes
            )
        }
    )

    val studentNameStatus = deriveStudentNameUiStatus(
        studentName = studentName,
        hasStudentReference = viewModel.hasStudentReference
    )
    val homeworkLabel = koraStringResource(id = R.string.homework_title)
    val resolvedStudentName = studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    }
    val topBarTitle = resolvedStudentName?.let {
        koraStringResource(
            id = R.string.homework_top_bar_title,
            it,
            homeworkLabel
        )
    } ?: homeworkLabel
    val navigateDescription = koraStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val addHomeworkDescription = koraStringResource(id = R.string.homework_add_fab_content_description)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val displayStudentName = studentName.takeIf { studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank() }

    val topBarConfig = remember(
        topBarTitle,
        navigateDescription,
        onNavigateToStudentList
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigateDescription,
                onClick = onNavigateToStudentList
            )
        )
    }
    val fabConfig = remember(
        addHomeworkDescription,
        containerColor,
        contentColor,
        viewModel
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addHomeworkDescription,
            onClick = viewModel::showAddHomeworkDialog,
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    HomeworkScreenContent(
        modifier = modifier.fillMaxSize(),
        studentName = displayStudentName,
        homeworkList = homeworkList,
        aiInsightsState = aiInsightsState,
        onGenerateAiInsights = { viewModel.retryAiInsights(locale) },
        onHomeworkClick = viewModel::showEditHomeworkDialog
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeworkScreenContent(
    modifier: Modifier = Modifier,
    studentName: String?,
    homeworkList: List<Homework>,
    aiInsightsState: AiInsightsUiState,
    onGenerateAiInsights: () -> Unit,
    onHomeworkClick: (Homework) -> Unit
) {
    val locale = LocalLocale.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AiAssistantCard(
                studentName = studentName,
                aiState = aiInsightsState,
                onGenerateAgain = onGenerateAiInsights,
                modifier = Modifier.animateItem(placementSpec = KoraAnimationSpecs.itemPlacementSpec)
            )
        }
        item {
            Text(
                text = koraStringResource(id = R.string.homework_assignments_section_title),
                style = MaterialTheme.typography.titleMedium
            )
        }
        if (homeworkList.isEmpty()) {
            item {
                Text(
                    text = koraStringResource(id = R.string.homework_empty_state_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(homeworkList, key = { it.id }) { homework ->
                HomeworkListItem(
                    homework = homework,
                    locale = locale,
                    onClick = onHomeworkClick,
                    modifier = Modifier.animateItem(placementSpec = KoraAnimationSpecs.itemPlacementSpec)
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AiAssistantCard(
    studentName: String?,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = koraStringResource(id = R.string.homework_ai_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            AnimatedContent(
                targetState = aiState,
                transitionSpec = { KoraAnimationSpecs.cardContentTransform() },
                label = "homework_ai_card"
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
                            val message = state.messageRes?.let { resId ->
                                koraStringResource(id = resId)
                            } ?: koraStringResource(id = R.string.ai_generic_error_message)
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
                            val message = state.messageRes?.let { resId ->
                                when (resId) {
                                    R.string.homework_ai_no_data_message ->
                                        koraStringResource(
                                            id = resId,
                                            studentName ?: ""
                                        )
                                    else -> koraStringResource(id = resId)
                                }
                            } ?: koraStringResource(id = R.string.ai_generic_no_data_message)
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

@Composable
private fun HomeworkListItem(
    homework: Homework,
    locale: Locale,
    onClick: (Homework) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val dueDateText = remember(homework.dueDate, locale) {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        Instant.ofEpochMilli(homework.dueDate)
            .atZone(zoneId)
            .toLocalDate()
            .format(formatter)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = KoraAnimationSpecs.contentSizeSpec)
            .clickable { onClick(homework) },
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = koraStringResource(id = R.string.homework_due_date_label, dueDateText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            StatusBadge(status = homework.status)
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: HomeworkStatus, modifier: Modifier = Modifier) {
    val (labelRes, color) = when (status) {
        HomeworkStatus.PENDING -> R.string.homework_status_pending to StatusYellow
        HomeworkStatus.COMPLETED -> R.string.homework_status_completed to StatusGreen
    }
    val text = koraStringResource(id = labelRes)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeworkScreenPreview() {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val homeworkList = listOf(
        Homework(
            id = 1,
            studentId = 1,
            title = "Trigonometry Worksheet",
            description = "Complete problems 1-20",
            creationDate = today.minusDays(3).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            dueDate = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = HomeworkStatus.COMPLETED,
            performanceNotes = "Excellent work"
        ),
        Homework(
            id = 2,
            studentId = 1,
            title = "Calculus Problems",
            description = "Review integrals",
            creationDate = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            dueDate = today.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = HomeworkStatus.PENDING,
            performanceNotes = null
        )
    )
    val previewAiState = AiInsightsUiState(
        status = AiStatus.Success,
        insight = "Elif completed her recent assignments accurately. Introduce mixed-problem sets to reinforce multi-step reasoning."
    )
    KoraTheme {
        HomeworkScreenContent(
            studentName = "Elif Yılmaz",
            homeworkList = homeworkList,
            aiInsightsState = previewAiState,
            onGenerateAiInsights = {},
            onHomeworkClick = {},
            modifier = Modifier
        )
    }
}
