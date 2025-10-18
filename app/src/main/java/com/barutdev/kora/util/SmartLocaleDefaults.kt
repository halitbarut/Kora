package com.barutdev.kora.util

import java.util.Locale

/**
 * Central mapping for smart defaults based on device language.
 * - tr -> (tr, TRY)
 * - de -> (de, EUR)
 * - else -> (en, USD)
 */
object SmartLocaleDefaults {
    data class SmartDefaults(
        val languageCode: String,
        val currencyCode: String,
    )

    fun resolveSmartDefaults(deviceLanguage: String): SmartDefaults {
        return when (deviceLanguage.lowercase(Locale.ROOT)) {
            "tr" -> SmartDefaults(languageCode = "tr", currencyCode = "TRY")
            "de" -> SmartDefaults(languageCode = "de", currencyCode = "EUR")
            else -> SmartDefaults(languageCode = "en", currencyCode = "USD")
        }
    }

    fun resolveSmartDefaultsFromDevice(): SmartDefaults {
        val deviceLanguage = Locale.getDefault().language
        return resolveSmartDefaults(deviceLanguage)
    }
}
