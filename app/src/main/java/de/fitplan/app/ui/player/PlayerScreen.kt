package de.fitplan.app.ui.player

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fitplan.app.R
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.workout.Phase
import de.fitplan.app.core.workout.PhaseType
import de.fitplan.app.core.workout.Side
import de.fitplan.app.ui.LocalContainer
import de.fitplan.app.ui.common.EmptyState
import de.fitplan.app.ui.common.FEELING_EMOJIS
import de.fitplan.app.ui.common.InfoCard
import de.fitplan.app.ui.common.formatDuration
import de.fitplan.app.ui.plans.ExerciseDetails
import de.fitplan.app.ui.theme.FitTheme
import kotlin.math.ceil

@Composable
fun PlayerScreen(planId: String, onClose: () -> Unit) {
    val container = LocalContainer.current
    val application = LocalContext.current.applicationContext as Application
    val vm = viewModel { PlayerViewModel(container, application, planId) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    var confirmQuit by remember { mutableStateOf(false) }

    KeepScreenOn()
    BackHandler {
        if (ui.state.finished || ui.phases.isEmpty()) {
            onClose()
        } else {
            confirmQuit = true
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            ui.loading -> Box(Modifier.fillMaxSize())
            ui.missing || ui.phases.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    emoji = "🤷",
                    title = stringResource(R.string.player_missing_title),
                    text = stringResource(R.string.player_missing_text),
                    action = { Button(onClick = onClose) { Text(stringResource(R.string.close)) } },
                )
            }
            ui.state.finished -> SummaryContent(ui, onFeeling = vm::setFeeling, onDone = onClose)
            else -> ActiveContent(
                ui = ui,
                onQuit = { confirmQuit = true },
                onTogglePause = vm::togglePause,
                onSkip = vm::skip,
                onPrevious = vm::previous,
                onRepsDone = vm::completeReps,
                onExtend = { vm.extend(10) },
            )
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text(stringResource(R.string.player_quit_title)) },
            text = { Text(stringResource(R.string.player_quit_text, ui.completedExercises, ui.totalExercises)) },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            vm.stop(keep = false)
                            confirmQuit = false
                            onClose()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(stringResource(R.string.discard)) }
                    if (ui.completedExercises > 0) {
                        TextButton(onClick = {
                            vm.stop(keep = true)
                            confirmQuit = false
                            onClose()
                        }) { Text(stringResource(R.string.player_save_quit)) }
                    }
                }
            },
            dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.player_continue)) } },
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

@Composable
private fun ActiveContent(
    ui: PlayerUi,
    onQuit: () -> Unit,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    onRepsDone: () -> Unit,
    onExtend: () -> Unit,
) {
    val phase = ui.phase ?: return
    val state = ui.state
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onQuit) { Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close)) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${ui.planEmoji} ${ui.planName}", style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.player_exercise_of, phase.stepIndex + 1, ui.totalExercises),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(48.dp))
        }
        val progress = (state.index + phaseFraction(phase, state.remainingMs)) / ui.phases.size
        val animatedProgress by animateFloatAsState(progress, label = "progress")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            strokeCap = StrokeCap.Round,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            PhaseBadge(phase)
            Spacer(Modifier.height(12.dp))
            AnimatedContent(targetState = phase.exercise.name, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "name") { name ->
                Text(name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            }
            val detail = phaseDetail(phase)
            if (detail.isNotEmpty()) {
                Text(detail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
            TimerRing(phase = phase, remainingMs = state.remainingMs, paused = state.paused)
            Spacer(Modifier.height(20.dp))
            when {
                phase.type == PhaseType.WORK && phase.mode == ExerciseMode.REPS -> {
                    Button(
                        onClick = onRepsDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.player_reps_done), style = MaterialTheme.typography.titleMedium)
                    }
                }
                phase.type == PhaseType.REST || phase.type == PhaseType.GET_READY -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onExtend) { Text(stringResource(R.string.player_plus_10)) }
                        Button(onClick = onSkip) { Text(stringResource(R.string.player_skip_rest)) }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            InstructionsCard(phase)
            nextWork(ui.phases, state.index)?.let { next ->
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.player_next, next.exercise.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = stringResource(R.string.player_previous))
            }
            FilledIconButton(
                onClick = onTogglePause,
                modifier = Modifier.size(80.dp),
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(
                    if (state.paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = stringResource(if (state.paused) R.string.player_resume else R.string.player_pause),
                    modifier = Modifier.size(40.dp),
                )
            }
            FilledTonalIconButton(onClick = onSkip, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Rounded.SkipNext, contentDescription = stringResource(R.string.player_skip))
            }
        }
    }
}

private fun phaseFraction(phase: Phase, remainingMs: Long): Float {
    if (!phase.isTimed || phase.seconds <= 0) return 0f
    return (1f - remainingMs / (phase.seconds * 1000f)).coerceIn(0f, 1f)
}

private fun nextWork(phases: List<Phase>, index: Int): Phase? {
    val current = phases.getOrNull(index) ?: return null
    return phases.drop(index + 1).firstOrNull { it.type == PhaseType.WORK && it.stepIndex != current.stepIndex }
}

@Composable
private fun PhaseBadge(phase: Phase) {
    val scheme = MaterialTheme.colorScheme
    val (label, container, content) = when (phase.type) {
        PhaseType.GET_READY -> Triple(stringResource(R.string.phase_get_ready), scheme.tertiaryContainer, scheme.onTertiaryContainer)
        PhaseType.WORK -> Triple(stringResource(R.string.phase_work), scheme.primary, scheme.onPrimary)
        PhaseType.SWITCH_SIDE -> Triple(stringResource(R.string.phase_switch), scheme.tertiaryContainer, scheme.onTertiaryContainer)
        PhaseType.REST -> Triple(stringResource(R.string.phase_rest), scheme.secondaryContainer, scheme.onSecondaryContainer)
    }
    Surface(color = container, contentColor = content, shape = CircleShape) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
    }
}

@Composable
private fun phaseDetail(phase: Phase): String {
    val parts = mutableListOf<String>()
    if (phase.totalSets > 1) parts += stringResource(R.string.player_set_of, phase.set, phase.totalSets)
    when (phase.side) {
        Side.LEFT -> parts += stringResource(R.string.side_left)
        Side.RIGHT -> parts += stringResource(R.string.side_right)
        null -> Unit
    }
    if (phase.type == PhaseType.REST) parts.add(0, stringResource(R.string.player_coming_up))
    return parts.joinToString(" · ")
}

@Composable
private fun TimerRing(phase: Phase, remainingMs: Long, paused: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val ringColor = when (phase.type) {
        PhaseType.WORK -> scheme.primary
        PhaseType.REST -> scheme.secondary
        else -> scheme.tertiary
    }
    val track = scheme.surfaceContainerHighest
    val fraction = if (phase.isTimed && phase.seconds > 0) (remainingMs / (phase.seconds * 1000f)).coerceIn(0f, 1f) else 1f
    Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
            drawArc(
                color = if (paused) ringColor.copy(alpha = 0.4f) else ringColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (phase.isTimed) {
                val seconds = ceil(remainingMs / 1000.0).toInt()
                Text(
                    text = if (seconds >= 60) formatDuration(seconds) else seconds.toString(),
                    fontSize = 72.sp,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (paused) scheme.onSurfaceVariant else scheme.onSurface,
                )
                Text(
                    stringResource(if (paused) R.string.player_paused else R.string.player_seconds),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                Text(phase.reps.toString(), fontSize = 72.sp, style = MaterialTheme.typography.displayLarge)
                Text(
                    pluralStringResource(R.plurals.player_reps_label, phase.reps),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InstructionsCard(phase: Phase) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.player_how_to), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null)
            }
            if (expanded) ExerciseDetails(phase.exercise)
        }
    }
}

@Composable
private fun SummaryContent(ui: PlayerUi, onFeeling: (Int) -> Unit, onDone: () -> Unit) {
    val extra = FitTheme.extra
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(if (ui.completedExercises > 0) "🎉" else "🙌", fontSize = 72.sp)
        Text(
            stringResource(if (ui.completedExercises > 0) R.string.summary_title else R.string.summary_title_skipped),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.summary_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("⏱", formatDuration(ui.durationSeconds), stringResource(R.string.summary_duration), Modifier.weight(1f))
            StatTile("✅", "${ui.completedExercises}/${ui.totalExercises}", stringResource(R.string.summary_exercises), Modifier.weight(1f))
            StatTile("🔥", ui.streak.toString(), stringResource(R.string.summary_streak), Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.summary_feeling_question), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FEELING_EMOJIS.forEachIndexed { index, emoji ->
                        val value = index + 1
                        val selected = ui.feeling == value
                        Surface(
                            onClick = { onFeeling(value) },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = if (selected) 32.sp else 26.sp) }
                        }
                    }
                }
                val labels = listOf(
                    R.string.feeling_1, R.string.feeling_2, R.string.feeling_3, R.string.feeling_4, R.string.feeling_5,
                )
                ui.feeling?.let { Text(stringResource(labels[it - 1]), style = MaterialTheme.typography.labelLarge) }
            }
        }
        if ((ui.feeling ?: 5) <= 2) {
            InfoCard(
                icon = Icons.Rounded.Info,
                title = stringResource(R.string.summary_pain_title),
                text = stringResource(R.string.summary_pain_text),
                container = extra.warningContainer,
                content = extra.onWarningContainer,
            )
        }
        Button(onClick = onDone, modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)) {
            Text(stringResource(R.string.done), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
