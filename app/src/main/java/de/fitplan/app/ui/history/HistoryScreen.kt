package de.fitplan.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fitplan.app.R
import de.fitplan.app.core.logic.DayState
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.dayState
import de.fitplan.app.core.logic.dueSupplements
import de.fitplan.app.core.logic.feelingHistory
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.logic.longestStreak
import de.fitplan.app.core.logic.monthSummary
import de.fitplan.app.core.logic.plan
import de.fitplan.app.core.logic.supplementDays
import de.fitplan.app.core.logic.totalMinutes
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutSession
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.ColumnChart
import de.fitplan.app.ui.common.ColumnDatum
import de.fitplan.app.ui.common.ConfirmDialog
import de.fitplan.app.ui.common.DAY_MONTH
import de.fitplan.app.ui.common.EmptyState
import de.fitplan.app.ui.common.FEELING_EMOJIS
import de.fitplan.app.ui.common.GERMAN
import de.fitplan.app.ui.common.LineChart
import de.fitplan.app.ui.common.LinePoint
import de.fitplan.app.ui.common.MONTH_YEAR
import de.fitplan.app.ui.common.SectionHeader
import de.fitplan.app.ui.common.feelingEmoji
import de.fitplan.app.ui.common.formatClock
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.common.weekdayShort
import de.fitplan.app.ui.theme.FitTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val SESSION_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EE, d. MMM", GERMAN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(data: AppData) {
    val actions = LocalActions.current
    val today = rememberToday()
    var monthOffset by rememberSaveable { mutableLongStateOf(0L) }
    var deleting by remember { mutableStateOf<WorkoutSession?>(null) }
    val month = YearMonth.from(today).minusMonths(monthOffset)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.history_title)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StatsGrid(data, today) }
            item {
                MonthCalendar(
                    data = data,
                    month = month,
                    today = today,
                    canGoForward = monthOffset > 0,
                    onPrevious = { monthOffset++ },
                    onNext = { if (monthOffset > 0) monthOffset-- },
                )
            }
            item { FeelingCard(data, today) }
            if (data.supplements.isNotEmpty()) item { SupplementTrendCard(data, today) }
            item { SectionHeader(stringResource(R.string.history_recent)) }
            val recent = data.sessions.sortedByDescending { it.finishedAt }.take(20)
            if (recent.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📈",
                        title = stringResource(R.string.history_empty_title),
                        text = stringResource(R.string.history_empty_text),
                    )
                }
            }
            items(recent, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    planName = data.plan(session.planId)?.let { "${it.emoji} ${it.name}" } ?: stringResource(R.string.deleted_plan),
                    onDelete = { deleting = session },
                )
            }
        }
    }

    deleting?.let { session ->
        ConfirmDialog(
            title = stringResource(R.string.session_delete_title),
            text = stringResource(R.string.session_delete_text),
            confirmLabel = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                actions.launch { deleteSession(session.id) }
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun StatsGrid(data: AppData, today: LocalDate) {
    val month = data.monthSummary(today)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("🔥", data.currentStreak(today).toString(), stringResource(R.string.stat_streak), Modifier.weight(1f))
            StatTile("🏆", data.longestStreak(today).toString(), stringResource(R.string.stat_best_streak), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("✅", month.sessions.toString(), stringResource(R.string.stat_month_sessions), Modifier.weight(1f))
            StatTile("⏱", data.totalMinutes().toString(), stringResource(R.string.stat_total_minutes), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(6.dp))
                Text(emoji, fontSize = 20.sp)
            }
        }
    }
}

private enum class SupplementState { COMPLETE, PARTIAL, NONE }

private fun AppData.supplementState(date: LocalDate): SupplementState? {
    var due = 0
    var taken = 0
    TimeSlot.entries.forEach { slot ->
        dueSupplements(date, slot).forEach {
            due++
            if (isTaken(it.id, date.toEpochDay(), slot)) taken++
        }
    }
    return when {
        due == 0 -> null
        taken == due -> SupplementState.COMPLETE
        taken > 0 -> SupplementState.PARTIAL
        else -> SupplementState.NONE
    }
}

@Composable
private fun MonthCalendar(
    data: AppData,
    month: YearMonth,
    today: LocalDate,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val extra = FitTheme.extra
    val names = weekdayShort()
    val offset = month.atDay(1).dayOfWeek.value - 1
    val cells = offset + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    Card(colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) { Icon(Icons.Rounded.ChevronLeft, contentDescription = stringResource(R.string.previous_month)) }
                Text(
                    month.format(MONTH_YEAR).replaceFirstChar { it.titlecase(GERMAN) },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                IconButton(onClick = onNext, enabled = canGoForward) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = stringResource(R.string.next_month))
                }
            }
            Row(Modifier.fillMaxWidth()) {
                names.forEach { name ->
                    Text(
                        name,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (column in 0 until 7) {
                        val dayNumber = row * 7 + column - offset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (dayNumber in 1..month.lengthOfMonth()) {
                                val date = month.atDay(dayNumber)
                                val future = date.isAfter(today)
                                val state = if (future) DayState.OPEN else data.dayState(date, today)
                                val isToday = date == today
                                val background = when (state) {
                                    DayState.DONE -> scheme.primary
                                    DayState.MISSED -> scheme.errorContainer.copy(alpha = 0.6f)
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(background)
                                        .then(if (isToday) Modifier.border(2.dp, scheme.primary, CircleShape) else Modifier),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        dayNumber.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            state == DayState.DONE -> scheme.onPrimary
                                            future -> scheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            else -> scheme.onSurface
                                        },
                                    )
                                    val supplementState = if (future) null else data.supplementState(date)
                                    if (supplementState != null && supplementState != SupplementState.NONE) {
                                        Box(
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 3.dp)
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (supplementState == SupplementState.COMPLETE) extra.success else extra.warning),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                LegendDot(scheme.primary, stringResource(R.string.legend_done))
                LegendDot(scheme.errorContainer, stringResource(R.string.legend_missed))
                if (data.supplements.isNotEmpty()) {
                    LegendDot(extra.success, stringResource(R.string.legend_supplements_complete), small = true)
                    LegendDot(extra.warning, stringResource(R.string.legend_supplements_partial), small = true)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, small: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(if (small) 6.dp else 10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeelingCard(data: AppData, today: LocalDate) {
    val days = 30
    val history = data.feelingHistory(today, days)
    val start = today.minusDays(days - 1L)
    val labels = listOf(R.string.feeling_1, R.string.feeling_2, R.string.feeling_3, R.string.feeling_4, R.string.feeling_5).map { stringResource(it) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.feeling_chart_title), style = MaterialTheme.typography.titleMedium)
            if (history.isEmpty()) {
                Text(
                    stringResource(R.string.feeling_chart_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                val average = history.map { it.feeling }.average()
                Text(
                    stringResource(R.string.feeling_average, "%.1f".format(GERMAN, average), FEELING_EMOJIS[(Math.round(average).toInt() - 1).coerceIn(0, 4)]),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                val points = history.map { point ->
                    val date = LocalDate.ofEpochDay(point.epochDay)
                    LinePoint(
                        x = (point.epochDay - start.toEpochDay()).toFloat(),
                        y = point.feeling.toFloat(),
                        detail = "${date.format(DAY_MONTH)}: ${FEELING_EMOJIS[point.feeling - 1]} ${labels[point.feeling - 1]}",
                    )
                }
                LineChart(
                    points = points,
                    xRange = 0f..(days - 1).toFloat(),
                    yMin = 1f,
                    yMax = 5f,
                    yLabels = listOf(1f to FEELING_EMOJIS[0], 3f to FEELING_EMOJIS[2], 5f to FEELING_EMOJIS[4]),
                    xLabels = listOf(
                        0f to start.format(DAY_MONTH),
                        (days - 1).toFloat() to stringResource(R.string.today),
                    ),
                    description = points.joinToString { it.detail },
                )
            }
        }
    }
}

@Composable
private fun SupplementTrendCard(data: AppData, today: LocalDate) {
    val days = data.supplementDays(today, 14)
    val names = weekdayShort()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.supplement_trend_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.supplement_trend_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val chartData = days.map { day ->
                ColumnDatum(
                    label = names[day.date.dayOfWeek.value - 1].take(1),
                    value = if (day.due == 0) null else day.taken.toFloat() / day.due,
                    detail = if (day.due == 0) {
                        stringResource(R.string.chart_no_intake, day.date.format(DAY_MONTH))
                    } else {
                        stringResource(R.string.chart_intake_detail, day.date.format(DAY_MONTH), day.taken, day.due)
                    },
                )
            }
            ColumnChart(data = chartData, description = chartData.joinToString { it.detail })
        }
    }
}

@Composable
private fun SessionRow(session: WorkoutSession, planName: String, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(planName, style = MaterialTheme.typography.titleSmall)
                val date = LocalDate.ofEpochDay(session.epochDay).format(SESSION_DATE)
                val detail = if (session.quickLog) {
                    stringResource(R.string.session_quick, date, formatClock(session.finishedAt))
                } else {
                    stringResource(
                        R.string.session_detail,
                        date,
                        formatClock(session.finishedAt),
                        (session.durationSeconds + 59) / 60,
                        session.completedExercises,
                        session.totalExercises,
                    )
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            feelingEmoji(session.feeling)?.let { Text(it, fontSize = 22.sp) }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}
