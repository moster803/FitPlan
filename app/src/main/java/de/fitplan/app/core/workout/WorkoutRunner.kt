package de.fitplan.app.core.workout

sealed interface RunnerEvent {
    data class PhaseStarted(val index: Int) : RunnerEvent
    data class Countdown(val secondsLeft: Int) : RunnerEvent
    data object Finished : RunnerEvent
}

/**
 * Unveränderlicher Zustand eines laufenden Trainings. Die Uhr wird von außen über [advance]
 * vorangetrieben, dadurch ist der Ablauf ohne Android testbar.
 */
data class RunnerState(
    val index: Int = 0,
    val remainingMs: Long = 0,
    val paused: Boolean = false,
    val finished: Boolean = false,
    val activeMs: Long = 0,
    /** Indizes der Übungs-Phasen, die tatsächlich absolviert (nicht übersprungen) wurden. */
    val completedWork: Set<Int> = emptySet(),
) {
    fun completedExercises(phases: List<Phase>): Int =
        completedWork.mapNotNull { phases.getOrNull(it)?.stepIndex }.distinct().size

    companion object {
        fun start(phases: List<Phase>): RunnerState =
            if (phases.isEmpty()) RunnerState(finished = true) else RunnerState(remainingMs = phases[0].durationMs())
    }
}

private fun Phase.durationMs(): Long = if (isTimed) seconds * 1000L else 0L

data class RunnerResult(val state: RunnerState, val events: List<RunnerEvent>)

private fun RunnerState.goTo(phases: List<Phase>, target: Int, events: MutableList<RunnerEvent>): RunnerState {
    if (target > phases.lastIndex) {
        events += RunnerEvent.Finished
        return copy(index = phases.lastIndex, remainingMs = 0, finished = true)
    }
    val safe = target.coerceAtLeast(0)
    events += RunnerEvent.PhaseStarted(safe)
    return copy(index = safe, remainingMs = phases[safe].durationMs())
}

/** Lässt [deltaMs] Millisekunden verstreichen. Zeitgesteuerte Phasen wechseln automatisch. */
fun RunnerState.advance(phases: List<Phase>, deltaMs: Long): RunnerResult {
    if (finished || paused || deltaMs <= 0) return RunnerResult(this, emptyList())
    val events = mutableListOf<RunnerEvent>()
    var state = copy(activeMs = activeMs + deltaMs)
    var budget = deltaMs
    while (!state.finished && budget > 0) {
        val phase = phases[state.index]
        if (!phase.isTimed) break // Wiederholungen: wartet auf „Erledigt“.
        val before = state.remainingMs
        val after = before - budget
        // Countdown-Signale für die letzten drei Sekunden.
        for (second in 3 downTo 1) {
            val mark = second * 1000L
            if (before > mark && after <= mark) events += RunnerEvent.Countdown(second)
        }
        if (after > 0) {
            state = state.copy(remainingMs = after)
            budget = 0
        } else {
            budget = -after
            val completed = if (phase.type == PhaseType.WORK) state.completedWork + state.index else state.completedWork
            state = state.copy(completedWork = completed).goTo(phases, state.index + 1, events)
        }
    }
    return RunnerResult(state, events)
}

/** Wiederholungs-Übung als erledigt markieren. */
fun RunnerState.completeReps(phases: List<Phase>): RunnerResult {
    if (finished) return RunnerResult(this, emptyList())
    val events = mutableListOf<RunnerEvent>()
    val phase = phases[index]
    val completed = if (phase.type == PhaseType.WORK) completedWork + index else completedWork
    return RunnerResult(copy(completedWork = completed, paused = false).goTo(phases, index + 1, events), events)
}

fun RunnerState.skip(phases: List<Phase>): RunnerResult {
    if (finished) return RunnerResult(this, emptyList())
    val events = mutableListOf<RunnerEvent>()
    return RunnerResult(goTo(phases, index + 1, events), events)
}

/** Zurück zum Anfang der aktuellen Übung bzw. zur vorherigen Übungs-Phase. */
fun RunnerState.previous(phases: List<Phase>): RunnerResult {
    if (phases.isEmpty()) return RunnerResult(this, emptyList())
    val events = mutableListOf<RunnerEvent>()
    val current = phases[index]
    val restarted = current.isTimed && remainingMs < current.durationMs() - 2000
    val target = if (restarted) {
        index
    } else {
        (index - 1 downTo 0).firstOrNull { phases[it].type == PhaseType.WORK } ?: 0
    }
    return RunnerResult(copy(finished = false).goTo(phases, target, events), events)
}

fun RunnerState.togglePause(): RunnerState = if (finished) this else copy(paused = !paused)

/** Pause um [seconds] verlängern (z. B. „+10 s“). */
fun RunnerState.extend(phases: List<Phase>, seconds: Int): RunnerState {
    if (finished || !phases[index].isTimed) return this
    return copy(remainingMs = remainingMs + seconds * 1000L)
}
