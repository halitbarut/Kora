package com.barutdev.kora.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

interface MessageNotifier {
    fun showMessage(message: String)
    fun showMessage(@StringRes resId: Int, vararg formatArgs: Any)
}

object NoopMessageNotifier : MessageNotifier {
    override fun showMessage(message: String) { /* no-op */ }
    override fun showMessage(resId: Int, vararg formatArgs: Any) { /* no-op */ }
}

class ToastMessageNotifier(private val context: Context) : MessageNotifier {
    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(resId: Int, vararg formatArgs: Any) {
        val text = context.getString(resId, *formatArgs)
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}

val LocalMessageNotifier = staticCompositionLocalOf<MessageNotifier> { NoopMessageNotifier }

@Composable
fun ProvideMessageNotifier(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val notifier = remember(context) { ToastMessageNotifier(context) }
    CompositionLocalProvider(LocalMessageNotifier provides notifier) {
        content()
    }
}

@Composable
fun ProvideMessageNotifier(
    notifier: MessageNotifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMessageNotifier provides notifier) {
        content()
    }
}
