package com.barutdev.kora.ui.student_list

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeTimeoutException
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.barutdev.kora.MainActivity
import com.barutdev.kora.R
import com.barutdev.kora.navigation.KoraDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StudentListReportsNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun reportsIconNavigatesToReportsAndBack() {
        skipOnboardingIfNeeded()

        val reportsDescription = composeRule.activity.getString(R.string.student_list_reports_action_description)
        val reportsMatcher = hasContentDescription(reportsDescription)
        waitForNode(reportsMatcher)

        composeRule.onNodeWithContentDescription(
            reportsDescription,
            useUnmergedTree = true
        ).performClick()

        val reportsTitle = composeRule.activity.getString(R.string.reports_tab_label)
        waitForNode(hasText(reportsTitle))

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        val studentListTitle = composeRule.activity.getString(R.string.student_list_title)
        waitForNode(hasText(studentListTitle))
    }

    @Test
    fun bottomBarDestinationsContainOnlyCoreStudentTabs() {
        val expected = listOf(
            KoraDestination.Dashboard,
            KoraDestination.Calendar,
            KoraDestination.Homework
        )
        assertEquals(expected, KoraDestination.bottomBarDestinations)
    }

    private fun skipOnboardingIfNeeded() {
        val nextLabel = composeRule.activity.getString(R.string.onboarding_next)
        val nextMatcher = hasText(nextLabel)
        val onboardingVisible = try {
            composeRule.waitUntil(timeoutMillis = 3_000) {
                composeRule.onAllNodes(nextMatcher, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            true
        } catch (_: ComposeTimeoutException) {
            false
        }

        if (!onboardingVisible) {
            return
        }

        repeat(4) { step ->
            composeRule.onNodeWithText(nextLabel, useUnmergedTree = true).performClick()
            if (step < 3) {
                composeRule.waitUntil(timeoutMillis = 3_000) {
                    composeRule.onAllNodes(nextMatcher, useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty()
                }
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(isToggleable(), useUnmergedTree = true).performClick()

        val getStartedLabel = composeRule.activity.getString(R.string.onboarding_get_started)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText(getStartedLabel), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(getStartedLabel, useUnmergedTree = true).performClick()

        val studentListTitle = composeRule.activity.getString(R.string.student_list_title)
        waitForNode(hasText(studentListTitle))
    }

    private fun waitForNode(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(matcher, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodes(matcher, useUnmergedTree = true).assertCountEquals(1)
    }
}
