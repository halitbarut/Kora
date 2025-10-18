package com.barutdev.kora.ui.screens.dashboard

import android.widget.Toast
import android.content.res.Configuration
import java.util.Locale
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
import com.barutdev.kora.util.MessageNotifier
import com.barutdev.kora.util.ProvideMessageNotifier
import com.barutdev.kora.util.LocalMessageNotifier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag

@RunWith(AndroidJUnit4::class)
class StudentDashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun markAsPaid_shows_dialog_when_balance_positive() {
        val expectedAmount = com.barutdev.kora.util.formatCurrency(100.0, "TRY")
        composeRule.setContent {
            ProvideLocale(languageCode = "en") {
                MaterialTheme {
                    TestHostWithPaymentCard(totalAmountDue = 100.0)
                }
            }
        }
        // Verify initial state shows a positive non-zero amount
        composeRule.onAllNodesWithText(expectedAmount).assertCountEquals(1)
        // Prefer stable testTags to avoid locale issues
        composeRule.onNodeWithTag("MarkAsPaidButton").performClick()
        // Wait for the confirm dialog to appear via tag
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("MarkAsPaidConfirmDialog")
                .fetchSemanticsNodes().isNotEmpty()
        }
        // Assert the dialog is visible
        composeRule.onNodeWithTag("MarkAsPaidConfirmDialog").assertIsDisplayed()
    }

    @Test
    fun markAsPaid_shows_toast_and_no_dialog_when_balance_zero_or_less() {
        val fake = FakeNotifierForStudentDashboard()
        composeRule.setContent {
            ProvideLocale(languageCode = "en") {
                ProvideMessageNotifier(notifier = fake) {
                    MaterialTheme {
                        TestHostWithPrecondition(totalAmountDue = 0.0)
                    }
                }
            }
        }
        composeRule.onNodeWithText(getString(R.string.dashboard_payment_mark_paid)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { fake.resIds.isNotEmpty() || fake.messages.isNotEmpty() }
        // Verify message requested via notifier
        assert(fake.resIds.contains(R.string.no_debt_to_pay_toast))
        // And dialog does not appear
        composeRule.onAllNodesWithText(getString(R.string.dashboard_mark_paid_confirm_title)).assertCountEquals(0)
    }
}

@Composable
private fun TestHostWithPrecondition(totalAmountDue: Double) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notifier = LocalMessageNotifier.current
    Button(onClick = {
        if (totalAmountDue > 0.0) {
            showDialog = true
        } else {
            notifier.showMessage(R.string.no_debt_to_pay_toast)
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

@Composable
private fun TestHostWithPaymentCard(totalAmountDue: Double) {
    var showDialog by remember { mutableStateOf(false) }
    val notifier = LocalMessageNotifier.current
    // Render card with the amount so the formatted currency is visible in UI
    PaymentTrackingCard(
        totalHours = 2.0,
        hourlyRate = 50.0,
        totalAmountDue = totalAmountDue,
        lastPaymentDate = null,
        onMarkPaidClick = {
            if (totalAmountDue > 0.0) {
                showDialog = true
            } else {
                notifier.showMessage(R.string.no_debt_to_pay_toast)
            }
        },
        onShowPaymentHistory = {},
        currencyCode = "TRY",
        locale = java.util.Locale("tr", "TR")
    )

    MarkAsPaidConfirmDialog(
        showDialog = showDialog,
        onConfirm = { showDialog = false },
        onDismiss = { showDialog = false }
    )
}

private fun getString(id: Int): String =
    androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        .targetContext.getString(id)

private fun getEnglishString(id: Int): String {
    val base = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
    val config = Configuration(base.resources.configuration)
    config.setLocale(Locale.ENGLISH)
    val ctx = base.createConfigurationContext(config)
    return ctx.getString(id)
}

private class FakeNotifierForStudentDashboard : MessageNotifier {
    val messages = mutableListOf<String>()
    val resIds = mutableListOf<Int>()
    override fun showMessage(message: String) {
        messages.add(message)
    }
    override fun showMessage(resId: Int, vararg formatArgs: Any) {
        resIds.add(resId)
    }
}
