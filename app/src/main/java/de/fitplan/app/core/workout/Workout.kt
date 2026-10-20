package de.fitplan.app.core.workout

import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.PlanStep

enum class PhaseType { GET_READY, WORK, SWITCH_SIDE, REST }

enum class Side { LEFT, RIGHT }

data class Phase(
    val type: PhaseType,
    val stepIndex: Int,
    val exercise: Exercise,
    val mode: ExerciseMode,
    /** Dauer in Sekunden für zeitgesteuerte Phasen, 0 bei Wiederholungen. */
    val seconds: Int,
    val reps: Int,
    val set: Int,
    val totalSets: Int,
    val side: Side? = null,
) {
    val isTimed: Boolean get() = type != PhaseType.WORK || mode == ExerciseMode.TIMED
}

const val SWITCH_SIDE_SECONDS = 5

/** Geschätzte Sekunden pro Wiederholung für die Dauerschätzung. */
const val SECONDS_PER_REP = 3

/**
 * Wandelt die Schritte eines Plans in eine lineare Abfolge von Phasen um:
 * Vorbereitung → Übung (ggf. je Seite) → Pause → … Nach der letzten Übung gibt es keine Pause.
 */
fun buildPhases(
    steps: List<PlanStep>,
    findExercise: (String) -> Exercise?,
    getReadySeconds: Int,
): List<Phase> {
    val resolved = steps.mapNotNull { step -> findExercise(step.exerciseId)?.let { step to it } }
    if (resolved.isEmpty()) return emptyList()
    val phases = mutableListOf<Phase>()
    resolved.forEachIndexed { index, (step, exercise) ->
        val sets = step.sets.coerceAtLeast(1)
        val workSeconds = if (step.mode == ExerciseMode.TIMED) step.seconds.coerceAtLeast(1) else 0
        fun phase(type: PhaseType, set: Int, seconds: Int, side: Side? = null) = Phase(
            type = type,
            stepIndex = index,
            exercise = exercise,
            mode = step.mode,
            seconds = seconds,
            reps = step.reps,
            set = set,
            totalSets = sets,
            side = side,
        )
        if (index == 0 && getReadySeconds > 0) phases += phase(PhaseType.GET_READY, 1, getReadySeconds)
        for (set in 1..sets) {
            if (exercise.perSide) {
                phases += phase(PhaseType.WORK, set, workSeconds, Side.LEFT)
                if (step.mode == ExerciseMode.TIMED) phases += phase(PhaseType.SWITCH_SIDE, set, SWITCH_SIDE_SECONDS, Side.RIGHT)
                phases += phase(PhaseType.WORK, set, workSeconds, Side.RIGHT)
            } else {
                phases += phase(PhaseType.WORK, set, workSeconds)
            }
            val isLast = index == resolved.lastIndex && set == sets
            if (!isLast && step.restSeconds > 0) {
                // Die Pause zeigt bereits die kommende Übung bzw. den nächsten Satz.
                val (nextStep, nextExercise) = if (set < sets) step to exercise else resolved[index + 1]
                val nextSet = if (set < sets) set + 1 else 1
                phases += Phase(
                    type = PhaseType.REST,
                    stepIndex = if (set < sets) index else index + 1,
                    exercise = nextExercise,
                    mode = nextStep.mode,
                    seconds = step.restSeconds,
                    reps = nextStep.reps,
                    set = nextSet,
                    totalSets = nextStep.sets.coerceAtLeast(1),
                )
            }
        }
    }
    return phases
}

fun estimatedSeconds(phases: List<Phase>): Int = phases.sumOf { phase ->
    if (phase.isTimed) phase.seconds else phase.reps * SECONDS_PER_REP
}

fun estimatedSeconds(steps: List<PlanStep>, findExercise: (String) -> Exercise?): Int =
    estimatedSeconds(buildPhases(steps, findExercise, getReadySeconds = 0))
