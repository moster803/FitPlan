package de.fitplan.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import androidx.core.net.toUri
import de.fitplan.app.core.logic.AlarmKind
import de.fitplan.app.core.logic.PlannedAlarm
import de.fitplan.app.core.logic.planAlarms
import de.fitplan.app.core.model.AppData
import java.time.LocalDateTime
import java.time.ZoneId

/** Plant die Erinnerungen über den AlarmManager. Es gibt immer nur den jeweils nächsten Wecker pro Plan bzw. Tageszeit. */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val prefs = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)

    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    @Synchronized
    fun reschedule(data: AppData, now: LocalDateTime = LocalDateTime.now()) {
        val alarms = planAlarms(data, now)
        val keys = alarms.map { it.key }.toSet()
        val previous = prefs.getStringSet(PREF_KEYS, emptySet()).orEmpty()
        (previous - keys).forEach { cancel(it) }
        alarms.forEach { set(it.key, it.kind, it.refId, it.at) }
        prefs.edit { putStringSet(PREF_KEYS, keys) }
    }

    /** Einmalige Erinnerung in [minutes] Minuten, unabhängig vom regulären Plan. */
    fun snooze(kind: AlarmKind, refId: String, minutes: Long = SNOOZE_MINUTES) {
        val alarm = PlannedAlarm(kind, refId, LocalDateTime.now().plusMinutes(minutes))
        set("SNOOZE:${alarm.key}", kind, refId, alarm.at)
    }

    private fun set(key: String, kind: AlarmKind, refId: String, at: LocalDateTime) {
        val triggerAt = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pendingIntent(key, kind, refId, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun cancel(key: String) {
        val kind = AlarmKind.entries.firstOrNull { key.startsWith("${it.name}:") } ?: return
        val pending = pendingIntent(key, kind, key.substringAfter(':'), PendingIntent.FLAG_NO_CREATE) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(key: String, kind: AlarmKind, refId: String, flags: Int): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_FIRE)
            // Die URI macht jeden Wecker zu einem eigenen PendingIntent.
            .setData("fitplan://alarm/$key".toUri())
            .putExtra(ReminderReceiver.EXTRA_KIND, kind.name)
            .putExtra(ReminderReceiver.EXTRA_REF, refId)
        return PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private const val PREF_KEYS = "scheduled_keys"
        const val SNOOZE_MINUTES = 30L
    }
}
