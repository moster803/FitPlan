package de.fitplan.app.ui.common

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import de.fitplan.app.R
import de.fitplan.app.ui.LocalContainer
import de.fitplan.app.ui.theme.FitTheme

fun isSamsung(): Boolean = Build.MANUFACTURER.equals("samsung", ignoreCase = true)

fun isIgnoringBatteryOptimizations(context: Context): Boolean =
    context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) == true

private fun Context.startSafely(vararg intents: Intent) {
    for (intent in intents) {
        try {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
            // Nächste Alternative probieren.
        } catch (_: SecurityException) {
            // Nächste Alternative probieren.
        }
    }
}

fun Context.openNotificationSettings() = startSafely(
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()),
)

/**
 * Alles, was für zuverlässige Erinnerungen nötig ist: Benachrichtigungen, pünktliche Wecker
 * und – gerade auf Samsung-Geräten wichtig – die Ausnahme von der Akku-Optimierung.
 */
@Composable
fun ReminderPermissions(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = LocalContainer.current
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }
    val notificationsOk = remember(refresh) { container.notifications.canPost() }
    val exactOk = remember(refresh) { container.scheduler.canScheduleExact() }
    val batteryOk = remember(refresh) { isIgnoringBatteryOptimizations(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        refresh++
        if (!granted) context.openNotificationSettings()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PermissionRow(
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.perm_notifications),
            text = stringResource(R.string.perm_notifications_text),
            ok = notificationsOk,
            onFix = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.openNotificationSettings()
                }
            },
        )
        if (!exactOk && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionRow(
                icon = Icons.Rounded.Alarm,
                title = stringResource(R.string.perm_exact),
                text = stringResource(R.string.perm_exact_text),
                ok = false,
                onFix = {
                    context.startSafely(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri()),
                    )
                },
            )
        }
        PermissionRow(
            icon = Icons.Rounded.BatteryAlert,
            title = stringResource(R.string.perm_battery),
            text = stringResource(R.string.perm_battery_text),
            ok = batteryOk,
            onFix = {
                @Suppress("BatteryLife")
                context.startSafely(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${context.packageName}".toUri()),
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                )
            },
        )
        if (isSamsung()) {
            InfoCard(
                icon = Icons.Rounded.PhoneAndroid,
                title = stringResource(R.string.samsung_title),
                text = stringResource(R.string.samsung_text),
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, title: String, text: String, ok: Boolean, onFix: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (ok) Icons.Rounded.CheckCircle else icon,
                contentDescription = null,
                tint = if (ok) FitTheme.extra.success else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (ok) stringResource(R.string.perm_ok) else text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!ok) {
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(onClick = onFix) { Text(stringResource(R.string.perm_fix)) }
            }
        }
    }
}
