package com.barutdev.kora.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.kora.R
import com.barutdev.kora.ui.theme.ProvideLocale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.barutdev.kora.testing.ToastMatcher
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed

@RunWith(AndroidJUnit4::class)
class StudentDashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun markAsPaid_shows_dialog_when_balance_positive() {
        composeRule.setContent {
            ProvideLocale(languageCode = "en") {
                MaterialTheme {
                    TestHostWithPrecondition(totalAmountDue = 10.0)
                }
            }
        }
        composeRule.onNodeWithText(getString(R.string.dashboard_payment_mark_paid)).performClick()
        composeRule.onNodeWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertIsDisplayed()
    }

    @Test
    fun markAsPaid_shows_toast_and_no_dialog_when_balance_zero_or_less() {
        composeRule.setContent {
            ProvideLocale(languageCode = "en") {
                MaterialTheme {
                    TestHostWithPrecondition(totalAmountDue = 0.0)
                }
            }
        }
        composeRule.onNodeWithText(getString(R.string.dashboard_payment_mark_paid)).performClick()
        // Verify toast appears
        androidx.test.espresso.Espresso.onView(
            androidx.test.espresso.matcher.ViewMatchers.withText(getString(R.string.no_debt_to_pay_toast))
        ).inRoot(ToastMatcher())
            .check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
        // And dialog does not appear
        composeRule.onNodeWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertDoesNotExist()
    }
}

@Composable
private fun TestHostWithPrecondition(totalAmountDue: Double) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Button(onClick = {
        if (totalAmountDue > 0.0) {
            showDialog = true
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_debt_to_pay_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }) {
        Text(text = context.getString(R.string.dashboard_payment_mark_paid))
    }

    MarkAsPaidConfirmDialog(
        showDialog = showDialog,
        onConfirm = { showDialog = false },
        onDismiss = { showDialog = false }
    )
}

private fun getString(id: Int): String =
    androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        .targetContext.getString(id)
