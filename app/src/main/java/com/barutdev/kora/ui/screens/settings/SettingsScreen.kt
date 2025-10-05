package com.barutdev.kora.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.barutdev.kora.util.koraStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.util.formatCurrency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val isLanguageDialogVisible by viewModel.isLanguageDialogVisible.collectAsStateWithLifecycle()
    val isCurrencyDialogVisible by viewModel.isCurrencyDialogVisible.collectAsStateWithLifecycle()
    val isHourlyRateDialogVisible by viewModel.isHourlyRateDialogVisible.collectAsStateWithLifecycle()

    if (isLanguageDialogVisible) {
        LanguageSelectionDialog(
            currentLanguage = userPreferences.languageCode,
            availableLanguages = viewModel.availableLanguages,
            onLanguageSelected = { languageCode ->
                viewModel.updateLanguage(languageCode)
            },
            onDismiss = viewModel::dismissLanguageDialog
        )
    }

    if (isCurrencyDialogVisible) {
        CurrencySelectionDialog(
            currentCurrency = userPreferences.currencyCode,
            availableCurrencies = viewModel.availableCurrencies,
            onCurrencySelected = viewModel::updateCurrency,
            onDismiss = viewModel::dismissCurrencyDialog
        )
    }

    if (isHourlyRateDialogVisible) {
        HourlyRateDialog(
            currentRate = userPreferences.defaultHourlyRate,
            currencyCode = userPreferences.currencyCode,
            onDismiss = viewModel::dismissHourlyRateDialog,
            onConfirm = viewModel::updateHourlyRate
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = koraStringResource(id = R.string.settings_title))
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = koraStringResource(id = R.string.settings_back_content_description)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(
                    title = koraStringResource(id = R.string.settings_section_general)
                ) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.DarkMode,
                        iconContentDescription = koraStringResource(id = R.string.settings_dark_mode_content_description),
                        title = koraStringResource(id = R.string.settings_dark_mode_label),
                        checked = userPreferences.isDarkMode,
                        onCheckedChange = viewModel::updateDarkMode
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.Language,
                        iconContentDescription = koraStringResource(id = R.string.settings_language_content_description),
                        title = koraStringResource(id = R.string.settings_language_label),
                        value = koraStringResource(id = languageLabelRes(userPreferences.languageCode)),
                        onClick = viewModel::showLanguageDialog
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.Sell,
                        iconContentDescription = koraStringResource(id = R.string.settings_currency_content_description),
                        title = koraStringResource(id = R.string.settings_currency_label),
                        value = koraStringResource(id = currencyLabelRes(userPreferences.currencyCode)),
                        onClick = viewModel::showCurrencyDialog
                    )
                }
            }
            item {
                SettingsSection(
                    title = koraStringResource(id = R.string.settings_section_notifications)
                ) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        iconContentDescription = koraStringResource(id = R.string.settings_lesson_reminders_content_description),
                        title = koraStringResource(id = R.string.settings_lesson_reminders_label),
                        checked = userPreferences.lessonRemindersEnabled,
                        onCheckedChange = viewModel::updateLessonRemindersEnabled
                    )
                    SettingsDivider()
                    SettingSwitchRow(
                        icon = Icons.Outlined.EventNote,
                        iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_content_description),
                        title = koraStringResource(id = R.string.settings_log_reminder_label),
                        checked = userPreferences.logReminderEnabled,
                        onCheckedChange = viewModel::updateLogReminderEnabled
                    )
                }
            }
            item {
                SettingsSection(
                    title = koraStringResource(id = R.string.settings_section_tutoring)
                ) {
                    SettingNavigationRow(
                        icon = Icons.Outlined.Payments,
                        iconContentDescription = koraStringResource(id = R.string.settings_default_hourly_rate_content_description),
                        title = koraStringResource(id = R.string.settings_default_hourly_rate_label),
                        value = formatHourlyRate(
                            amount = userPreferences.defaultHourlyRate,
                            currencyCode = userPreferences.currencyCode
                        ),
                        onClick = viewModel::showHourlyRateDialog
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Card(shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContentDescription: String,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon, contentDescription = iconContentDescription)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun SettingNavigationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContentDescription: String,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon, contentDescription = iconContentDescription)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    availableLanguages: List<String>,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languageDisplayNames = mapOf(
        "en" to "English",
        "tr" to "Türkçe",
        "de" to "Deutsch"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.settings_language_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                availableLanguages.forEach { languageCode ->
                    val label = languageDisplayNames[languageCode.lowercase(Locale.ROOT)] ?: languageCode
                    val selected = languageCode == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                role = Role.RadioButton,
                                onClick = { onLanguageSelected(languageCode) }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selected,
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}

@Composable
private fun CurrencySelectionDialog(
    currentCurrency: String,
    availableCurrencies: List<String>,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.settings_currency_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                availableCurrencies.forEach { currencyCode ->
                    val label = koraStringResource(id = currencyLabelRes(currencyCode))
                    val selected = currencyCode == currentCurrency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                role = Role.RadioButton,
                                onClick = { onCurrencySelected(currencyCode) }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selected,
                            onClick = null
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}

@Composable
private fun HourlyRateDialog(
    currentRate: Double,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var rateInput by rememberSaveable(currencyCode, currentRate) {
        mutableStateOf(
            currentRate.takeIf { it > 0.0 }?.let { String.format(Locale.US, "%.2f", it) }
                ?: ""
        )
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.settings_hourly_rate_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = rateInput,
                    onValueChange = { newValue ->
                        rateInput = newValue
                        isError = false
                    },
                    label = {
                        Text(text = koraStringResource(id = R.string.settings_hourly_rate_dialog_label))
                    },
                    placeholder = {
                        Text(text = koraStringResource(id = R.string.settings_hourly_rate_dialog_hint, currencyCode))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    textStyle = LocalTextStyle.current
                )
                if (isError) {
                    Text(
                        text = koraStringResource(id = R.string.settings_hourly_rate_dialog_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val normalized = rateInput.trim().replace(',', '.')
                    val value = normalized.toDoubleOrNull()
                    if (value == null || value < 0.0) {
                        isError = true
                        return@TextButton
                    }
                    onConfirm(value)
                }
            ) {
                Text(text = koraStringResource(id = R.string.settings_save_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = koraStringResource(id = R.string.dialog_action_cancel))
            }
        }
    )
}

@Composable
private fun formatHourlyRate(amount: Double, currencyCode: String): String {
    if (amount <= 0.0) {
        return koraStringResource(id = R.string.settings_hourly_rate_empty_value, currencyCode)
    }
    return remember(amount, currencyCode) {
        formatCurrency(amount, currencyCode)
    }
}

@androidx.annotation.StringRes
private fun languageLabelRes(languageCode: String): Int = when (languageCode.lowercase(Locale.ROOT)) {
    "en" -> R.string.settings_language_option_en
    "tr" -> R.string.settings_language_option_tr
    "de" -> R.string.settings_language_option_de
    else -> R.string.settings_language_option_en
}

@androidx.annotation.StringRes
private fun currencyLabelRes(currencyCode: String): Int = when (currencyCode.uppercase(Locale.ROOT)) {
    "USD" -> R.string.settings_currency_option_usd
    "EUR" -> R.string.settings_currency_option_eur
    "TRY" -> R.string.settings_currency_option_try
    else -> R.string.settings_currency_option_usd
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    val previewPreferences = UserPreferences(
        isDarkMode = true,
        languageCode = "en",
        currencyCode = "TRY",
        defaultHourlyRate = 80.0,
        lessonRemindersEnabled = true,
        logReminderEnabled = true
    )
    KoraTheme {
        SettingsPreviewContent(preferences = previewPreferences)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPreviewContent(preferences: UserPreferences) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = koraStringResource(id = R.string.settings_title)) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(title = koraStringResource(id = R.string.settings_section_general)) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.DarkMode,
                        iconContentDescription = koraStringResource(id = R.string.settings_dark_mode_content_description),
                        title = koraStringResource(id = R.string.settings_dark_mode_label),
                        checked = preferences.isDarkMode,
                        onCheckedChange = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.Language,
                        iconContentDescription = koraStringResource(id = R.string.settings_language_content_description),
                        title = koraStringResource(id = R.string.settings_language_label),
                        value = koraStringResource(id = languageLabelRes(preferences.languageCode)),
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.Sell,
                        iconContentDescription = koraStringResource(id = R.string.settings_currency_content_description),
                        title = koraStringResource(id = R.string.settings_currency_label),
                        value = koraStringResource(id = currencyLabelRes(preferences.currencyCode)),
                        onClick = {}
                    )
                }
            }
            item {
                SettingsSection(title = koraStringResource(id = R.string.settings_section_notifications)) {
                    SettingSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        iconContentDescription = koraStringResource(id = R.string.settings_lesson_reminders_content_description),
                        title = koraStringResource(id = R.string.settings_lesson_reminders_label),
                        checked = preferences.lessonRemindersEnabled,
                        onCheckedChange = {}
                    )
                    SettingsDivider()
                    SettingSwitchRow(
                        icon = Icons.Outlined.EventNote,
                        iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_content_description),
                        title = koraStringResource(id = R.string.settings_log_reminder_label),
                        checked = preferences.logReminderEnabled,
                        onCheckedChange = {}
                    )
                }
            }
            item {
                SettingsSection(title = koraStringResource(id = R.string.settings_section_tutoring)) {
                    SettingNavigationRow(
                        icon = Icons.Outlined.Payments,
                        iconContentDescription = koraStringResource(id = R.string.settings_default_hourly_rate_content_description),
                        title = koraStringResource(id = R.string.settings_default_hourly_rate_label),
                        value = formatHourlyRate(
                            amount = preferences.defaultHourlyRate,
                            currencyCode = preferences.currencyCode
                        ),
                        onClick = {}
                    )
                }
            }
        }
    }
}
