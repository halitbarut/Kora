package com.barutdev.kora.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private val currencyLocaleMap: Map<String, Locale> = mapOf(
    "USD" to Locale.US,
    "EUR" to Locale.GERMANY,
    "TRY" to Locale("tr", "TR")
)

fun formatCurrency(amount: Double, currencyCode: String): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
        ?: return amount.toString()
    val locale = currencyLocaleMap[currency.currencyCode] ?: Locale.getDefault()
    val formatter = NumberFormat.getCurrencyInstance(locale)
    formatter.currency = currency
    return formatter.format(amount)
}
