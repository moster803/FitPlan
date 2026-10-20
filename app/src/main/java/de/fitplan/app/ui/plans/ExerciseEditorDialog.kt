package de.fitplan.app.ui.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.fitplan.app.R
import de.fitplan.app.core.data.newId
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseCategory
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.ui.common.Stepper
import de.fitplan.app.ui.common.label

/** Dialog zum Anlegen oder Bearbeiten einer eigenen Übung, z. B. von der Physiotherapie. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditorDialog(existing: Exercise?, onDismiss: () -> Unit, onSave: (Exercise) -> Unit) {
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(existing?.description.orEmpty()) }
    var steps by rememberSaveable { mutableStateOf(existing?.steps?.joinToString("\n").orEmpty()) }
    var tip by rememberSaveable { mutableStateOf(existing?.tip.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: ExerciseCategory.STRENGTH) }
    var mode by rememberSaveable { mutableStateOf(existing?.mode ?: ExerciseMode.REPS) }
    var seconds by rememberSaveable { mutableIntStateOf(existing?.seconds ?: 30) }
    var reps by rememberSaveable { mutableIntStateOf(existing?.reps ?: 10) }
    var perSide by rememberSaveable { mutableStateOf(existing?.perSide ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing == null) R.string.exercise_new else R.string.exercise_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.field_category), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseCategory.entries.forEach { item ->
                        FilterChip(selected = category == item, onClick = { category = item }, label = { Text(stringResource(item.label)) })
                    }
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == ExerciseMode.REPS,
                        onClick = { mode = ExerciseMode.REPS },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(stringResource(R.string.mode_reps)) }
                    SegmentedButton(
                        selected = mode == ExerciseMode.TIMED,
                        onClick = { mode = ExerciseMode.TIMED },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(stringResource(R.string.mode_timed)) }
                }
                if (mode == ExerciseMode.REPS) {
                    Stepper(stringResource(R.string.mode_reps), reps, { reps = it }, 1..100)
                } else {
                    Stepper(stringResource(R.string.field_seconds), seconds, { seconds = it }, 5..600, step = 5)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.field_per_side), modifier = Modifier.weight(1f))
                    Switch(checked = perSide, onCheckedChange = { perSide = it })
                }
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text(stringResource(R.string.field_steps)) },
                    supportingText = { Text(stringResource(R.string.field_steps_hint)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    label = { Text(stringResource(R.string.field_tip)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        Exercise(
                            id = existing?.id ?: "custom-${newId()}",
                            name = name.trim(),
                            description = description.trim(),
                            steps = steps.lines().map { it.trim() }.filter { it.isNotEmpty() },
                            tip = tip.trim(),
                            category = category,
                            mode = mode,
                            seconds = seconds,
                            reps = reps,
                            perSide = perSide,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
