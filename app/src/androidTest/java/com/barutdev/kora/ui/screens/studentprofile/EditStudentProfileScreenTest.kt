package com.barutdev.kora.ui.screens.studentprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.barutdev.kora.R
import com.barutdev.kora.ui.screens.student_profile.StudentProfileContent
import com.barutdev.kora.ui.screens.student_profile.StudentProfileSnapshot
import com.barutdev.kora.ui.screens.student_profile.StudentProfileUiState
import com.barutdev.kora.ui.screens.student_profile.withComputedSaveState
import com.barutdev.kora.ui.theme.KoraTheme
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditStudentProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullNameValidationDisablesAndEnablesSave() {
        composeRule.setContent {
            KoraTheme {
                val initialFullName = "Alice Johnson"
                val initialParentName = ""
                val initialParentContact = ""
                val initialNotes = ""
                val initialHourlyRateInput = ""
                var formState by remember {
                    mutableStateOf(
                        StudentProfileUiState(
                            isLoading = false,
                            fullName = initialFullName,
                            parentName = initialParentName,
                            parentContact = initialParentContact,
                            notes = initialNotes,
                            hourlyRateInput = initialHourlyRateInput,
                            canSave = false
                        )
                    )
                }
                var saveTriggered by remember { mutableStateOf(false) }
                fun recalcCanSave(nextState: StudentProfileUiState): Boolean {
                    val trimmedName = nextState.fullName.trim()
                    if (trimmedName.isEmpty()) return false
                    val hasChanges =
                        trimmedName != initialFullName ||
                            nextState.parentName.trim() != initialParentName ||
                            nextState.parentContact.trim() != initialParentContact ||
                            nextState.notes.trim() != initialNotes ||
                            nextState.hourlyRateInput.trim().trimEnd('.') != initialHourlyRateInput
                    return hasChanges &&
                        nextState.hourlyRateError == null &&
                        !nextState.isSaving &&
                        !nextState.isLoading
                }

                StudentProfileContent(
                    title = getString(R.string.student_profile_title),
                    state = formState,
                    onBack = {},
                    onFullNameChanged = { value ->
                        val trimmed = value.trim()
                        val nextState = formState.copy(
                            fullName = value,
                            fullNameError = if (trimmed.isEmpty()) R.string.student_profile_full_name_error else null
                        )
                        formState = nextState.copy(canSave = recalcCanSave(nextState))
                    },
                    onParentNameChanged = {
                        val nextState = formState.copy(parentName = it)
                        formState = nextState.copy(canSave = recalcCanSave(nextState))
                    },
                    onParentContactChanged = {
                        val nextState = formState.copy(parentContact = it)
                        formState = nextState.copy(canSave = recalcCanSave(nextState))
                    },
                    onHourlyRateChanged = { value ->
                        val nextState = formState.copy(hourlyRateInput = value, hourlyRateError = null)
                        formState = nextState.copy(canSave = recalcCanSave(nextState))
                    },
                    onNotesChanged = {
                        val nextState = formState.copy(notes = it)
                        formState = nextState.copy(canSave = recalcCanSave(nextState))
                    },
                    onSave = { saveTriggered = true },
                    modifier = Modifier
                )
            }
        }

        val fullNameField = composeRule.onNodeWithTag("StudentProfileFullName")
        val saveButton = composeRule.onNodeWithTag("StudentProfileSaveButton")
        saveButton.assertIsNotEnabled()

        fullNameField.performTextInput(" Jr")
        composeRule.waitForIdle()
        saveButton.assertIsEnabled()

        fullNameField.performTextClearance()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(getString(R.string.student_profile_full_name_error)).assertIsDisplayed()
        saveButton.assertIsNotEnabled()

        fullNameField.performTextInput("Alice Johnson")
        composeRule.waitForIdle()
        saveButton.assertIsNotEnabled()

        fullNameField.performTextInput(" Jr")
        composeRule.waitForIdle()
        saveButton.assertIsEnabled()
        saveButton.performClick()
        composeRule.runOnIdle { assertTrue(saveTriggered) }
    }

    @Test
    fun addStudentFormRequiresNameBeforeSave() {
        composeRule.setContent {
            KoraTheme {
                val snapshot = StudentProfileSnapshot(
                    fullName = "",
                    parentName = "",
                    parentContact = "",
                    notes = "",
                    hourlyRateInput = ""
                )
                var formState by remember {
                    mutableStateOf(
                        StudentProfileUiState(
                            isLoading = false,
                            defaultHourlyRate = 72.0,
                            currencyCode = "USD"
                        ).withComputedSaveState(snapshot)
                    )
                }
                var saveTriggered by remember { mutableStateOf(false) }

                StudentProfileContent(
                    title = getString(R.string.add_student_profile_title),
                    state = formState,
                    onBack = {},
                    onFullNameChanged = { value ->
                        formState = formState.copy(fullName = value).withComputedSaveState(snapshot)
                    },
                    onParentNameChanged = { value ->
                        formState = formState.copy(parentName = value).withComputedSaveState(snapshot)
                    },
                    onParentContactChanged = { value ->
                        formState = formState.copy(parentContact = value).withComputedSaveState(snapshot)
                    },
                    onHourlyRateChanged = { value ->
                        formState = formState.copy(hourlyRateInput = value, hourlyRateError = null)
                            .withComputedSaveState(snapshot)
                    },
                    onNotesChanged = { value ->
                        formState = formState.copy(notes = value).withComputedSaveState(snapshot)
                    },
                    onSave = { saveTriggered = true },
                    modifier = Modifier
                )
            }
        }

        val fullNameField = composeRule.onNodeWithTag("StudentProfileFullName")
        val saveButton = composeRule.onNodeWithTag("StudentProfileSaveButton")
        saveButton.assertIsNotEnabled()

        composeRule.onNodeWithText(getString(R.string.student_profile_optional_label)).assertIsDisplayed()

        fullNameField.performTextInput("Taylor Swift")
        composeRule.waitForIdle()
        saveButton.assertIsEnabled()

        saveButton.performClick()
        composeRule.runOnIdle { assertTrue(saveTriggered) }
    }

    private fun getString(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
}
