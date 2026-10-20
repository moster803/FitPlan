package de.fitplan.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.fitplan.app.R
import de.fitplan.app.core.data.newId
import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.library.PlanTemplates.toPlan
import de.fitplan.app.core.library.SupplementTemplates
import de.fitplan.app.core.library.toSupplement
import de.fitplan.app.core.workout.estimatedSeconds
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.EmojiAvatar
import de.fitplan.app.ui.common.ReminderPermissions
import de.fitplan.app.ui.common.TimePickerDialog
import de.fitplan.app.ui.common.daysSummary
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.theme.FitTheme
import java.time.LocalDate

private const val STEPS = 4
private val SUGGESTED_TIMES = listOf(6 * 60 + 30, 7 * 60, 8 * 60, 12 * 60, 18 * 60, 20 * 60)

@Composable
fun OnboardingScreen() {
    val actions = LocalActions.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var templates by rememberSaveable { mutableStateOf(listOf(PlanTemplates.dailyBack.key)) }
    var reminderMinute by rememberSaveable { mutableIntStateOf(8 * 60) }
    var supplements by rememberSaveable { mutableStateOf(listOf<String>()) }
    var customTime by rememberSaveable { mutableStateOf(false) }

    fun finish() {
        val today = LocalDate.now().toEpochDay()
        val chosenPlans = PlanTemplates.all.filter { it.key in templates }
        val chosenSupplements = SupplementTemplates.all.filter { it.key in supplements }.map { it.toSupplement(newId(), today) }
        actions.launch { completeOnboarding(name, chosenPlans, reminderMinute, chosenSupplements) }
    }

    BackHandler(enabled = step > 0) { step-- }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState > initialState
                    slideInHorizontally { if (forward) it else -it } togetherWith slideOutHorizontally { if (forward) -it else it }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding",
            ) { current ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (current) {
                        0 -> WelcomeStep(name, onName = { name = it })
                        1 -> TrainingStep(
                            selected = templates,
                            onToggle = { key -> templates = if (key in templates) templates - key else templates + key },
                            reminderMinute = reminderMinute,
                            onReminder = { reminderMinute = it },
                            onCustomTime = { customTime = true },
                        )
                        2 -> SupplementStep(
                            selected = supplements,
                            onToggle = { key -> supplements = if (key in supplements) supplements - key else supplements + key },
                        )
                        else -> RemindersStep()
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (step > 0) TextButton(onClick = { step-- }) { Text(stringResource(R.string.back)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(STEPS) { index ->
                        Box(
                            Modifier
                                .size(if (index == step) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        )
                    }
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Button(onClick = { if (step < STEPS - 1) step++ else finish() }) {
                        Text(
                            stringResource(
                                when (step) {
                                    0 -> R.string.onboarding_start
                                    STEPS - 1 -> R.string.onboarding_finish
                                    else -> R.string.next
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    if (customTime) {
        TimePickerDialog(
            initialMinute = reminderMinute,
            onDismiss = { customTime = false },
            onConfirm = {
                reminderMinute = it
                customTime = false
            },
        )
    }
}

@Composable
private fun ColumnScope.WelcomeStep(name: String, onName: (String) -> Unit) {
    Spacer(Modifier.height(24.dp))
    Box(
        Modifier
            .size(112.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(colorResource(R.color.icon_background))
            .align(Alignment.CenterHorizontally),
    ) {
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, modifier = Modifier.fillMaxSize())
    }
    Text(
        stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        stringResource(R.string.onboarding_welcome_text),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        Feature("🧘", stringResource(R.string.onboarding_feature_plans))
        Feature("🔔", stringResource(R.string.onboarding_feature_reminders))
        Feature("💊", stringResource(R.string.onboarding_feature_supplements))
        Feature("🔥", stringResource(R.string.onboarding_feature_streaks))
    }
    OutlinedTextField(
        value = name,
        onValueChange = onName,
        label = { Text(stringResource(R.string.onboarding_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Feature(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingStep(
    selected: List<String>,
    onToggle: (String) -> Unit,
    reminderMinute: Int,
    onReminder: (Int) -> Unit,
    onCustomTime: () -> Unit,
) {
    Text(stringResource(R.string.onboarding_training_title), style = MaterialTheme.typography.headlineSmall)
    Text(
        stringResource(R.string.onboarding_training_text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PlanTemplates.all.forEach { template ->
        val isSelected = template.key in selected
        val minutes = (estimatedSeconds(template.toPlan("x", 0) { "s" }.steps, ExerciseLibrary::find) + 59) / 60
        Card(
            onClick = { onToggle(template.key) },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(template.emoji, FitTheme.accent(template.color))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    Text(template.description, style = MaterialTheme.typography.bodySmall)
                    Text(
                        stringResource(R.string.minutes_short, minutes) + " · " + daysSummary(template.days),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    Text(stringResource(R.string.onboarding_time_title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
    Text(
        stringResource(R.string.onboarding_time_text),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SUGGESTED_TIMES.forEach { minute ->
            FilterChip(selected = reminderMinute == minute, onClick = { onReminder(minute) }, label = { Text(formatMinute(minute)) })
        }
        FilterChip(
            selected = reminderMinute !in SUGGESTED_TIMES,
            onClick = onCustomTime,
            label = {
                Text(if (reminderMinute in SUGGESTED_TIMES) stringResource(R.string.onboarding_other_time) else formatMinute(reminderMinute))
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupplementStep(selected: List<String>, onToggle: (String) -> Unit) {
    Text(stringResource(R.string.onboarding_supplements_title), style = MaterialTheme.typography.headlineSmall)
    Text(
        stringResource(R.string.onboarding_supplements_text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SupplementTemplates.all.forEach { template ->
            FilterChip(
                selected = template.key in selected,
                onClick = { onToggle(template.key) },
                label = { Text("${template.emoji} ${template.name}") },
            )
        }
    }
    Text(
        stringResource(R.string.supplement_disclaimer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun RemindersStep() {
    Text(stringResource(R.string.onboarding_reminders_title), style = MaterialTheme.typography.headlineSmall)
    Text(
        stringResource(R.string.onboarding_reminders_text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    ReminderPermissions()
}
