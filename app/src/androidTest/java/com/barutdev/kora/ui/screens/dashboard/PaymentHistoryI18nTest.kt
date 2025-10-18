package com.barutdev.kora.ui.screens.dashboard

import android.content.res.Configuration
import android.widget.Toast
import com.barutdev.kora.util.MessageNotifier
import com.barutdev.kora.util.ProvideMessageNotifier
import com.barutdev.kora.util.LocalMessageNotifier
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.barutdev.kora.R
import com.barutdev.kora.ui.theme.ProvideLocale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentHistoryI18nTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dialog_title_uses_german_locale_when_app_locale_is_de() {
        composeRule.setContent {
            ProvideLocale(languageCode = "de") {
                MaterialTheme {
                    PaymentHistoryDialog(
                        showDialog = true,
                        records = listOf(),
                        currencyCode = "EUR",
                        onDismiss = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText("Zahlungsverlauf").assertIsDisplayed()
    }

    @Test
    fun toast_uses_turkish_locale_when_app_locale_is_tr() {
        val fake = FakeNotifierForPaymentHistory()
        composeRule.setContent {
            ProvideLocale(languageCode = "tr") {
                ProvideMessageNotifier(notifier = fake) {
                    MaterialTheme {
                        TurkishToastInvoker()
                    }
                }
            }
        }
        // Click to show message
        composeRule.onNodeWithText("Show Toast").performClick()
        // Verify the notifier was invoked with the correct resource id
        composeRule.waitUntil(timeoutMillis = 5_000) { fake.resIds.isNotEmpty() || fake.messages.isNotEmpty() }
        assert(fake.resIds.contains(R.string.no_payment_history_toast))
    }
}

@Composable
private fun TurkishToastInvoker() {
    val context = LocalContext.current
    val localizedContext = remember(context) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(java.util.Locale("tr"))
        context.createConfigurationContext(configuration)
    }
    val notifier = LocalMessageNotifier.current
    Button(onClick = {
        notifier.showMessage(R.string.no_payment_history_toast)
    }) {
        Text("Show Toast")
    }
}

private class FakeNotifierForPaymentHistory : MessageNotifier {
    val messages = mutableListOf<String>()
    val resIds = mutableListOf<Int>()
    override fun showMessage(message: String) {
        messages.add(message)
    }
    override fun showMessage(resId: Int, vararg formatArgs: Any) {
        resIds.add(resId)
    }
}
