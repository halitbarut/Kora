package com.barutdev.kora.ui.screens.student_list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import java.util.Locale

@Composable
fun StudentEditDialog(
    showDialog: Boolean,
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, hourlyRate: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showDialog || student == null) return

    var fullName by rememberSaveable(student.id, showDialog) { mutableStateOf(student.fullName) }
    var hourlyRate by rememberSaveable(student.id, showDialog) {
        mutableStateOf(String.format(Locale.US, "%.2f", student.hourlyRate))
    }

    val isSaveEnabled = fullName.isNotBlank() && hourlyRate.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.edit_student_dialog_title))
        },
        text = {
            Column(modifier = modifier) {
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(text = koraStringResource(id = R.string.add_student_dialog_full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
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
}
