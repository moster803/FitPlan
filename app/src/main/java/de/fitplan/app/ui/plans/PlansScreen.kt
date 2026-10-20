package de.fitplan.app.ui.plans

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.fitplan.app.R
import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.library.PlanTemplates.toPlan
import de.fitplan.app.core.logic.allExercises
import de.fitplan.app.core.logic.currentPlanStreak
import de.fitplan.app.core.logic.estimatedMinutes
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.workout.estimatedSeconds
import de.fitplan.app.ui.common.EmojiAvatar
import de.fitplan.app.ui.common.EmptyState
import de.fitplan.app.ui.common.Pill
import de.fitplan.app.ui.common.daysSummary
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.theme.FitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    data: AppData,
    onStartPlan: (String) -> Unit,
    onEditPlan: (String) -> Unit,
    onNewPlan: (String?) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenEvidence: () -> Unit,
) {
    var showTemplates by rememberSaveable { mutableStateOf(false) }
    val today = rememberToday()
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.plans_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTemplates = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.plans_new)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (data.plans.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🧘",
                        title = stringResource(R.string.plans_empty_title),
                        text = stringResource(R.string.plans_empty_text),
                        action = { Button(onClick = { showTemplates = true }) { Text(stringResource(R.string.plans_choose_template)) } },
                    )
                }
            }
            items(data.plans, key = { it.id }) { plan ->
                PlanCard(
                    plan = plan,
                    minutes = data.estimatedMinutes(plan),
                    streak = data.currentPlanStreak(plan, today),
                    onClick = { onEditPlan(plan.id) },
                    onStart = { onStartPlan(plan.id) },
                )
            }
            item {
                Card(
                    onClick = onOpenLibrary,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.library_title), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.library_subtitle, data.allExercises().size),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                    }
                }
            }
            item {
                Card(
                    onClick = onOpenEvidence,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Science, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.evidence_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            Text(
                                stringResource(R.string.evidence_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.exercise_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
    if (showTemplates) {
        TemplateSheet(
            onDismiss = { showTemplates = false },
            onSelect = { key ->
                showTemplates = false
                onNewPlan(key)
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanCard(plan: WorkoutPlan, minutes: Int, streak: Int, onClick: () -> Unit, onStart: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(plan.emoji, FitTheme.accent(plan.color), size = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(daysSummary(plan.days))
                    Pill(
                        if (plan.reminder.enabled) "🔔 " + formatMinute(plan.reminder.minuteOfDay) else "🔕 " + stringResource(R.string.off),
                    )
                    Pill("⏱ " + stringResource(R.string.minutes_short, minutes))
                    if (streak > 0) Pill("🔥 $streak")
                    if (!plan.active) {
                        Pill(
                            stringResource(R.string.paused),
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onStart, enabled = plan.steps.isNotEmpty()) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.start))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSheet(onDismiss: () -> Unit, onSelect: (String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                stringResource(R.string.plans_new_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            PlanTemplates.all.forEach { template ->
                val plan = template.toPlan("preview", 0) { "s" }
                val minutes = (estimatedSeconds(plan.steps, ExerciseLibrary::find) + 59) / 60
                TemplateRow(
                    emoji = template.emoji,
                    color = template.color,
                    title = template.name,
                    subtitle = template.description,
                    meta = stringResource(R.string.minutes_short, minutes) + " · " + daysSummary(template.days),
                    onClick = { onSelect(template.key) },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            TemplateRow(
                emoji = "✏️",
                color = 3,
                title = stringResource(R.string.plans_blank),
                subtitle = stringResource(R.string.plans_blank_text),
                meta = null,
                onClick = { onSelect(null) },
            )
        }
    }
}

@Composable
private fun TemplateRow(emoji: String, color: Int, title: String, subtitle: String, meta: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiAvatar(emoji, FitTheme.accent(color))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (meta != null) Text(meta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
