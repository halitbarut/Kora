package com.barutdev.kora.ui.screens.dashboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.PaymentRecord
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun markAsPaid_and_paymentHistory_dialogs_are_independent() {
        composeRule.setContent {
            MaterialTheme {
                TestHost()
            }
        }

        // Initially no dialogs
        composeRule.onAllNodesWithText(getString(R.string.payment_history_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertCountEquals(0)

        // Click Mark as Paid button -> only MarkAsPaid dialog shows
        composeRule.onNodeWithText(getString(R.string.dashboard_payment_mark_paid)).performClick()
        composeRule.onNodeWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertIsDisplayed()
        composeRule.onAllNodesWithText(getString(R.string.payment_history_title)).assertCountEquals(0)

        // Dismiss MarkAsPaid dialog
        composeRule.onNodeWithText(getString(R.string.dashboard_mark_paid_cancel)).performClick()
        composeRule.onAllNodesWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertCountEquals(0)

        // Click Payment History icon -> only PaymentHistory dialog shows
        composeRule.onNodeWithContentDescription(getString(R.string.dashboard_payment_history_icon_description)).performClick()
        composeRule.onNodeWithText(getString(R.string.payment_history_title)).assertIsDisplayed()
        composeRule.onAllNodesWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertCountEquals(0)

        // Close PaymentHistory dialog
        composeRule.onNodeWithText(getString(R.string.close_button)).performClick()
        composeRule.onAllNodesWithText(getString(R.string.payment_history_title)).assertCountEquals(0)
    }

    @Composable
    private fun TestHost() {
        var showHistory by remember { mutableStateOf(false) }
        var showMarkPaid by remember { mutableStateOf(false) }
        val records = remember { listOf(PaymentRecord(id = 1, studentId = 1, amountMinor = 1000, paidAtEpochMs = 0L)) }

        PaymentTrackingCard(
            totalHours = 2.0,
            hourlyRate = 10.0,
            totalAmountDue = 20.0,
            lastPaymentDate = null,
            onMarkPaidClick = { showMarkPaid = true },
            onShowPaymentHistory = { showHistory = true },
            currencyCode = "USD",
            locale = java.util.Locale.US
        )

        PaymentHistoryDialog(
            showDialog = showHistory,
            records = records,
            currencyCode = "USD",
            onDismiss = { showHistory = false }
        )

        MarkAsPaidConfirmDialog(
            showDialog = showMarkPaid,
            onConfirm = { showMarkPaid = false },
            onDismiss = { showMarkPaid = false }
        )
    }

    private fun getString(id: Int): String = androidx.test.platform.app.InstrumentationRegistry
        .getInstrumentation().targetContext.getString(id)
}
