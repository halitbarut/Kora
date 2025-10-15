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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
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
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.ui.navigation.FabConfig
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.preferences.LocalUserPreferences
import com.barutdev.kora.ui.screens.student_list.components.AddStudentDialog
import com.barutdev.kora.ui.screens.student_list.components.StudentEditDialog
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.util.formatCurrency
import java.util.Locale

@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDialog by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val isEditDialogVisible by viewModel.isEditStudentDialogVisible.collectAsStateWithLifecycle()
    val studentToEdit by viewModel.studentToEdit.collectAsStateWithLifecycle()
    val userPreferences = LocalUserPreferences.current

    val topBarTitle = koraStringResource(id = R.string.student_list_title)
    val addStudentDescription = koraStringResource(id = R.string.student_list_add_student_fab_content_description)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val topBarConfig = remember(topBarTitle) {
        TopBarConfig(title = topBarTitle)
    }
    val fabConfig = remember(
        addStudentDescription,
        containerColor,
        contentColor,
        onAddStudent,
        viewModel
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addStudentDescription,
            onClick = {
                viewModel.onAddStudentClick()
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
        onStudentClick = onStudentClick,
        onEditStudent = viewModel::onEditStudentClicked,
        currencyCode = userPreferences.currencyCode,
        modifier = modifier.fillMaxSize()
    )

    AddStudentDialog(
        showDialog = showDialog,
        onDismiss = viewModel::onAddStudentDismiss,
        onSave = viewModel::onSaveStudent,
        initialHourlyRate = uiState.defaultHourlyRate
    )

    StudentEditDialog(
        showDialog = isEditDialogVisible,
        student = studentToEdit,
        onDismiss = viewModel::onEditStudentDismiss,
        onSave = viewModel::onUpdateStudent,
        onDelete = viewModel::onDeleteStudent
    )
}


@Composable
private fun StudentListScreenContent(
    students: List<StudentWithDebt>,
    onStudentClick: (String) -> Unit,
    onEditStudent: (Student) -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    if (students.isEmpty()) {
        StudentListEmptyState(
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
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
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListEmptyScreenPreview() {
    KoraTheme {
        StudentListScreenContent(
            students = emptyList(),
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY"
        )
    }
}

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
