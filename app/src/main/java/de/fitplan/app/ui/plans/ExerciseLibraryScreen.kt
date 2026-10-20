package de.fitplan.app.ui.plans

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.fitplan.app.R
import de.fitplan.app.core.library.Evidence
import de.fitplan.app.core.logic.allExercises
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.ui.LocalActions
import de.fitplan.app.ui.common.ConfirmDialog
import de.fitplan.app.ui.common.EmojiAvatar
import de.fitplan.app.ui.common.emoji
import de.fitplan.app.ui.common.label
import de.fitplan.app.ui.evidence.SourceLinks
import de.fitplan.app.ui.theme.FitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(data: AppData, initialFilter: String?, onBack: () -> Unit, onOpenEvidence: () -> Unit) {
    val actions = LocalActions.current
    var filter by rememberSaveable { mutableStateOf(initialFilter) }
    var editing by remember { mutableStateOf<Exercise?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Exercise?>(null) }
    val exercises = data.allExercises().filter { it.matchesFilter(filter) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.exercise_new)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    onClick = onOpenEvidence,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Science, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.evidence_title),
                                style = MaterialTheme.typography.titleSmall,
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
                ApproachFilterRow(
                    selected = filter,
                    onSelect = { filter = it },
                    showCustom = data.customExercises.isNotEmpty(),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(exercises, key = { it.id }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onEdit = { editing = exercise }.takeUnless { exercise.builtIn },
                    onDelete = { deleting = exercise }.takeUnless { exercise.builtIn },
                )
            }
        }
    }

    if (creating || editing != null) {
        ExerciseEditorDialog(
            existing = editing,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { exercise ->
                actions.launch { upsertCustomExercise(exercise) }
                creating = false
                editing = null
            },
        )
    }
    deleting?.let { exercise ->
        ConfirmDialog(
            title = stringResource(R.string.exercise_delete_title),
            text = stringResource(R.string.exercise_delete_text, exercise.name),
            confirmLabel = stringResource(R.string.delete),
            destructive = true,
            onConfirm = {
                actions.launch { deleteCustomExercise(exercise.id) }
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onEdit: (() -> Unit)?, onDelete: (() -> Unit)?) {
    var expanded by rememberSaveable(exercise.id) { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(
                    exercise.approach?.let { Evidence.info(it).emoji } ?: exercise.category.emoji,
                    FitTheme.accent(exercise.approach?.ordinal ?: exercise.category.ordinal),
                    size = 40.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        exerciseKind(exercise) + " · " + defaultAmount(exercise),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null)
            }
            if (expanded) {
                ExerciseDetails(exercise)
                if (onEdit != null || onDelete != null) {
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.align(Alignment.End)) {
                        onEdit?.let {
                            IconButton(onClick = it) { Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit)) }
                        }
                        onDelete?.let {
                            IconButton(onClick = it) { Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseDetails(exercise: Exercise, showEvidence: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (exercise.description.isNotBlank()) Text(exercise.description, style = MaterialTheme.typography.bodyMedium)
        if (exercise.equipment.isNotBlank()) {
            Text(
                "🧰 " + stringResource(R.string.equipment, exercise.equipment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        exercise.steps.forEachIndexed { index, step ->
            Row {
                Text("${index + 1}.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(24.dp))
                Text(step, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (exercise.tip.isNotBlank()) {
            Text("💡 " + exercise.tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (showEvidence) {
            val approach = exercise.approach
            if (approach != null) {
                val info = Evidence.info(approach)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${info.emoji} ${stringResource(R.string.evidence_basis)}: ${info.name}",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(info.summary, style = MaterialTheme.typography.bodySmall)
                        SourceLinks(info.sourceIds)
                    }
                }
            } else if (!exercise.builtIn) {
                Text(
                    stringResource(R.string.custom_exercise_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Übungsform (z. B. „Pilates“) bzw. Kategorie bei eigenen Übungen. */
@Composable
fun exerciseKind(exercise: Exercise): String =
    exercise.approach?.let { Evidence.info(it).name } ?: stringResource(exercise.category.label)

/** Filterwert: null = alle, Name einer [Approach] oder [FILTER_CUSTOM]. */
const val FILTER_CUSTOM = "custom"

fun Exercise.matchesFilter(filter: String?): Boolean = when (filter) {
    null -> true
    FILTER_CUSTOM -> !builtIn
    else -> approach?.name == filter
}

@Composable
fun ApproachFilterRow(selected: String?, onSelect: (String?) -> Unit, showCustom: Boolean, modifier: Modifier = Modifier) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text(stringResource(R.string.filter_all)) })
        }
        items(Evidence.approaches, key = { it.approach.name }) { info ->
            FilterChip(
                selected = selected == info.approach.name,
                onClick = { onSelect(info.approach.name) },
                label = { Text(info.emoji + " " + info.name) },
            )
        }
        if (showCustom) {
            item {
                FilterChip(
                    selected = selected == FILTER_CUSTOM,
                    onClick = { onSelect(FILTER_CUSTOM) },
                    label = { Text("✏️ " + stringResource(R.string.filter_custom)) },
                )
            }
        }
    }
}

@Composable
fun defaultAmount(exercise: Exercise): String {
    val amount = when (exercise.mode) {
        ExerciseMode.TIMED -> stringResource(R.string.step_seconds, exercise.seconds)
        ExerciseMode.REPS -> stringResource(R.string.step_reps, exercise.reps)
    }
    return if (exercise.perSide) amount + " " + stringResource(R.string.per_side) else amount
}
