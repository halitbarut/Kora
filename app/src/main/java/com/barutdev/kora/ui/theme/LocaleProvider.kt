package com.barutdev.kora.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }

@Composable
fun ProvideLocale(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val locale = remember(languageCode) {
        val sanitizedTag = languageCode.trim().replace('_', '-').takeIf { it.isNotBlank() }
        if (sanitizedTag == null) {
            Locale.getDefault()
        } else {
            val fromTag = Locale.forLanguageTag(sanitizedTag)
            if (fromTag.language.isNotEmpty()) {
                fromTag
            } else {
                val parts = sanitizedTag.split('-', limit = 3).filter { it.isNotBlank() }
                when (parts.size) {
                    3 -> Locale(parts[0], parts[1], parts[2])
                    2 -> Locale(parts[0], parts[1])
                    1 -> Locale(parts[0])
                    else -> Locale.getDefault()
                }
            }
        }
    }

    SideEffect {
        Locale.setDefault(locale)
    }

    CompositionLocalProvider(LocalLocale provides locale) {
        content()
    }
}
