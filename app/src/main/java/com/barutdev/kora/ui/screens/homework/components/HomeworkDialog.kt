package com.barutdev.kora.ui.screens.homework.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barutdev.kora.util.koraStringResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Homework
import com.barutdev.kora.domain.model.HomeworkStatus
import com.barutdev.kora.ui.theme.LocalLocale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDialog(
    showDialog: Boolean,
    editingHomework: Homework?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, dueDate: Long, status: HomeworkStatus, performanceNotes: String?) -> Unit
) {
    if (!showDialog) return

    val locale = LocalLocale.current
    val zoneId = remember { ZoneId.systemDefault() }
    val previewFormatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }

    val editingId = editingHomework?.id

    var title by rememberSaveable(editingId) {
        mutableStateOf(editingHomework?.title.orEmpty())
    }
    var description by rememberSaveable(editingId) {
        mutableStateOf(editingHomework?.description.orEmpty())
    }
    var selectedDueDateMillis by rememberSaveable(editingId) {
        mutableStateOf(editingHomework?.dueDate)
    }
    var status by rememberSaveable(editingId) {
        mutableStateOf(editingHomework?.status ?: HomeworkStatus.PENDING)
    }
    var performanceNotes by rememberSaveable(editingId) {
        mutableStateOf(editingHomework?.performanceNotes.orEmpty())
    }
    var isStatusMenuExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }

    val isEditing = editingHomework != null
    val confirmLabel = if (isEditing) {
        koraStringResource(id = R.string.homework_dialog_update)
    } else {
        koraStringResource(id = R.string.homework_dialog_save)
    }
    val dialogTitle = if (isEditing) {
        koraStringResource(id = R.string.homework_dialog_edit_title)
    } else {
        koraStringResource(id = R.string.homework_dialog_add_title)
    }

    val isConfirmEnabled = title.isNotBlank() && selectedDueDateMillis != null

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = title,
                    onValueChange = { newValue ->
                        title = newValue
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.homework_dialog_title_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                TextField(
                    value = description,
                    onValueChange = { newValue ->
                        description = newValue
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.homework_dialog_description_label)) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
                // Read-only clickable date field that opens DatePickerDialog
                val dueDateText = selectedDueDateMillis?.let { millis ->
                    Instant.ofEpochMilli(millis)
                        .atZone(zoneId)
                        .toLocalDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                }.orEmpty()
                OutlinedTextField(
                    value = dueDateText,
                    onValueChange = { /* read-only */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePickerDialog = true },
                    label = { Text(text = koraStringResource(id = R.string.homework_dialog_due_date_label)) },
                    placeholder = { Text(text = koraStringResource(id = R.string.homework_dialog_due_date_hint)) },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = koraStringResource(id = R.string.homework_dialog_due_date_label)
                            )
                        }
                    }
                )
                ExposedDropdownMenuBox(
                    expanded = isStatusMenuExpanded,
                    onExpandedChange = { isStatusMenuExpanded = !isStatusMenuExpanded }
                ) {
                    TextField(
                        value = koraStringResource(id = statusLabelRes(status)),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text(text = koraStringResource(id = R.string.homework_dialog_status_label)) },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusMenuExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = isStatusMenuExpanded,
                        onDismissRequest = { isStatusMenuExpanded = false }
                    ) {
                        HomeworkStatus.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = koraStringResource(id = statusLabelRes(option))) },
                                onClick = {
                                    status = option
                                    isStatusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                TextField(
                    value = performanceNotes,
                    onValueChange = { newValue ->
                        performanceNotes = newValue
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.homework_dialog_performance_notes_label)) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
                selectedDueDateMillis?.let { millis ->
                    val readable = Instant.ofEpochMilli(millis)
                        .atZone(zoneId)
                        .toLocalDate()
                        .format(previewFormatter)
                    Text(text = koraStringResource(id = R.string.homework_due_date_label, readable))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dueDateMillis = selectedDueDateMillis ?: return@Button
                    onConfirm(
                        title,
                        description,
                        dueDateMillis,
                        status,
                        performanceNotes.ifBlank { null }
                    )
                },
                enabled = isConfirmEnabled
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = koraStringResource(id = R.string.homework_dialog_cancel))
            }
        }
    )

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDueDateMillis = millis
                            showDatePickerDialog = false
                        }
                    }
                ) {
                    Text(text = koraStringResource(id = R.string.dialog_action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(text = koraStringResource(id = R.string.dialog_action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun statusLabelRes(status: HomeworkStatus): Int = when (status) {
    HomeworkStatus.PENDING -> R.string.homework_status_pending
    HomeworkStatus.COMPLETED -> R.string.homework_status_completed
}
