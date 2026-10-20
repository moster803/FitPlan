package de.fitplan.app.ui.supplements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fitplan.app.R
import de.fitplan.app.core.library.SupplementTemplates
import de.fitplan.app.core.logic.daysLeft
import de.fitplan.app.core.logic.interactionHints
import de.fitplan.app.core.logic.isLowStock
import de.fitplan.app.core.logic.supplementAdherence
import de.fitplan.app.core.logic.supplementDays
import de.fitplan.app.core.logic.weekStart
import de.fitplan.app.core.model.ALL_DAYS
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.ColumnChart
import de.fitplan.app.ui.common.ColumnDatum
import de.fitplan.app.ui.common.DAY_MONTH
import de.fitplan.app.ui.common.EmojiAvatar
import de.fitplan.app.ui.common.EmptyState
import de.fitplan.app.ui.common.InfoCard
import de.fitplan.app.ui.common.Pill
import de.fitplan.app.ui.common.TimePickerDialog
import de.fitplan.app.ui.common.daysSummary
import de.fitplan.app.ui.common.emoji
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.common.icon
import de.fitplan.app.ui.common.label
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.common.unitText
import de.fitplan.app.ui.common.weekdayShort
import de.fitplan.app.ui.theme.FitTheme
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementsScreen(data: AppData, onEdit: (String) -> Unit, onAdd: (String?) -> Unit) {
    var showTemplates by rememberSaveable { mutableStateOf(false) }
    var showTimes by rememberSaveable { mutableStateOf(false) }
    val today = rememberToday()
    val hints = interactionHints(data.supplements)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.supplements_title)) },
                actions = {
                    IconButton(onClick = { showTimes = true }) {
                        Icon(Icons.Rounded.Schedule, contentDescription = stringResource(R.string.slot_times_title))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTemplates = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (data.supplements.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "💊",
                        title = stringResource(R.string.supplements_empty_title),
                        text = stringResource(R.string.supplements_empty_text),
                        action = { Button(onClick = { showTemplates = true }) { Text(stringResource(R.string.supplement_add)) } },
                    )
                }
            } else {
                item { AdherenceCard(data, today) }
                items(hints) { hint ->
                    InfoCard(
                        icon = Icons.Rounded.Warning,
                        title = stringResource(R.string.hint_title, hint.first.name, hint.second.name, stringResource(hint.slot.label)),
                        text = hint.message,
                        container = FitTheme.extra.warningContainer,
                        content = FitTheme.extra.onWarningContainer,
                    )
                }
                items(data.supplements, key = { it.id }) { supplement ->
                    SupplementCard(
                        supplement = supplement,
                        slotMinute = data.settings::slotMinute,
                        today = today,
                        onClick = { onEdit(supplement.id) },
                    )
                }
                item {
                    Text(
                        stringResource(R.string.supplement_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    if (showTemplates) {
        SupplementTemplateSheet(
            onDismiss = { showTemplates = false },
            onSelect = { key ->
                showTemplates = false
                onAdd(key)
            },
        )
    }
    if (showTimes) SlotTimesSheet(data, onDismiss = { showTimes = false })
}

@Composable
private fun AdherenceCard(data: AppData, today: LocalDate) {
    val start = weekStart(today)
    val adherence = data.supplementAdherence(start, today)
    val names = weekdayShort()
    val days = data.supplementDays(today, 7)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.adherence_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                adherence?.let { stringResource(R.string.percent, (it * 100).roundToInt()) } ?: "–",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(stringResource(R.string.adherence_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            val chartData = days.map { day ->
                val value = if (day.due == 0) null else day.taken.toFloat() / day.due
                ColumnDatum(
                    label = names[day.date.dayOfWeek.value - 1],
                    value = value,
                    detail = if (day.due == 0) {
                        stringResource(R.string.chart_no_intake, day.date.format(DAY_MONTH))
                    } else {
                        stringResource(R.string.chart_intake_detail, day.date.format(DAY_MONTH), day.taken, day.due)
                    },
                )
            }
            ColumnChart(
                data = chartData,
                description = chartData.joinToString { it.detail },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupplementCard(supplement: Supplement, slotMinute: (TimeSlot) -> Int, today: LocalDate, onClick: () -> Unit) {
    val accent = FitTheme.accent(supplement.color)
    val ended = supplement.endEpochDay?.let { it < today.toEpochDay() } == true
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(supplement.emoji, accent)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(supplement.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(supplement.dose.takeIf { it.isNotBlank() }, stringResource(supplement.form.label)).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!supplement.active || ended) {
                    Pill(
                        stringResource(if (ended) R.string.course_ended else R.string.paused),
                        container = MaterialTheme.colorScheme.errorContainer,
                        content = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                supplement.slots.sorted().forEach { slot ->
                    Pill("${stringResource(slot.label)} ${formatMinute(slotMinute(slot))}", icon = slot.icon)
                }
                if (supplement.hint != IntakeHint.ANY) Pill("${supplement.hint.emoji} ${stringResource(supplement.hint.label)}")
                if (supplement.days != ALL_DAYS) Pill(daysSummary(supplement.days))
                supplement.endEpochDay?.let { Pill(stringResource(R.string.course_until, LocalDate.ofEpochDay(it).format(DAY_MONTH))) }
            }
            if (supplement.trackStock) StockRow(supplement)
        }
    }
}

@Composable
private fun StockRow(supplement: Supplement) {
    val actions = LocalActions.current
    val low = supplement.isLowStock()
    val extra = FitTheme.extra
    val daysLeft = supplement.daysLeft()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(
            progress = { (supplement.stock.toFloat() / supplement.packSize.coerceAtLeast(1)).coerceIn(0f, 1f) },
            color = if (low) extra.warning else MaterialTheme.colorScheme.primary,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildString {
                    append(unitText(supplement.form, supplement.stock))
                    if (daysLeft != null) append(" · ").append(pluralStringResource(R.plurals.stock_days_left, daysLeft, daysLeft))
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (low) extra.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { actions.launch { refill(supplement.id, supplement.packSize) } }) {
                Text(stringResource(R.string.refill, supplement.packSize))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplementTemplateSheet(onDismiss: () -> Unit, onSelect: (String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.navigationBarsPadding()) {
            Text(
                stringResource(R.string.supplement_template_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Text(
                stringResource(R.string.supplement_template_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    TemplateTile("✏️", stringResource(R.string.supplement_custom), 3) { onSelect(null) }
                }
                items(SupplementTemplates.all, key = { it.key }) { template ->
                    TemplateTile(template.emoji, template.name, template.color) { onSelect(template.key) }
                }
            }
        }
    }
}

@Composable
private fun TemplateTile(emoji: String, name: String, color: Int, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = FitTheme.accent(color).soft)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotTimesSheet(data: AppData, onDismiss: () -> Unit) {
    val actions = LocalActions.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editing by rememberSaveable { mutableStateOf<TimeSlot?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.slot_times_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.slot_times_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.supplement_reminders), modifier = Modifier.weight(1f))
                Switch(
                    checked = data.settings.supplementReminders,
                    onCheckedChange = { enabled -> actions.launch { updateSettings { it.copy(supplementReminders = enabled) } } },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            TimeSlot.entries.forEach { slot ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(slot.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(slot.label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { editing = slot }) {
                        Text(formatMinute(data.settings.slotMinute(slot)), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            InfoCard(
                icon = Icons.Rounded.Info,
                title = stringResource(R.string.slot_times_info_title),
                text = stringResource(R.string.slot_times_info_text),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    editing?.let { slot ->
        TimePickerDialog(
            initialMinute = data.settings.slotMinute(slot),
            title = stringResource(slot.label),
            onDismiss = { editing = null },
            onConfirm = { minute ->
                actions.launch { updateSettings { it.copy(slotTimes = it.slotTimes + (slot to minute)) } }
                editing = null
            },
        )
    }
}
