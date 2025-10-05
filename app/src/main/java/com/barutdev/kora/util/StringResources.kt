package com.barutdev.kora.util

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.barutdev.kora.ui.theme.LocalLocale

@Composable
inline fun koraStringResource(
    @StringRes id: Int,
    vararg formatArgs: Any
): String {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    val localizedContext = context.createConfigurationContext(configuration)
    return localizedContext.resources.getString(id, *formatArgs)
}
