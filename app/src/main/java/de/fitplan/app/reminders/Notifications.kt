package de.fitplan.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import de.fitplan.app.MainActivity
import de.fitplan.app.R
import de.fitplan.app.core.logic.AlarmKind
import de.fitplan.app.core.logic.daysLeft
import de.fitplan.app.core.logic.estimatedMinutes
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.ui.common.label
import java.time.LocalDate

class Notifications(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannels() {
        val system = context.getSystemService(NotificationManager::class.java)
        system.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_WORKOUT, context.getString(R.string.channel_workout), NotificationManager.IMPORTANCE_HIGH).apply {
                    description = context.getString(R.string.channel_workout_desc)
                },
                NotificationChannel(CHANNEL_SUPPLEMENTS, context.getString(R.string.channel_supplements), NotificationManager.IMPORTANCE_HIGH).apply {
                    description = context.getString(R.string.channel_supplements_desc)
                },
                NotificationChannel(CHANNEL_STOCK, context.getString(R.string.channel_stock), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = context.getString(R.string.channel_stock_desc)
                },
            ),
        )
    }

    fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return manager.areNotificationsEnabled()
    }

    fun showWorkout(data: AppData, plan: WorkoutPlan, followUp: Boolean, streak: Int) {
        val minutes = data.estimatedMinutes(plan)
        val title = if (followUp) {
            context.getString(R.string.notif_workout_followup_title)
        } else {
            context.getString(R.string.notif_workout_title, plan.emoji)
        }
        val text = buildString {
            append(context.getString(R.string.notif_workout_text, plan.name, minutes, plan.steps.size))
            if (streak > 0) append(" · ").append(context.getString(R.string.notif_streak, streak))
        }
        val open = activityIntent(MainActivity.OPEN_PLAYER, plan.id, requestCode = plan.id.hashCode())
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_stat_fitplan)
            .setColor(ContextCompat.getColor(context, R.color.brand))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.notif_action_start), open)
            .addAction(0, context.getString(R.string.notif_action_snooze), broadcast(ReminderReceiver.ACTION_SNOOZE, AlarmKind.WORKOUT.name, plan.id))
            .addAction(0, context.getString(R.string.notif_action_done), broadcast(ReminderReceiver.ACTION_WORKOUT_DONE, AlarmKind.WORKOUT.name, plan.id))
            .build()
        post(workoutId(plan.id), notification)
    }

    fun showSupplements(slot: TimeSlot, supplements: List<Supplement>, date: LocalDate) {
        val names = supplements.joinToString { it.name }
        val inbox = NotificationCompat.InboxStyle()
        supplements.forEach { supplement ->
            val hint = if (supplement.hint == IntakeHint.ANY) "" else " · ${context.getString(supplement.hint.label)}"
            val dose = if (supplement.dose.isBlank()) "" else " (${supplement.dose})"
            inbox.addLine("${supplement.emoji} ${supplement.name}$dose$hint")
        }
        val title = context.getString(R.string.notif_supplements_title, context.getString(slot.label))
        inbox.setBigContentTitle(title)
        val notification = NotificationCompat.Builder(context, CHANNEL_SUPPLEMENTS)
            .setSmallIcon(R.drawable.ic_stat_fitplan)
            .setColor(ContextCompat.getColor(context, R.color.brand))
            .setContentTitle(title)
            .setContentText(names)
            .setStyle(inbox)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(activityIntent(MainActivity.OPEN_SUPPLEMENTS, null, requestCode = 2000 + slot.ordinal))
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.notif_action_take_all), broadcast(ReminderReceiver.ACTION_TAKE_ALL, slot.name, date.toEpochDay().toString()))
            .addAction(0, context.getString(R.string.notif_action_snooze), broadcast(ReminderReceiver.ACTION_SNOOZE, AlarmKind.SUPPLEMENT.name, slot.name))
            .build()
        post(supplementId(slot), notification)
    }

    fun showLowStock(supplement: Supplement) {
        val days = supplement.daysLeft() ?: return
        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(R.drawable.ic_stat_fitplan)
            .setColor(ContextCompat.getColor(context, R.color.brand))
            .setContentTitle(context.getString(R.string.notif_stock_title, supplement.emoji, supplement.name))
            .setContentText(context.resources.getQuantityString(R.plurals.notif_stock_text, days, days))
            .setContentIntent(activityIntent(MainActivity.OPEN_SUPPLEMENTS, null, requestCode = 3000))
            .setAutoCancel(true)
            .build()
        post(stockId(supplement.id), notification)
    }

    fun showTest() {
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_stat_fitplan)
            .setColor(ContextCompat.getColor(context, R.color.brand))
            .setContentTitle(context.getString(R.string.notif_test_title))
            .setContentText(context.getString(R.string.notif_test_text))
            .setContentIntent(activityIntent(null, null, requestCode = 9999))
            .setAutoCancel(true)
            .build()
        post(TEST_ID, notification)
    }

    fun cancelWorkout(planId: String) = manager.cancel(workoutId(planId))

    fun cancelSupplements(slot: TimeSlot) = manager.cancel(supplementId(slot))

    private fun post(id: Int, notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.notify(id, notification)
    }

    private fun activityIntent(open: String?, planId: String?, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(MainActivity.EXTRA_OPEN, open)
            .putExtra(MainActivity.EXTRA_PLAN_ID, planId)
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun broadcast(action: String, kind: String, ref: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(action)
            .setData("fitplan://action/$action/$kind/$ref".toUri())
            .putExtra(ReminderReceiver.EXTRA_KIND, kind)
            .putExtra(ReminderReceiver.EXTRA_REF, ref)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val CHANNEL_WORKOUT = "workout"
        const val CHANNEL_SUPPLEMENTS = "supplements"
        const val CHANNEL_STOCK = "stock"
        private const val TEST_ID = 9999

        private fun workoutId(planId: String) = 1000 + Math.floorMod(planId.hashCode(), 900)
        private fun supplementId(slot: TimeSlot) = 2000 + slot.ordinal
        private fun stockId(supplementId: String) = 3000 + Math.floorMod(supplementId.hashCode(), 900)
    }
}
