package com.barutdev.kora.ui.screens.student_list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.util.koraStringResource
import java.util.Locale

@Composable
fun StudentEditDialog(
    showDialog: Boolean,
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, hourlyRate: String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showDialog || student == null) return

    var fullName by rememberSaveable(student.id, showDialog) { mutableStateOf(student.fullName) }
    var hourlyRate by rememberSaveable(student.id, showDialog) {
        mutableStateOf(String.format(Locale.US, "%.2f", student.hourlyRate))
    }
    var showDeleteConfirmation by rememberSaveable(student.id, showDialog) { mutableStateOf(false) }

    val isSaveEnabled = fullName.isNotBlank() && hourlyRate.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.edit_student_dialog_title))
        },
        text = {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = koraStringResource(id = R.string.edit_student_section_header),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(text = koraStringResource(id = R.string.add_student_dialog_full_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextField(
                        value = hourlyRate,
                        onValueChange = { newValue ->
                            val normalized = newValue.replace(',', '.')
                            if (normalized.count { it == '.' } <= 1 && normalized.all { it.isDigit() || it == '.' }) {
                                hourlyRate = normalized
                            }
                        },
                        label = { Text(text = koraStringResource(id = R.string.add_student_dialog_hourly_rate)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                HorizontalDivider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = koraStringResource(id = R.string.delete_student_section_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = koraStringResource(
                            id = R.string.delete_student_section_body,
                            student.fullName
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = koraStringResource(id = R.string.delete_student_button))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(fullName, hourlyRate)
                },
                enabled = isSaveEnabled
            ) {
                Text(text = koraStringResource(id = R.string.dialog_action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(text = koraStringResource(id = R.string.delete_student_confirmation_title))
            },
            text = {
                Text(
                    text = koraStringResource(
                        id = R.string.delete_student_confirmation_message,
                        student.fullName
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = koraStringResource(id = R.string.dialog_action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = koraStringResource(id = R.string.delete_student_confirmation_keep))
                }
            }
        )
    }
}
