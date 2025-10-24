package com.barutdev.kora.ui.screens.student_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.screens.student_profile.StudentProfileEvent
import com.barutdev.kora.ui.screens.student_profile.StudentProfileUiState
import com.barutdev.kora.util.LocalMessageNotifier
import com.barutdev.kora.util.formatCurrency
import com.barutdev.kora.util.koraStringResource

@Composable
fun EditStudentProfileScreen(
    onBack: () -> Unit,
    onProfileSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditStudentProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageNotifier = LocalMessageNotifier.current
    val successMessage = koraStringResource(id = R.string.student_profile_save_success)
    val errorMessage = koraStringResource(id = R.string.student_profile_save_error)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                StudentProfileEvent.ProfileSaved -> {
                    messageNotifier.showMessage(successMessage)
                    onProfileSaved()
                }
                StudentProfileEvent.SaveFailed -> {
                    messageNotifier.showMessage(errorMessage)
                }
                StudentProfileEvent.StudentMissing -> {
                    messageNotifier.showMessage(errorMessage)
                    onBack()
                }
            }
        }
    }

    StudentProfileContent(
        title = koraStringResource(id = R.string.student_profile_title),
        state = uiState,
        onBack = onBack,
        onFullNameChanged = viewModel::onFullNameChanged,
        onParentNameChanged = viewModel::onParentNameChanged,
        onParentContactChanged = viewModel::onParentContactChanged,
        onHourlyRateChanged = viewModel::onHourlyRateChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::onSave,
        modifier = modifier
    )
}

@Composable
fun StudentProfileContent(
    title: String,
    state: StudentProfileUiState,
    onBack: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onParentNameChanged: (String) -> Unit,
    onParentContactChanged: (String) -> Unit,
    onHourlyRateChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backContentDescription = koraStringResource(id = R.string.student_profile_back_content_description)
    val optionalLabel = koraStringResource(id = R.string.student_profile_optional_label)
    val requiredIndicator = koraStringResource(id = R.string.student_profile_required_indicator)
    val topBarConfig = remember(onBack, title, backContentDescription) {
        TopBarConfig(
            title = title,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = backContentDescription,
                onClick = onBack
            )
        )
    }

    ScreenScaffoldConfig(topBarConfig = topBarConfig)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val scrollState = rememberScrollState()
            val defaultRateText = remember(state.defaultHourlyRate, state.currencyCode) {
                formatCurrency(state.defaultHourlyRate, state.currencyCode)
            }
            val fullNameLabel = koraStringResource(id = R.string.student_profile_full_name_label)
            val parentNameLabel = koraStringResource(id = R.string.student_profile_parent_name_label)
            val parentContactLabel = koraStringResource(id = R.string.student_profile_parent_contact_label)
            val hourlyRateLabel = koraStringResource(id = R.string.student_profile_hourly_rate_label)
            val hourlyRateHelper = koraStringResource(id = R.string.student_profile_hourly_rate_helper, defaultRateText)
            val notesLabel = koraStringResource(id = R.string.student_profile_notes_label)
            val saveLabel = koraStringResource(id = R.string.student_profile_save)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 32.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (state.isSaving) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = onFullNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileFullName"),
                    label = {
                        FieldLabel(
                            text = fullNameLabel,
                            requiredIndicator = requiredIndicator,
                            optionalText = null
                        )
                    },
                    isError = state.fullNameError != null,
                    supportingText = state.fullNameError?.let { errorRes ->
                        { Text(text = koraStringResource(id = errorRes)) }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                OutlinedTextField(
                    value = state.parentName,
                    onValueChange = onParentNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileParentName"),
                    label = {
                        FieldLabel(
                            text = parentNameLabel,
                            requiredIndicator = null,
                            optionalText = optionalLabel
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                OutlinedTextField(
                    value = state.parentContact,
                    onValueChange = onParentContactChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileParentContact"),
                    label = {
                        FieldLabel(
                            text = parentContactLabel,
                            requiredIndicator = null,
                            optionalText = optionalLabel
                        )
                    },
                    singleLine = true,
                    enabled = !state.isSaving
                )

                OutlinedTextField(
                    value = state.hourlyRateInput,
                    onValueChange = onHourlyRateChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileHourlyRate"),
                    label = {
                        FieldLabel(
                            text = hourlyRateLabel,
                            requiredIndicator = null,
                            optionalText = optionalLabel
                        )
                    },
                    isError = state.hourlyRateError != null,
                    supportingText = {
                        val errorRes = state.hourlyRateError
                        if (errorRes != null) {
                            Text(
                                text = koraStringResource(id = errorRes),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = hourlyRateHelper,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !state.isSaving
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileNotes"),
                    label = {
                        FieldLabel(
                            text = notesLabel,
                            requiredIndicator = null,
                            optionalText = optionalLabel
                        )
                    },
                    enabled = !state.isSaving,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    minLines = 4,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("StudentProfileSaveButton")
                ) {
                    Text(text = saveLabel)
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    requiredIndicator: String?,
    optionalText: String?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        requiredIndicator?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        optionalText?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentProfileContentPreview() {
    KoraTheme {
        StudentProfileContent(
            title = "Edit Student",
            state = StudentProfileUiState(
                fullName = "Alice Johnson",
                parentName = "Dana Johnson",
                parentContact = "dana@example.com",
                hourlyRateInput = "90",
                notes = "Prefers morning sessions.",
                defaultHourlyRate = 80.0,
                currencyCode = "USD",
                canSave = true,
                isLoading = false
            ),
            onBack = {},
            onFullNameChanged = {},
            onParentNameChanged = {},
            onParentContactChanged = {},
            onHourlyRateChanged = {},
            onNotesChanged = {},
            onSave = {}
        )
    }
}
