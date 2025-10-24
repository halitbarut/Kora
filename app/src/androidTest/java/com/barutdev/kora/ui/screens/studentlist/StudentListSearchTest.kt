package com.barutdev.kora.ui.screens.studentlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.Student
import com.barutdev.kora.ui.screens.student_list.StudentListScreenContent
import com.barutdev.kora.ui.screens.student_list.StudentWithDebt
import com.barutdev.kora.ui.theme.KoraTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentListSearchTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun searchFieldFiltersAndClearsStudentList() {
        composeRule.setContent {
            KoraTheme {
                var query by remember { mutableStateOf("") }
                val allStudents = remember { sampleStudents() }
                val filteredStudents = remember(query) {
                    if (query.isBlank()) {
                        allStudents
                    } else {
                        allStudents.filter { studentWithDebt ->
                            studentWithDebt.student.fullName.contains(query, ignoreCase = true)
                        }
                    }
                }

                StudentListScreenContent(
                    students = filteredStudents,
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onClearSearch = { query = "" },
                    onStudentClick = {},
                    onEditStudent = {},
                    currencyCode = "USD",
                    isSearchActive = query.isNotBlank(),
                    hasAnyStudents = allStudents.isNotEmpty(),
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Stone").assertIsDisplayed()

        composeRule.onNodeWithTag("StudentListSearchField")
            .performTextInput("ali")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onAllNodesWithText("Bob Stone").assertCountEquals(0)

        composeRule.onNodeWithTag("StudentListSearchField")
            .performTextClearance()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("StudentListSearchField")
            .performTextInput("zzz")
        composeRule.waitForIdle()

        composeRule.onNodeWithText(getString(R.string.student_list_search_no_results_message))
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription(getString(R.string.student_list_search_clear_content_description))
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeRule.onNodeWithText("Bob Stone").assertIsDisplayed()
    }

    private fun sampleStudents(): List<StudentWithDebt> = listOf(
        StudentWithDebt(
            student = Student(id = 1, fullName = "Alice Johnson", hourlyRate = 100.0),
            currentDebt = 150.0
        ),
        StudentWithDebt(
            student = Student(id = 2, fullName = "Bob Stone", hourlyRate = 80.0),
            currentDebt = 90.0
        ),
        StudentWithDebt(
            student = Student(id = 3, fullName = "Carla Brant", hourlyRate = 120.0),
            currentDebt = 60.0
        )
    )

    private fun getString(id: Int): String =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(id)
}

