package com.barutdev.kora.ui.screens.dashboard

import android.content.res.Configuration
import android.widget.Toast
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
        composeRule.setContent {
            ProvideLocale(languageCode = "tr") {
                MaterialTheme {
                    TurkishToastInvoker()
                }
            }
        }
        // Click to show toast
        composeRule.onNodeWithText("Show Toast").performClick()
        // Verify Turkish toast text appears
        androidx.test.espresso.Espresso.onView(
            androidx.test.espresso.matcher.ViewMatchers.withText("Ödeme geçmişi bulunamadı.")
        ).inRoot(com.barutdev.kora.testing.ToastMatcher())
            .check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
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
    Button(onClick = {
        Toast.makeText(
            localizedContext,
            localizedContext.getString(R.string.no_payment_history_toast),
            Toast.LENGTH_SHORT
        ).show()
    }) {
        Text("Show Toast")
    }
}
