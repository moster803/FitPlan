package de.fitplan.app.ui.settings

import android.content.ActivityNotFoundException
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.fitplan.app.BuildConfig
import de.fitplan.app.R
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Settings
import de.fitplan.app.core.model.ThemeMode
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.LocalContainer
import de.fitplan.app.ui.common.ConfirmDialog
import de.fitplan.app.ui.common.ReminderPermissions
import de.fitplan.app.ui.common.SectionHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(data: AppData, onBack: () -> Unit) {
    val context = LocalContext.current
    val container = LocalContainer.current
    val actions = LocalActions.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val settings = data.settings
    var editName by rememberSaveable { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<String?>(null) }

    val messageSaved = stringResource(R.string.backup_saved)
    val messageFailed = stringResource(R.string.backup_failed)
    val messageImported = stringResource(R.string.backup_imported)
    val messageTestBlocked = stringResource(R.string.test_blocked)

    fun update(transform: (Settings) -> Settings) = actions.launch { updateSettings(transform) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = runCatching {
                val json = container.repository.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } ?: error("Kein Zugriff")
                }
            }.isSuccess
            snackbar.showSnackbar(if (ok) messageSaved else messageFailed)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = runCatching {
                withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }
            }.getOrNull()
            if (text == null) {
                snackbar.showSnackbar(messageFailed)
            } else {
                pendingImport = text
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Group {
                    ActionRow(
                        icon = Icons.Rounded.Person,
                        title = stringResource(R.string.settings_name),
                        subtitle = settings.name.ifBlank { stringResource(R.string.settings_name_empty) },
                        onClick = { editName = true },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.settings_reminders)) }
            item { ReminderPermissions() }
            item {
                Group {
                    SwitchRow(
                        title = stringResource(R.string.supplement_reminders),
                        subtitle = stringResource(R.string.supplement_reminders_text),
                        checked = settings.supplementReminders,
                        onChange = { on -> update { it.copy(supplementReminders = on) } },
                    )
                    SwitchRow(
                        title = stringResource(R.string.low_stock_alerts),
                        subtitle = stringResource(R.string.low_stock_alerts_text),
                        checked = settings.lowStockAlerts,
                        onChange = { on -> update { it.copy(lowStockAlerts = on) } },
                    )
                    ActionRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = stringResource(R.string.test_notification),
                        subtitle = stringResource(R.string.test_notification_text),
                        onClick = {
                            if (container.notifications.canPost()) {
                                container.notifications.showTest()
                            } else {
                                scope.launch { snackbar.showSnackbar(messageTestBlocked) }
                            }
                        },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.settings_training)) }
            item {
                Group {
                    SwitchRow(stringResource(R.string.setting_sound), stringResource(R.string.setting_sound_text), settings.sound) { on ->
                        update { it.copy(sound = on) }
                    }
                    SwitchRow(stringResource(R.string.setting_vibration), null, settings.vibration) { on ->
                        update { it.copy(vibration = on) }
                    }
                    SwitchRow(stringResource(R.string.setting_voice), stringResource(R.string.setting_voice_text), settings.voice) { on ->
                        update { it.copy(voice = on) }
                    }
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.setting_get_ready), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(8.dp))
                        val options = listOf(0, 5, 10)
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            options.forEachIndexed { index, seconds ->
                                SegmentedButton(
                                    selected = settings.getReadySeconds == seconds,
                                    onClick = { update { it.copy(getReadySeconds = seconds) } },
                                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                                ) { Text(if (seconds == 0) stringResource(R.string.off) else "$seconds s") }
                            }
                        }
                    }
                }
            }

            item { SectionHeader(stringResource(R.string.settings_appearance)) }
            item {
                Group {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.setting_theme), style = MaterialTheme.typography.bodyLarge)
                        val modes = listOf(
                            ThemeMode.SYSTEM to R.string.theme_system,
                            ThemeMode.LIGHT to R.string.theme_light,
                            ThemeMode.DARK to R.string.theme_dark,
                        )
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            modes.forEachIndexed { index, (mode, label) ->
                                SegmentedButton(
                                    selected = settings.themeMode == mode,
                                    onClick = { update { it.copy(themeMode = mode) } },
                                    shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                                ) { Text(stringResource(label)) }
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SwitchRow(stringResource(R.string.setting_dynamic), stringResource(R.string.setting_dynamic_text), settings.dynamicColor) { on ->
                            update { it.copy(dynamicColor = on) }
                        }
                    }
                }
            }

            item { SectionHeader(stringResource(R.string.settings_data)) }
            item {
                Group {
                    ActionRow(
                        icon = Icons.Rounded.Upload,
                        title = stringResource(R.string.backup_export),
                        subtitle = stringResource(R.string.backup_export_text),
                        onClick = {
                            launchPicker(scope, snackbar, messageFailed) { exportLauncher.launch("fitplan-backup-${LocalDate.now()}.json") }
                        },
                    )
                    ActionRow(
                        icon = Icons.Rounded.Download,
                        title = stringResource(R.string.backup_import),
                        subtitle = stringResource(R.string.backup_import_text),
                        onClick = {
                            launchPicker(scope, snackbar, messageFailed) { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                        },
                    )
                    ActionRow(
                        icon = Icons.Rounded.RestartAlt,
                        title = stringResource(R.string.reset_all),
                        subtitle = stringResource(R.string.reset_all_text),
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { confirmReset = true },
                    )
                }
            }

            item { SectionHeader(stringResource(R.string.settings_about)) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodySmall)
                        Text(
                            stringResource(R.string.about_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (editName) {
        var name by remember { mutableStateOf(settings.name) }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text(stringResource(R.string.settings_name)) },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    update { it.copy(name = name.trim()) }
                    editName = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    pendingImport?.let { text ->
        ConfirmDialog(
            title = stringResource(R.string.backup_import),
            text = stringResource(R.string.backup_import_confirm),
            confirmLabel = stringResource(R.string.backup_import_action),
            destructive = true,
            onConfirm = {
                pendingImport = null
                scope.launch {
                    val ok = runCatching { container.repository.importJson(text) }.isSuccess
                    snackbar.showSnackbar(if (ok) messageImported else messageFailed)
                }
            },
            onDismiss = { pendingImport = null },
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.reset_all),
            text = stringResource(R.string.reset_all_confirm),
            confirmLabel = stringResource(R.string.reset_all_action),
            destructive = true,
            onConfirm = {
                confirmReset = false
                actions.launch { resetAll() }
            },
            onDismiss = { confirmReset = false },
        )
    }
}

/** Öffnet die Dateiauswahl; fehlt sie auf dem Gerät, gibt es einen Hinweis statt eines Absturzes. */
private fun launchPicker(scope: CoroutineScope, snackbar: SnackbarHostState, message: String, open: () -> Unit) {
    try {
        open()
    } catch (_: ActivityNotFoundException) {
        scope.launch { snackbar.showSnackbar(message) }
    }
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column { content() }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.primary) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
