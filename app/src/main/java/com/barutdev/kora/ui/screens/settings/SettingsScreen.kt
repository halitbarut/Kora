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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.kora.R
import com.barutdev.kora.domain.model.UserPreferences
import com.barutdev.kora.ui.navigation.LocalKoraScaffoldController
import com.barutdev.kora.ui.navigation.ScreenScaffoldConfig
import com.barutdev.kora.ui.navigation.TopBarAction
import com.barutdev.kora.ui.navigation.TopBarConfig
import com.barutdev.kora.ui.theme.KoraTheme
import com.barutdev.kora.ui.theme.LocalLocale
import com.barutdev.kora.util.formatCurrency
import android.Manifest
import android.text.format.DateFormat
import android.util.Log
import android.content.pm.PackageManager
import android.os.Build
import java.io.IOException
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlin.text.Charsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val scaffoldController = LocalKoraScaffoldController.current
    val snackbarHostState = scaffoldController.snackbarHostState
    val isLanguageDialogVisible by viewModel.isLanguageDialogVisible.collectAsStateWithLifecycle()
    val isCurrencyDialogVisible by viewModel.isCurrencyDialogVisible.collectAsStateWithLifecycle()
    val isHourlyRateDialogVisible by viewModel.isHourlyRateDialogVisible.collectAsStateWithLifecycle()
    var showLessonReminderTimeDialog by rememberSaveable { mutableStateOf(false) }
    var showLogReminderTimeDialog by rememberSaveable { mutableStateOf(false) }
    val locale = LocalLocale.current
    val lessonReminderTimeText = remember(
        userPreferences.lessonReminderHour,
        userPreferences.lessonReminderMinute,
        locale
    ) {
        formatReminderTime(
            hour = userPreferences.lessonReminderHour,
            minute = userPreferences.lessonReminderMinute,
            locale = locale
        )
    }
    val logReminderTimeText = remember(
        userPreferences.logReminderHour,
        userPreferences.logReminderMinute,
        locale
    ) {
        formatReminderTime(
            hour = userPreferences.logReminderHour,
            minute = userPreferences.logReminderMinute,
            locale = locale
        )
    }
    var showResetConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationPermissionRequest = remember { mutableStateOf<NotificationToggle?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pendingToggle = notificationPermissionRequest.value
        notificationPermissionRequest.value = null
        when (pendingToggle) {
            NotificationToggle.LESSON_REMINDER -> {
                if (granted) {
                    viewModel.updateLessonRemindersEnabled(true)
                } else {
                    viewModel.updateLessonRemindersEnabled(false)
                }
            }
            NotificationToggle.LOG_REMINDER -> {
                if (granted) {
                    viewModel.updateLogReminderEnabled(true)
                } else {
                    viewModel.updateLogReminderEnabled(false)
                }
            }
            null -> Unit
        }
    }
    val handleNotificationToggle: (Boolean, NotificationToggle) -> Unit = { isEnabled, toggle ->
        if (!isEnabled) {
            when (toggle) {
                NotificationToggle.LESSON_REMINDER -> viewModel.updateLessonRemindersEnabled(false)
                NotificationToggle.LOG_REMINDER -> viewModel.updateLogReminderEnabled(false)
            }
        } else {
            val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            if (!needsRuntimePermission) {
                when (toggle) {
                    NotificationToggle.LESSON_REMINDER -> viewModel.updateLessonRemindersEnabled(true)
                    NotificationToggle.LOG_REMINDER -> viewModel.updateLogReminderEnabled(true)
                }
            } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    when (toggle) {
                        NotificationToggle.LESSON_REMINDER -> viewModel.updateLessonRemindersEnabled(true)
                        NotificationToggle.LOG_REMINDER -> viewModel.updateLogReminderEnabled(true)
                    }
                } else {
                    notificationPermissionRequest.value = toggle
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    val backupSuccessMessage = koraStringResource(id = R.string.settings_backup_success)
    val backupFailureMessage = koraStringResource(id = R.string.settings_backup_failure)
    val restoreSuccessMessage = koraStringResource(id = R.string.settings_restore_success)
    val restoreFailureMessage = koraStringResource(id = R.string.settings_restore_failure)
    val resetSuccessMessage = koraStringResource(id = R.string.settings_reset_success)
    val resetFailureMessage = koraStringResource(id = R.string.settings_reset_failure)
    val backupFileNameFormatter = remember { DateTimeFormatter.ofPattern("yyyyMMdd_HHmm") }

    val topBarTitle = koraStringResource(id = R.string.settings_title)
    val backContentDescription = koraStringResource(id = R.string.settings_back_content_description)
    val topBarConfig = remember(
        topBarTitle,
        backContentDescription,
        onNavigateBack
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = onNavigateBack?.let {
                TopBarAction(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = backContentDescription,
                    onClick = it
                )
            }
        )
    }
    ScreenScaffoldConfig(topBarConfig = topBarConfig)

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val exportResult = viewModel.exportCsv()
            exportResult.onSuccess { csv ->
                val writeResult = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csv.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    } ?: throw IOException("Unable to open output stream")
                }
                if (writeResult.isSuccess) {
                    snackbarHostState.showSnackbar(backupSuccessMessage)
                } else {
                    Log.e("SettingsScreen", "Failed to write backup", writeResult.exceptionOrNull())
                    snackbarHostState.showSnackbar(backupFailureMessage)
                }
            }.onFailure { error ->
                Log.e("SettingsScreen", "Failed to export backup", error)
                snackbarHostState.showSnackbar(backupFailureMessage)
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val csvResult = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    reader.readText()
                } ?: throw IOException("Unable to open input stream")
            }
            csvResult.onSuccess { csv ->
                val importResult = viewModel.importCsv(csv)
                if (importResult.isSuccess) {
                    snackbarHostState.showSnackbar(restoreSuccessMessage)
                } else {
                    Log.e("SettingsScreen", "Failed to import backup", importResult.exceptionOrNull())
                    snackbarHostState.showSnackbar(restoreFailureMessage)
                }
            }.onFailure { error ->
                Log.e("SettingsScreen", "Failed to read backup", error)
                snackbarHostState.showSnackbar(restoreFailureMessage)
            }
        }
    }

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

    if (showLessonReminderTimeDialog) {
        ReminderTimeDialog(
            title = koraStringResource(id = R.string.settings_lesson_reminder_time_dialog_title),
            confirmLabel = koraStringResource(id = R.string.dialog_action_save),
            dismissLabel = koraStringResource(id = R.string.dialog_action_cancel),
            initialHour = userPreferences.lessonReminderHour,
            initialMinute = userPreferences.lessonReminderMinute,
            onDismiss = { showLessonReminderTimeDialog = false },
            onConfirm = { hour, minute ->
                showLessonReminderTimeDialog = false
                viewModel.updateLessonReminderTime(hour, minute)
            }
        )
    }

    if (showLogReminderTimeDialog) {
        ReminderTimeDialog(
            title = koraStringResource(id = R.string.settings_log_reminder_time_dialog_title),
            confirmLabel = koraStringResource(id = R.string.dialog_action_save),
            dismissLabel = koraStringResource(id = R.string.dialog_action_cancel),
            initialHour = userPreferences.logReminderHour,
            initialMinute = userPreferences.logReminderMinute,
            onDismiss = { showLogReminderTimeDialog = false },
            onConfirm = { hour, minute ->
                showLogReminderTimeDialog = false
                viewModel.updateLogReminderTime(hour, minute)
            }
        )
    }

    if (showResetConfirmation) {
        ResetConfirmationDialog(
            onConfirm = {
                showResetConfirmation = false
                coroutineScope.launch {
                    val result = viewModel.resetAllData()
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar(resetSuccessMessage)
                    } else {
                        Log.e("SettingsScreen", "Failed to reset data", result.exceptionOrNull())
                        snackbarHostState.showSnackbar(resetFailureMessage)
                    }
                }
            },
            onDismiss = { showResetConfirmation = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
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
                    onCheckedChange = { isEnabled ->
                        handleNotificationToggle(isEnabled, NotificationToggle.LESSON_REMINDER)
                    }
                )
                SettingsDivider()
                SettingSwitchRow(
                    icon = Icons.AutoMirrored.Outlined.EventNote,
                    iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_content_description),
                    title = koraStringResource(id = R.string.settings_log_reminder_label),
                    checked = userPreferences.logReminderEnabled,
                    onCheckedChange = { isEnabled ->
                        handleNotificationToggle(isEnabled, NotificationToggle.LOG_REMINDER)
                    }
                )
                SettingsDivider()
                SettingNavigationRow(
                    icon = Icons.Outlined.Schedule,
                    iconContentDescription = koraStringResource(id = R.string.settings_lesson_reminder_time_content_description),
                    title = koraStringResource(id = R.string.settings_lesson_reminder_time_label),
                    value = lessonReminderTimeText,
                    onClick = { showLessonReminderTimeDialog = true }
                )
                SettingsDivider()
                SettingNavigationRow(
                    icon = Icons.Outlined.AccessTime,
                    iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_time_content_description),
                    title = koraStringResource(id = R.string.settings_log_reminder_time_label),
                    value = logReminderTimeText,
                    onClick = { showLogReminderTimeDialog = true }
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
        item {
            SettingsSection(
                title = koraStringResource(id = R.string.settings_section_data)
            ) {
                SettingNavigationRow(
                    icon = Icons.Outlined.FileDownload,
                    iconContentDescription = koraStringResource(id = R.string.settings_backup_content_description),
                    title = koraStringResource(id = R.string.settings_backup_title),
                    value = koraStringResource(id = R.string.settings_backup_action_value),
                    onClick = {
                        val fileName = "kora_backup_${LocalDateTime.now().format(backupFileNameFormatter)}.csv"
                        backupLauncher.launch(fileName)
                    }
                )
                SettingsDivider()
                SettingNavigationRow(
                    icon = Icons.Outlined.FileUpload,
                    iconContentDescription = koraStringResource(id = R.string.settings_restore_content_description),
                    title = koraStringResource(id = R.string.settings_restore_title),
                    value = koraStringResource(id = R.string.settings_restore_action_value),
                    onClick = {
                        restoreLauncher.launch(arrayOf("text/*", "application/*"))
                    }
                )
                SettingsDivider()
                SettingNavigationRow(
                    icon = Icons.Outlined.DeleteForever,
                    iconContentDescription = koraStringResource(id = R.string.settings_reset_content_description),
                    title = koraStringResource(id = R.string.settings_reset_title),
                    value = koraStringResource(id = R.string.settings_reset_action_value),
                    onClick = {
                        showResetConfirmation = true
                    }
                )
            }
        }
        item {
            val uriHandler = LocalUriHandler.current

            SettingsSection(
                title = koraStringResource(id = R.string.settings_section_about)
            ) {
                SettingNavigationRow(
                    icon = Icons.Outlined.Policy,
                    iconContentDescription = koraStringResource(id = R.string.settings_privacy_policy_content_description),
                    title = koraStringResource(id = R.string.settings_privacy_policy_label),
                    value = "",
                    onClick = {
                        uriHandler.openUri("https://gist.github.com/halitbarut/b6b011b0d3cca23bd36781b9465a3cef")
                    }
                )
                SettingsDivider()
                SettingNavigationRow(
                    icon = Icons.AutoMirrored.Outlined.Article,
                    iconContentDescription = koraStringResource(id = R.string.settings_terms_content_description),
                    title = koraStringResource(id = R.string.settings_terms_label),
                    value = "",
                    onClick = {
                        uriHandler.openUri("https://gist.github.com/halitbarut/5a56f975637a6e815feaea41539854a2")
                    }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    title: String,
    confirmLabel: String,
    dismissLabel: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember { DateFormat.is24HourFormat(context) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel)
            }
        }
    )
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = koraStringResource(id = R.string.settings_reset_dialog_title))
        },
        text = {
            Text(text = koraStringResource(id = R.string.settings_reset_dialog_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = koraStringResource(id = R.string.settings_reset_dialog_confirm))
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

private enum class NotificationToggle {
    LESSON_REMINDER,
    LOG_REMINDER
}

private fun formatReminderTime(hour: Int, minute: Int, locale: Locale): String {
    val safeHour = hour.coerceIn(0, 23)
    val safeMinute = minute.coerceIn(0, 59)
    val time = LocalTime.of(safeHour, safeMinute)
    val formatter = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale)
    return time.format(formatter)
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
        logReminderEnabled = true,
        lessonReminderHour = 10,
        lessonReminderMinute = 30,
        logReminderHour = 19,
        logReminderMinute = 15
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
                        icon = Icons.AutoMirrored.Outlined.EventNote,
                        iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_content_description),
                        title = koraStringResource(id = R.string.settings_log_reminder_label),
                        checked = preferences.logReminderEnabled,
                        onCheckedChange = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.Schedule,
                        iconContentDescription = koraStringResource(id = R.string.settings_lesson_reminder_time_content_description),
                        title = koraStringResource(id = R.string.settings_lesson_reminder_time_label),
                        value = formatReminderTime(
                            hour = preferences.lessonReminderHour,
                            minute = preferences.lessonReminderMinute,
                            locale = Locale.getDefault()
                        ),
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.AccessTime,
                        iconContentDescription = koraStringResource(id = R.string.settings_log_reminder_time_content_description),
                        title = koraStringResource(id = R.string.settings_log_reminder_time_label),
                        value = formatReminderTime(
                            hour = preferences.logReminderHour,
                            minute = preferences.logReminderMinute,
                            locale = Locale.getDefault()
                        ),
                        onClick = {}
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
            item {
                SettingsSection(title = koraStringResource(id = R.string.settings_section_data)) {
                    SettingNavigationRow(
                        icon = Icons.Outlined.FileDownload,
                        iconContentDescription = koraStringResource(id = R.string.settings_backup_content_description),
                        title = koraStringResource(id = R.string.settings_backup_title),
                        value = koraStringResource(id = R.string.settings_backup_action_value),
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.FileUpload,
                        iconContentDescription = koraStringResource(id = R.string.settings_restore_content_description),
                        title = koraStringResource(id = R.string.settings_restore_title),
                        value = koraStringResource(id = R.string.settings_restore_action_value),
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingNavigationRow(
                        icon = Icons.Outlined.DeleteForever,
                        iconContentDescription = koraStringResource(id = R.string.settings_reset_content_description),
                        title = koraStringResource(id = R.string.settings_reset_title),
                        value = koraStringResource(id = R.string.settings_reset_action_value),
                        onClick = {}
                    )
                }
            }
        }
    }
}
