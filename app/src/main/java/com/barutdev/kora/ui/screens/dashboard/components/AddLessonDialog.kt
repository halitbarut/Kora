package com.barutdev.kora.ui.screens.dashboard.components

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
import kotlin.text.StringBuilder

@Composable
fun AddLessonDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (duration: String, notes: String) -> Unit
) {
    if (!showDialog) return

    var duration by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    val isSaveEnabled = duration.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true

    AlertDialog(
        onDismissRequest = {
            duration = ""
            notes = ""
            onDismiss()
        },
        title = {
            Text(text = koraStringResource(id = R.string.dashboard_add_lesson_dialog_title))
        },
        text = {
            Column {
                TextField(
                    value = duration,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        var decimalAdded = false
                        val sanitized = StringBuilder()
                        normalized.forEach { char ->
                            when {
                                char.isDigit() -> sanitized.append(char)
                                char == '.' && !decimalAdded -> {
                                    sanitized.append(char)
                                    decimalAdded = true
                                }
                            }
                        }
                        duration = sanitized.toString()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.dashboard_add_lesson_duration_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = koraStringResource(id = R.string.dashboard_add_lesson_notes_label)) },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(duration, notes)
                    duration = ""
                    notes = ""
                },
                enabled = isSaveEnabled
            ) {
                Text(text = koraStringResource(id = R.string.dialog_action_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    duration = ""
                    notes = ""
                    onDismiss()
                }
            ) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}
