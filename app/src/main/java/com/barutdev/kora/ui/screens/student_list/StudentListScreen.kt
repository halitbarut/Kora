package com.barutdev.kora.ui.screens.student_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.ui.navigation.FabConfig
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.navigation.TopBarAction as NavigationTopBarAction
import com.barutdev.kora.ui.preferences.LocalUserPreferences
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.util.formatCurrency
import com.barutdev.kora.util.koraStringResource
import java.util.Locale

@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onStudentClick: (String) -> Unit,
    onEditStudentProfile: (Int) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userPreferences = LocalUserPreferences.current

    val topBarTitle = koraStringResource(id = R.string.student_list_title)
    val addStudentDescription = koraStringResource(id = R.string.student_list_add_student_fab_content_description)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val topBarActionDefinitions = remember(
        onNavigateToReports,
        onNavigateToSettings
    ) {
        listOf(
            TopBarAction(
                icon = Icons.Outlined.BarChart,
                descriptionResId = R.string.student_list_reports_action_description,
                onClick = onNavigateToReports
            ),
            TopBarAction(
                icon = Icons.Filled.Settings,
                descriptionResId = R.string.dashboard_settings_icon_description,
                onClick = onNavigateToSettings
            )
        )
    }

    val topBarActions = topBarActionDefinitions.map { action ->
        NavigationTopBarAction(
            icon = action.icon,
            contentDescription = koraStringResource(id = action.descriptionResId),
            onClick = action.onClick
        )
    }

    val topBarConfig = remember(topBarTitle, topBarActions) {
        TopBarConfig(
            title = topBarTitle,
            actions = topBarActions
        )
    }
    val fabConfig = remember(
        addStudentDescription,
        containerColor,
        contentColor,
        onAddStudent
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addStudentDescription,
            onClick = {
                onAddStudent()
            },
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    StudentListScreenContent(
        students = uiState.students,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearchQuery,
        onStudentClick = onStudentClick,
        onEditStudent = { student -> onEditStudentProfile(student.id) },
        currencyCode = userPreferences.currencyCode,
        isSearchActive = uiState.isSearchActive,
        hasAnyStudents = uiState.hasAnyStudents,
        isLoading = uiState.isLoading,
        modifier = modifier.fillMaxSize()
    )
}


@Composable
private fun StudentListScreenContent(
    students: List<StudentWithDebt>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onStudentClick: (String) -> Unit,
    onEditStudent: (Student) -> Unit,
    currencyCode: String,
    isSearchActive: Boolean,
    hasAnyStudents: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        StudentListLoadingState(
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (!hasAnyStudents) {
        StudentListEmptyState(
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val showNoResults = isSearchActive && students.isEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            StudentListSearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearQuery = onClearSearch,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (showNoResults) {
            item {
                StudentListNoResultsState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }
        } else {
            items(
                items = students,
                key = { it.student.id }
            ) { studentWithDebt ->
                StudentListItem(
                    student = studentWithDebt,
                    currencyCode = currencyCode,
                    onStudentClick = onStudentClick,
                    onEditClick = onEditStudent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StudentListSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = koraStringResource(id = R.string.student_list_search_placeholder)
    val clearContentDescription = koraStringResource(id = R.string.student_list_search_clear_content_description)

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.testTag("StudentListSearchField"),
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = clearContentDescription
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
    )
}

@Composable
private fun StudentListNoResultsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = koraStringResource(id = R.string.student_list_search_no_results_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Composable
private fun StudentListLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = koraStringResource(id = R.string.student_list_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudentListEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = koraStringResource(id = R.string.student_list_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = koraStringResource(id = R.string.student_list_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StudentListItem(
    student: StudentWithDebt,
    currencyCode: String,
    onStudentClick: (String) -> Unit,
    onEditClick: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val studentDetails = student.student
    val formattedDebt by remember(student.currentDebt, currencyCode) {
        derivedStateOf { formatCurrency(student.currentDebt, currencyCode) }
    }
    val studentInitials by remember(studentDetails.fullName) {
        derivedStateOf { studentDetails.initials() }
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onStudentClick(studentDetails.id.toString()) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentInitials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = studentDetails.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = koraStringResource(
                        id = R.string.student_list_amount_due,
                        formattedDebt
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onEditClick(studentDetails) }) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = koraStringResource(
                        id = R.string.student_list_edit_student_content_description
                    ),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = koraStringResource(
                    id = R.string.student_list_list_item_trailing_icon_content_description
                ),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private val previewStudents = listOf(
    Student(id = 1, fullName = "Elif Yılmaz", hourlyRate = 360.0),
    Student(id = 2, fullName = "Ahmet Demir", hourlyRate = 240.0),
    Student(id = 3, fullName = "Ayşe Kaya", hourlyRate = 180.0)
)

private val previewStudentsWithDebt = previewStudents.mapIndexed { index, student ->
    StudentWithDebt(
        student = student,
        currentDebt = (index + 1) * 150.0
    )
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenPreview() {
    KoraTheme {
        StudentListScreenContent(
            students = previewStudentsWithDebt,
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY",
            isSearchActive = false,
            hasAnyStudents = true,
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListEmptyScreenPreview() {
    KoraTheme {
        StudentListScreenContent(
            students = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY",
            isSearchActive = false,
            hasAnyStudents = false,
            isLoading = false
        )
    }
}

private data class TopBarAction(
    val icon: ImageVector,
    @StringRes val descriptionResId: Int,
    val onClick: () -> Unit
)

private fun Student.initials(): String = fullName
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString(separator = "") { part ->
        part.take(1).uppercase(Locale.getDefault())
    }
    .ifEmpty {
        fullName.take(2).uppercase(Locale.getDefault())
    }
