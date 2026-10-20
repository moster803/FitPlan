package de.fitplan.app.ui.plans

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fitplan.app.R
import de.fitplan.app.core.library.Evidence
import de.fitplan.app.core.logic.allExercises
import de.fitplan.app.core.logic.findExercise
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.model.Reminder
import de.fitplan.app.core.workout.estimatedSeconds
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.ConfirmDialog
import de.fitplan.app.ui.common.DayChips
import de.fitplan.app.ui.common.SectionHeader
import de.fitplan.app.ui.common.SelectableCircle
import de.fitplan.app.ui.common.Stepper
import de.fitplan.app.ui.common.TimePickerDialog
import de.fitplan.app.ui.common.emoji
import de.fitplan.app.ui.common.formatMinute
import de.fitplan.app.ui.common.rememberToday
import de.fitplan.app.ui.common.stepSummary
import de.fitplan.app.ui.theme.ACCENT_COUNT
import de.fitplan.app.ui.theme.FitTheme

private val PLAN_EMOJIS = listOf("🧘", "🤸", "💪", "🦴", "🏃", "🚶", "🔥", "🌿", "🪑", "🌙", "☀️", "❤️", "⭐", "🏋️", "🙆", "🛌")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(data: AppData, planId: String?, templateKey: String?, onClose: () -> Unit) {
    val actions = LocalActions.current
    val today = rememberToday()
    val vm = viewModel { PlanEditorViewModel.create(data, planId, templateKey, today) }
    val plan = vm.plan
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var editingStepId by rememberSaveable { mutableStateOf<String?>(null) }
    var timeDialog by rememberSaveable { mutableStateOf<String?>(null) }

    val close: () -> Unit = {
        if (vm.isDirty) {
            confirmDiscard = true
        } else {
            onClose()
        }
    }
    BackHandler(onBack = close)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (vm.isNew) R.string.plan_new_title else R.string.plan_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = close) { Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close)) }
                },
                actions = {
                    TextButton(
                        enabled = vm.canSave,
                        onClick = {
                            val toSave = plan.copy(name = plan.name.trim())
                            actions.launch { upsertPlan(toSave) }
                            onClose()
                        },
                    ) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = plan.name,
                    onValueChange = { name -> vm.update { it.copy(name = name) } },
                    label = { Text(stringResource(R.string.field_plan_name)) },
                    placeholder = { Text(stringResource(R.string.field_plan_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SectionHeader(stringResource(R.string.field_icon))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PLAN_EMOJIS) { emoji ->
                        SelectableCircle(
                            selected = plan.emoji == emoji,
                            onClick = { vm.update { it.copy(emoji = emoji) } },
                            background = FitTheme.accent(plan.color).soft,
                        ) { Text(emoji, fontSize = 20.sp) }
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.field_color))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0 until ACCENT_COUNT).forEach { index ->
                        SelectableCircle(
                            selected = plan.color == index,
                            onClick = { vm.update { it.copy(color = index) } },
                            background = FitTheme.accent(index).strong,
                            size = 36.dp,
                        ) {
                            if (plan.color == index) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.field_days))
                DayChips(selected = plan.days, onChange = { days -> vm.update { it.copy(days = days) } })
            }
            item {
                ReminderCard(
                    reminder = plan.reminder,
                    onChange = { reminder -> vm.update { it.copy(reminder = reminder) } },
                    onPickTime = { timeDialog = it },
                )
            }
            item {
                val minutes = (estimatedSeconds(plan.steps, data::findExercise) + 59) / 60
                SectionHeader(stringResource(R.string.plan_exercises_header, plan.steps.size, minutes))
            }
            itemsIndexed(plan.steps, key = { _, step -> step.id }) { index, step ->
                val exercise = data.findExercise(step.exerciseId)
                StepRow(
                    index = index,
                    step = step,
                    exercise = exercise,
                    isFirst = index == 0,
                    isLast = index == plan.steps.lastIndex,
                    onClick = { editingStepId = step.id },
                    onMove = { delta -> vm.moveStep(index, delta) },
                    onDelete = { vm.removeStep(step.id) },
                )
            }
            item {
                OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.plan_add_exercise))
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.plan_active), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.plan_active_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = plan.active, onCheckedChange = { active -> vm.update { it.copy(active = active) } })
                }
            }
            if (!vm.isNew) {
                item {
                    TextButton(
                        onClick = { confirmDelete = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.plan_delete))
                    }
                }
            }
        }
    }

    timeDialog?.let { which ->
        val reminder = plan.reminder
        TimePickerDialog(
            initialMinute = if (which == "main") reminder.minuteOfDay else reminder.followUpMinuteOfDay,
            onDismiss = { timeDialog = null },
            onConfirm = { minute ->
                vm.update {
                    it.copy(reminder = if (which == "main") reminder.copy(minuteOfDay = minute) else reminder.copy(followUpMinuteOfDay = minute))
                }
                timeDialog = null
            },
        )
    }
    if (showPicker) {
        ExercisePickerSheet(
            exercises = data.allExercises(),
            onDismiss = { showPicker = false },
            onPick = { exercise ->
                vm.addExercise(exercise)
                showPicker = false
            },
            onCreate = { exercise ->
                actions.launch { upsertCustomExercise(exercise) }
                vm.addExercise(exercise)
                showPicker = false
            },
        )
    }
    editingStepId?.let { id ->
        val step = plan.steps.firstOrNull { it.id == id }
        if (step == null) {
            editingStepId = null
        } else {
            StepEditSheet(
                step = step,
                exercise = data.findExercise(step.exerciseId),
                onChange = vm::updateStep,
                onDismiss = { editingStepId = null },
            )
        }
    }
    if (confirmDiscard) {
        ConfirmDialog(
            title = stringResource(R.string.discard_title),
            text = stringResource(R.string.discard_text),
            confirmLabel = stringResource(R.string.discard),
            destructive = true,
            onConfirm = {
                confirmDiscard = false
                onClose()
            },
            onDismiss = { confirmDiscard = false },
        )
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.plan_delete),
            text = stringResource(R.string.plan_delete_text, plan.name),
            confirmLabel = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                actions.launch { deletePlan(plan.id) }
                confirmDelete = false
                onClose()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, onChange: (Reminder) -> Unit, onPickTime: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.padding(top = 12.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.reminder), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = reminder.enabled, onCheckedChange = { onChange(reminder.copy(enabled = it)) })
            }
            if (reminder.enabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.reminder_time), modifier = Modifier.weight(1f))
                    TextButton(onClick = { onPickTime("main") }) {
                        Text(formatMinute(reminder.minuteOfDay), style = MaterialTheme.typography.titleLarge)
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.reminder_followup))
                        Text(
                            stringResource(R.string.reminder_followup_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = reminder.followUp, onCheckedChange = { onChange(reminder.copy(followUp = it)) })
                }
                if (reminder.followUp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.reminder_followup_time), modifier = Modifier.weight(1f))
                        TextButton(onClick = { onPickTime("followup") }) {
                            Text(formatMinute(reminder.followUpMinuteOfDay), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    if (reminder.followUpMinuteOfDay <= reminder.minuteOfDay) {
                        Text(
                            stringResource(R.string.reminder_followup_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(
    index: Int,
    step: PlanStep,
    exercise: Exercise?,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    exercise?.name ?: stringResource(R.string.unknown_exercise),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val rest = if (step.restSeconds > 0) " · " + stringResource(R.string.rest_short, step.restSeconds) else ""
                Text(
                    stepSummary(step, exercise?.perSide == true) + rest,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onMove(-1) }, enabled = !isFirst) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.move_up))
            }
            IconButton(onClick = { onMove(1) }, enabled = !isLast) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.move_down))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepEditSheet(step: PlanStep, exercise: Exercise?, onChange: (PlanStep) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(exercise?.name ?: stringResource(R.string.unknown_exercise), style = MaterialTheme.typography.titleLarge)
            if (exercise != null) ExerciseDetails(exercise)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = step.mode == ExerciseMode.REPS,
                    onClick = { onChange(step.copy(mode = ExerciseMode.REPS)) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(stringResource(R.string.mode_reps)) }
                SegmentedButton(
                    selected = step.mode == ExerciseMode.TIMED,
                    onClick = { onChange(step.copy(mode = ExerciseMode.TIMED)) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(stringResource(R.string.mode_timed)) }
            }
            if (step.mode == ExerciseMode.REPS) {
                Stepper(stringResource(R.string.mode_reps), step.reps, { onChange(step.copy(reps = it)) }, 1..100)
            } else {
                Stepper(
                    stringResource(R.string.field_duration),
                    step.seconds,
                    { onChange(step.copy(seconds = it)) },
                    5..600,
                    step = 5,
                    format = { "$it s" },
                )
            }
            Stepper(stringResource(R.string.field_sets), step.sets, { onChange(step.copy(sets = it)) }, 1..10)
            Stepper(
                stringResource(R.string.field_rest),
                step.restSeconds,
                { onChange(step.copy(restSeconds = it)) },
                0..180,
                step = 5,
                format = { "$it s" },
            )
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.done)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    exercises: List<Exercise>,
    onDismiss: () -> Unit,
    onPick: (Exercise) -> Unit,
    onCreate: (Exercise) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    val filtered = exercises.filter {
        it.matchesFilter(filter) &&
            (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) || it.description.contains(query.trim(), ignoreCase = true))
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxHeight(0.9f)) {
            Text(
                stringResource(R.string.plan_add_exercise),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_exercises)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ApproachFilterRow(
                selected = filter,
                onSelect = { filter = it },
                showCustom = exercises.any { !it.builtIn },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .imePadding(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    TextButton(onClick = { creating = true }, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.exercise_new))
                    }
                }
                items(filtered, key = { it.id }) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(exercise) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(exercise.approach?.let { Evidence.info(it).emoji } ?: exercise.category.emoji, fontSize = 22.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                exerciseKind(exercise) + " · " + defaultAmount(exercise),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
    if (creating) {
        ExerciseEditorDialog(
            existing = null,
            onDismiss = { creating = false },
            onSave = { exercise ->
                creating = false
                onCreate(exercise)
            },
        )
    }
}
