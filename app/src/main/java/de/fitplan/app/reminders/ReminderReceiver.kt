package de.fitplan.app.reminders

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.fitplan.app.container
import de.fitplan.app.core.logic.AlarmKind
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.estimatedSeconds
import de.fitplan.app.core.logic.isPlanDone
import de.fitplan.app.core.logic.isScheduledOn
import de.fitplan.app.core.logic.pendingSupplements
import de.fitplan.app.core.logic.plan
import de.fitplan.app.core.model.TimeSlot
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Empfängt Wecker und Aktionen aus Benachrichtigungen. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.container
        app.appScope.launch {
            try {
                handle(context, intent)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val app = context.container
        val repository = app.repository
        val kind = intent.getStringExtra(EXTRA_KIND)
        val ref = intent.getStringExtra(EXTRA_REF) ?: return
        when (intent.action) {
            ACTION_FIRE -> {
                val data = repository.current()
                val today = repository.today()
                when (kind?.let { runCatching { AlarmKind.valueOf(it) }.getOrNull() }) {
                    AlarmKind.WORKOUT, AlarmKind.WORKOUT_FOLLOW_UP -> {
                        val plan = data.plan(ref)
                        val followUp = kind == AlarmKind.WORKOUT_FOLLOW_UP.name
                        if (plan != null &&
                            // Auch ein zurückgestellter Wecker richtet sich nach den aktuellen Einstellungen.
                            plan.reminder.enabled &&
                            (!followUp || plan.reminder.followUp) &&
                            plan.isScheduledOn(today) &&
                            !data.isPlanDone(plan.id, today.toEpochDay())
                        ) {
                            app.notifications.showWorkout(data, plan, followUp, data.currentStreak(today), today)
                        }
                    }
                    AlarmKind.SUPPLEMENT -> {
                        val slot = runCatching { TimeSlot.valueOf(ref) }.getOrNull() ?: return
                        val due = data.pendingSupplements(today, slot)
                        if (due.isNotEmpty() && data.settings.supplementReminders) {
                            app.notifications.showSupplements(slot, due, today)
                        }
                    }
                    null -> Unit
                }
                app.scheduler.reschedule(data)
                app.widgets.updateAll()
            }
            ACTION_SNOOZE -> {
                val alarmKind = kind?.let { runCatching { AlarmKind.valueOf(it) }.getOrNull() } ?: return
                if (alarmKind == AlarmKind.SUPPLEMENT) {
                    runCatching { TimeSlot.valueOf(ref) }.getOrNull()?.let { app.notifications.cancelSupplements(it) }
                } else {
                    app.notifications.cancelWorkout(ref)
                }
                app.scheduler.snooze(alarmKind, ref)
            }
            ACTION_WORKOUT_DONE -> {
                val data = repository.current()
                val plan = data.plan(ref) ?: return
                app.notifications.cancelWorkout(ref)
                val day = intent.getLongExtra(EXTRA_DAY, NO_DAY).takeIf { it != NO_DAY } ?: repository.today().toEpochDay()
                repository.logSession(plan.id, data.estimatedSeconds(plan), plan.steps.size, plan.steps.size, quickLog = true, epochDay = day)
            }
            ACTION_TAKE_ALL -> {
                val slot = kind?.let { runCatching { TimeSlot.valueOf(it) }.getOrNull() } ?: return
                val date = ref.toLongOrNull()?.let(LocalDate::ofEpochDay) ?: repository.today()
                app.notifications.cancelSupplements(slot)
                val change = repository.takeAll(date, slot)
                if (repository.current().settings.lowStockAlerts) change.lowStock.forEach(app.notifications::showLowStock)
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "de.fitplan.app.action.FIRE"
        const val ACTION_SNOOZE = "de.fitplan.app.action.SNOOZE"
        const val ACTION_WORKOUT_DONE = "de.fitplan.app.action.WORKOUT_DONE"
        const val ACTION_TAKE_ALL = "de.fitplan.app.action.TAKE_ALL"
        const val EXTRA_KIND = "kind"
        const val EXTRA_REF = "ref"
        const val EXTRA_DAY = "day"
        private const val NO_DAY = Long.MIN_VALUE
    }
}

/** Plant nach Neustart, Zeitumstellung oder App-Update alle Erinnerungen neu. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            -> Unit
            else -> return
        }
        val pending = goAsync()
        val app = context.container
        app.appScope.launch {
            try {
                app.scheduler.reschedule(app.repository.current())
                app.widgets.updateAll()
            } finally {
                pending.finish()
            }
        }
    }
}
