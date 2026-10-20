package de.fitplan.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.fitplan.app.MainActivity
import de.fitplan.app.R
import de.fitplan.app.container
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.dueSupplements
import de.fitplan.app.core.logic.isPlanDone
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.logic.plansScheduledOn
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.TimeSlot
import java.time.LocalDate

/** Homescreen-Widget: Serie, heutiges Training und Supplement-Fortschritt auf einen Blick. */
class FitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = context.container.repository.current()
        val today = LocalDate.now()
        provideContent {
            GlanceTheme { WidgetContent(context, data, today) }
        }
    }
}

class FitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FitWidget()
}

class WidgetUpdater(private val context: Context) {
    suspend fun updateAll() {
        runCatching { FitWidget().updateAll(context) }
    }
}

@Composable
private fun WidgetContent(context: Context, data: AppData, today: LocalDate) {
    val streak = data.currentStreak(today)
    val epochDay = today.toEpochDay()
    val plans = data.plansScheduledOn(today)
    val openPlan = plans.firstOrNull { !data.isPlanDone(it.id, epochDay) }
    var due = 0
    var taken = 0
    TimeSlot.entries.forEach { slot ->
        data.dueSupplements(today, slot).forEach {
            due++
            if (data.isTaken(it.id, epochDay, slot)) taken++
        }
    }
    val openApp = Intent(context, MainActivity::class.java)
    val openPlayer = openPlan?.let {
        Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN, MainActivity.OPEN_PLAYER)
            .putExtra(MainActivity.EXTRA_PLAN_ID, it.id)
    }
    val openSupplements = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN, MainActivity.OPEN_SUPPLEMENTS)
    val colors = GlanceTheme.colors

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.widgetBackground)
            .cornerRadius(24.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(openApp)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxWidth()) {
            Text("🔥 $streak", style = TextStyle(color = colors.onSurface, fontSize = 26.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.width(8.dp))
            Text(
                context.resources.getQuantityString(R.plurals.widget_streak_label, streak),
                style = TextStyle(color = colors.onSurfaceVariant, fontSize = 13.sp),
            )
        }
        Spacer(GlanceModifier.height(10.dp))
        val trainingText = when {
            plans.isEmpty() -> context.getString(R.string.widget_rest_day)
            openPlan == null -> context.getString(R.string.widget_training_done)
            else -> context.getString(R.string.widget_training_open, openPlan.emoji, openPlan.name)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(if (openPlan != null) colors.primaryContainer else colors.secondaryContainer)
                .cornerRadius(16.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable(actionStartActivity(openPlayer ?: openApp)),
        ) {
            Text(
                trainingText,
                maxLines = 1,
                style = TextStyle(
                    color = if (openPlan != null) colors.onPrimaryContainer else colors.onSecondaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        if (due > 0) {
            Spacer(GlanceModifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(colors.secondaryContainer)
                    .cornerRadius(16.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable(actionStartActivity(openSupplements)),
            ) {
                Text(
                    context.getString(R.string.widget_supplements, taken, due),
                    maxLines = 1,
                    style = TextStyle(color = colors.onSecondaryContainer, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}
