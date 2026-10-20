package de.fitplan.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fitplan.app.R
import de.fitplan.app.core.library.BackTips
import de.fitplan.app.core.logic.DayState
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.daysLeft
import de.fitplan.app.core.logic.dueSupplements
import de.fitplan.app.core.logic.estimatedMinutes
import de.fitplan.app.core.logic.isLowStock
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.logic.longestStreak
import de.fitplan.app.core.logic.plansScheduledOn
import de.fitplan.app.core.logic.sessionFor
import de.fitplan.app.core.logic.week
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.model.WorkoutSession
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.CheckCircle
import de.fitplan.app.ui.common.EmojiAvatar
import de.fitplan.app.ui.common.InfoCard
import de.fitplan.app.ui.common.Pill
import de.fitplan.app.ui.common.SectionHeader
import de.fitplan.app.ui.common.WEEKDAY_DAY_MONTH
import de.fitplan.app.ui.common.feelingEmoji
import de.fitplan.app.ui.common.formatClock
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.common.icon
import de.fitplan.app.ui.common.label
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.common.weekdayShort
import de.fitplan.app.ui.theme.FitTheme
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    data: AppData,
    onStartPlan: (String) -> Unit,
    onOpenPlans: () -> Unit,
    onOpenSupplements: () -> Unit,
    onAddSupplement: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val actions = LocalActions.current
    val today = rememberToday()
    val epochDay = today.toEpochDay()
    val plansToday = data.plansScheduledOn(today)
    val slots = TimeSlot.entries.map { it to data.dueSupplements(today, it) }.filter { it.second.isNotEmpty() }
    val lowStock = data.supplements.filter { it.active && it.isLowStock() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(name = data.settings.name, today = today, onOpenSettings = onOpenSettings) }
        item {
            val week = data.week(today)
            StreakCard(
                streak = data.currentStreak(today),
                best = data.longestStreak(today),
                todayDone = data.sessions.any { it.epochDay == epochDay },
                week = week,
                today = today,
            )
        }

        item { SectionHeader(stringResource(R.string.home_training_today)) }
        when {
            data.plans.isEmpty() -> item {
                InfoCard(
                    icon = Icons.Rounded.Spa,
                    title = stringResource(R.string.home_no_plan_title),
                    text = stringResource(R.string.home_no_plan_text),
                    action = { Button(onClick = onOpenPlans) { Text(stringResource(R.string.home_create_plan)) } },
                )
            }
            plansToday.isEmpty() -> item {
                InfoCard(
                    icon = Icons.Rounded.Spa,
                    title = stringResource(R.string.home_rest_day_title),
                    text = stringResource(R.string.home_rest_day_text),
                    action = { TextButton(onClick = onOpenPlans) { Text(stringResource(R.string.home_choose_workout)) } },
                )
            }
            else -> plansToday.forEach { plan ->
                item(key = "plan-${plan.id}") {
                    val session = data.sessionFor(plan.id, epochDay)
                    WorkoutTodayCard(
                        plan = plan,
                        minutes = data.estimatedMinutes(plan),
                        session = session,
                        onStart = { onStartPlan(plan.id) },
                        onQuickDone = {
                            actions.cancelWorkoutNotification(plan.id)
                            actions.launch {
                                logSession(plan.id, data.estimatedMinutes(plan) * 60, plan.steps.size, plan.steps.size, quickLog = true)
                            }
                        },
                        onUndo = { session?.let { s -> actions.launch { deleteSession(s.id) } } },
                    )
                }
            }
        }

        item {
            SectionHeader(stringResource(R.string.home_supplements_today)) {
                if (data.supplements.isNotEmpty()) {
                    TextButton(onClick = onOpenSupplements) { Text(stringResource(R.string.show_all)) }
                }
            }
        }
        when {
            data.supplements.isEmpty() -> item {
                InfoCard(
                    icon = Icons.Rounded.Inventory2,
                    title = stringResource(R.string.home_no_supplements_title),
                    text = stringResource(R.string.home_no_supplements_text),
                    action = { FilledTonalButton(onClick = onAddSupplement) { Text(stringResource(R.string.supplement_add)) } },
                )
            }
            slots.isEmpty() -> item {
                Text(
                    stringResource(R.string.home_no_supplements_due),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> slots.forEach { (slot, supplements) ->
                item(key = "slot-${slot.name}") {
                    SupplementSlotCard(
                        slot = slot,
                        minute = data.settings.slotMinute(slot),
                        supplements = supplements,
                        isTaken = { data.isTaken(it.id, epochDay, slot) },
                        onToggle = { actions.toggleIntake(it.id, today, slot) },
                        onTakeAll = { actions.takeAll(today, slot) },
                    )
                }
            }
        }
        if (lowStock.isNotEmpty()) {
            item {
                LowStockCard(lowStock, onClick = onOpenSupplements)
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            InfoCard(
                icon = Icons.Rounded.Lightbulb,
                title = stringResource(R.string.home_tip_title),
                text = BackTips.forDay(epochDay),
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun Header(name: String, today: LocalDate, onOpenSettings: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = stringResource(
        when (hour) {
            in 5..10 -> R.string.greeting_morning
            in 11..16 -> R.string.greeting_day
            in 17..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        },
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(
                text = if (name.isBlank()) stringResource(R.string.greeting_plain, greeting) else stringResource(R.string.greeting_named, greeting, name),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = today.format(WEEKDAY_DAY_MONTH),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings_title))
        }
    }
}

@Composable
private fun StreakCard(streak: Int, best: Int, todayDone: Boolean, week: List<Pair<LocalDate, DayState>>, today: LocalDate) {
    val extra = FitTheme.extra
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(extra.flameContainer),
                    contentAlignment = Alignment.Center,
                ) { Text("🔥", fontSize = 28.sp) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = pluralStringResource(R.plurals.streak_days, streak, streak),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = stringResource(
                            when {
                                todayDone -> R.string.streak_today_done
                                streak == 0 -> R.string.streak_start
                                else -> R.string.streak_keep_going
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (best > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("🏆 $best", style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.streak_record), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            WeekStrip(week, today)
        }
    }
}

@Composable
private fun WeekStrip(week: List<Pair<LocalDate, DayState>>, today: LocalDate) {
    val names = weekdayShort()
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        week.forEachIndexed { index, (date, state) ->
            val isToday = date == today
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    names[index],
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
                val (background, border) = when (state) {
                    DayState.DONE -> scheme.primary to null
                    DayState.MISSED -> scheme.surface.copy(alpha = 0.5f) to null
                    DayState.OPEN -> Color.Transparent to BorderStroke(2.dp, if (isToday) scheme.primary else scheme.outline.copy(alpha = 0.5f))
                    DayState.REST -> Color.Transparent to null
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(background)
                        .then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    when (state) {
                        DayState.DONE -> Icon(Icons.Rounded.Check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(20.dp))
                        DayState.MISSED -> Text("·", style = MaterialTheme.typography.titleMedium)
                        DayState.REST -> Text("–", style = MaterialTheme.typography.bodySmall, color = scheme.onPrimaryContainer.copy(alpha = 0.5f))
                        DayState.OPEN -> Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutTodayCard(
    plan: WorkoutPlan,
    minutes: Int,
    session: WorkoutSession?,
    onStart: () -> Unit,
    onQuickDone: () -> Unit,
    onUndo: () -> Unit,
) {
    val accent = FitTheme.accent(plan.color)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(plan.emoji, accent)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(R.string.plan_meta, minutes, plan.steps.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (plan.reminder.enabled && session == null) {
                    Pill(text = "🔔 " + formatMinute(plan.reminder.minuteOfDay))
                }
            }
            if (session != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckCircle(checked = true, color = FitTheme.extra.success)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.home_done_at, formatClock(session.finishedAt)) +
                            (feelingEmoji(session.feeling)?.let { "  $it" } ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (session.quickLog) {
                        TextButton(onClick = onUndo) { Text(stringResource(R.string.undo)) }
                    } else {
                        TextButton(onClick = onStart) { Text(stringResource(R.string.home_again)) }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onStart, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.start))
                    }
                    OutlinedButton(onClick = onQuickDone) { Text(stringResource(R.string.home_already_done)) }
                }
            }
        }
    }
}

@Composable
private fun SupplementSlotCard(
    slot: TimeSlot,
    minute: Int,
    supplements: List<Supplement>,
    isTaken: (Supplement) -> Boolean,
    onToggle: (Supplement) -> Unit,
    onTakeAll: () -> Unit,
) {
    val takenCount = supplements.count(isTaken)
    val allTaken = takenCount == supplements.size
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(slot.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${stringResource(slot.label)} · ${formatMinute(minute)}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (allTaken) {
                    Pill(
                        text = stringResource(R.string.all_taken),
                        container = FitTheme.extra.success.copy(alpha = 0.15f),
                        content = FitTheme.extra.success,
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Text("$takenCount/${supplements.size}", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = onTakeAll) { Text(stringResource(R.string.take_all)) }
                }
            }
            supplements.forEach { supplement ->
                SupplementRow(supplement, taken = isTaken(supplement), onClick = { onToggle(supplement) })
            }
        }
    }
}

@Composable
fun SupplementRow(supplement: Supplement, taken: Boolean, onClick: () -> Unit) {
    val accent = FitTheme.accent(supplement.color)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiAvatar(supplement.emoji, accent, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                supplement.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (taken) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            val details = listOfNotNull(
                supplement.dose.takeIf { it.isNotBlank() },
                if (supplement.hint != IntakeHint.ANY) stringResource(supplement.hint.label) else null,
            ).joinToString(" · ")
            if (details.isNotEmpty()) {
                Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        CheckCircle(checked = taken, color = accent.strong)
    }
}

@Composable
private fun LowStockCard(supplements: List<Supplement>, onClick: () -> Unit) {
    val extra = FitTheme.extra
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = extra.warningContainer, contentColor = extra.onWarningContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Inventory2, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.low_stock_title), style = MaterialTheme.typography.titleSmall)
                val text = buildList {
                    supplements.forEach { supplement ->
                        val days = supplement.daysLeft() ?: 0
                        add("${supplement.name} (${pluralStringResource(R.plurals.days_count, days, days)})")
                    }
                }.joinToString()
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
