package de.fitplan.app.ui.player

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fitplan.app.AppContainer
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.findExercise
import de.fitplan.app.core.logic.plan
import de.fitplan.app.core.workout.Phase
import de.fitplan.app.core.workout.RunnerEvent
import de.fitplan.app.core.workout.RunnerResult
import de.fitplan.app.core.workout.RunnerState
import de.fitplan.app.core.workout.advance
import de.fitplan.app.core.workout.buildPhases
import de.fitplan.app.core.workout.completeReps
import de.fitplan.app.core.workout.extend
import de.fitplan.app.core.workout.previous
import de.fitplan.app.core.workout.skip
import de.fitplan.app.core.workout.togglePause
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUi(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val planName: String = "",
    val planEmoji: String = "",
    val phases: List<Phase> = emptyList(),
    val state: RunnerState = RunnerState(),
    val totalExercises: Int = 0,
    val sessionId: String? = null,
    val streak: Int = 0,
    val feeling: Int? = null,
) {
    val phase: Phase? get() = phases.getOrNull(state.index)
    val completedExercises: Int get() = state.completedExercises(phases)
    val durationSeconds: Int get() = (state.activeMs / 1000).toInt()
}

/** Steuert ein geführtes Training. Die Zeit wird über die Systemuhr gemessen, damit nichts driftet. */
class PlayerViewModel(
    private val container: AppContainer,
    application: Application,
    private val planId: String,
) : ViewModel() {

    private val repository = container.repository
    private val feedback = WorkoutFeedback(application)
    private val _ui = MutableStateFlow(PlayerUi())
    val ui: StateFlow<PlayerUi> = _ui.asStateFlow()
    private var lastTick = SystemClock.elapsedRealtime()
    private var saved = false

    init {
        viewModelScope.launch {
            val data = repository.current()
            val plan = data.plan(planId)
            if (plan == null) {
                _ui.value = PlayerUi(loading = false, missing = true)
                return@launch
            }
            feedback.configure(data.settings)
            val phases = buildPhases(plan.steps, data::findExercise, data.settings.getReadySeconds)
            _ui.value = PlayerUi(
                loading = false,
                planName = plan.name,
                planEmoji = plan.emoji,
                phases = phases,
                state = RunnerState.start(phases),
                totalExercises = phases.map { it.stepIndex }.distinct().size,
            )
            phases.firstOrNull()?.let { feedback.announce(it, phases.getOrNull(1)) }
            container.notifications.cancelWorkout(planId)
            runTicker()
        }
    }

    private suspend fun runTicker() {
        lastTick = SystemClock.elapsedRealtime()
        while (viewModelScope.isActive && !_ui.value.state.finished) {
            delay(TICK_MS)
            val now = SystemClock.elapsedRealtime()
            val delta = now - lastTick
            lastTick = now
            val ui = _ui.value
            apply(ui.state.advance(ui.phases, delta))
        }
    }

    private fun apply(result: RunnerResult) {
        val phases = _ui.value.phases
        _ui.update { it.copy(state = result.state) }
        result.events.forEach { event ->
            when (event) {
                is RunnerEvent.PhaseStarted -> feedback.phaseStarted(phases, event.index)
                is RunnerEvent.Countdown -> feedback.countdown(event.secondsLeft)
                RunnerEvent.Finished -> onFinished()
            }
        }
    }

    fun togglePause() {
        lastTick = SystemClock.elapsedRealtime()
        _ui.update { it.copy(state = it.state.togglePause()) }
    }

    fun skip() = apply(_ui.value.state.skip(_ui.value.phases))

    fun previous() = apply(_ui.value.state.previous(_ui.value.phases))

    fun completeReps() = apply(_ui.value.state.completeReps(_ui.value.phases))

    fun extend(seconds: Int) = _ui.update { it.copy(state = it.state.extend(it.phases, seconds)) }

    private fun onFinished() {
        feedback.finished()
        // Wer alles übersprungen hat, bekommt keinen Eintrag.
        if (_ui.value.completedExercises > 0) save()
    }

    /** Vorzeitig beenden; mit [keep] wird der bisherige Fortschritt gespeichert. */
    fun stop(keep: Boolean) {
        if (keep && _ui.value.completedExercises > 0) save()
    }

    private fun save() {
        if (saved) return
        saved = true
        val ui = _ui.value
        // Im App-Scope speichern, damit das Schließen des Bildschirms den Eintrag nicht verhindert.
        container.appScope.launch {
            val session = repository.logSession(
                planId = planId,
                durationSeconds = ui.durationSeconds,
                completedExercises = ui.completedExercises,
                totalExercises = ui.totalExercises,
            )
            val streak = repository.current().currentStreak(repository.today())
            _ui.update { it.copy(sessionId = session.id, streak = streak) }
            _ui.value.feeling?.let { repository.setFeeling(session.id, it) }
        }
    }

    fun setFeeling(feeling: Int) {
        _ui.update { it.copy(feeling = feeling) }
        val sessionId = _ui.value.sessionId ?: return
        container.appScope.launch { repository.setFeeling(sessionId, feeling) }
    }

    override fun onCleared() {
        feedback.release()
    }

    private companion object {
        const val TICK_MS = 100L
    }
}
