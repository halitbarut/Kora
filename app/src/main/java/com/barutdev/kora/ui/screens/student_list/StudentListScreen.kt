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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.ui.screens.student_list.components.AddStudentDialog
import com.barutdev.kora.ui.theme.KoraTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val showDialog by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()

    StudentListScreenContent(
        students = students,
        onAddStudent = {
            viewModel.onAddStudentClick()
            onAddStudent()
        },
        onStudentClick = onStudentClick,
        modifier = modifier
    )

    AddStudentDialog(
        showDialog = showDialog,
        onDismiss = viewModel::onAddStudentDismiss,
        onSave = viewModel::onSaveStudent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentListScreenContent(
    students: List<Student>,
    onAddStudent: () -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.student_list_title))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudent,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(
                        id = R.string.student_list_add_student_fab_content_description
                    )
                )
            }
        }
    ) { innerPadding ->
        if (students.isEmpty()) {
            StudentListEmptyState(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(students) { student ->
                    StudentListItem(
                        student = student,
                        onStudentClick = onStudentClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                text = stringResource(id = R.string.student_list_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.student_list_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StudentListItem(
    student: Student,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onStudentClick(student.id.toString()) },
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
                    text = student.initials(),
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
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        id = R.string.student_list_amount_due,
                        student.formattedHourlyRate()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(
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

@Preview(showBackground = true)
@Composable
private fun StudentListScreenPreview() {
    KoraTheme {
        StudentListScreenContent(
            students = previewStudents,
            onAddStudent = {},
            onStudentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListEmptyScreenPreview() {
    KoraTheme {
        StudentListScreenContent(
            students = emptyList(),
            onAddStudent = {},
            onStudentClick = {}
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

private fun Student.formattedHourlyRate(): String = String.format(Locale.getDefault(), "%.2f TL", hourlyRate)
