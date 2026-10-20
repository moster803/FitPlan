package de.fitplan.app.core

import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.library.PlanTemplates.toPlan
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.workout.PhaseType
import de.fitplan.app.core.workout.RunnerEvent
import de.fitplan.app.core.workout.RunnerState
import de.fitplan.app.core.workout.Side
import de.fitplan.app.core.workout.advance
import de.fitplan.app.core.workout.buildPhases
import de.fitplan.app.core.workout.completeReps
import de.fitplan.app.core.workout.estimatedSeconds
import de.fitplan.app.core.workout.previous
import de.fitplan.app.core.workout.skip
import de.fitplan.app.core.workout.togglePause
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTest {

    private val steps = listOf(
        PlanStep("a", "plank", ExerciseMode.TIMED, seconds = 10, sets = 2, restSeconds = 5),
        PlanStep("b", "glute_bridge", ExerciseMode.REPS, reps = 12, sets = 1, restSeconds = 5),
        PlanStep("c", "side_plank", ExerciseMode.TIMED, seconds = 20, sets = 1, restSeconds = 0),
    )
    private val phases = buildPhases(steps, ExerciseLibrary::find, getReadySeconds = 3)

    @Test
    fun `phases follow sets rests and sides`() {
        val types = phases.map { it.type }
        assertEquals(
            listOf(
                PhaseType.GET_READY,
                PhaseType.WORK, PhaseType.REST, PhaseType.WORK, PhaseType.REST, // Plank 2 Sätze
                PhaseType.WORK, PhaseType.REST, // Beckenheben
                PhaseType.WORK, PhaseType.SWITCH_SIDE, PhaseType.WORK, // Seitstütz je Seite, keine Pause am Ende
            ),
            types,
        )
        assertEquals(Side.LEFT, phases[7].side)
        assertEquals(Side.RIGHT, phases[9].side)
        // Pause zwischen Übungen zeigt die nächste Übung.
        assertEquals("glute_bridge", phases[4].exercise.id)
    }

    @Test
    fun `unknown exercises are skipped`() {
        val result = buildPhases(listOf(PlanStep("x", "gibt_es_nicht", ExerciseMode.TIMED)), ExerciseLibrary::find, 5)
        assertTrue(result.isEmpty())
        assertTrue(RunnerState.start(result).finished)
    }

    @Test
    fun `timed phases advance automatically with countdown`() {
        var state = RunnerState.start(phases)
        val result = state.advance(phases, 3_000)
        state = result.state
        assertEquals(1, state.index)
        assertTrue(result.events.contains(RunnerEvent.Countdown(1)))
        assertTrue(result.events.contains(RunnerEvent.PhaseStarted(1)))
        assertEquals(10_000, state.remainingMs)
    }

    @Test
    fun `large time delta crosses several phases`() {
        val state = RunnerState.start(phases).advance(phases, 3_000 + 10_000 + 5_000 + 10_000 + 5_000).state
        assertEquals(5, state.index) // Beckenheben (Wiederholungen) wartet
        assertEquals(setOf(1, 3), state.completedWork)
        assertEquals(1, state.completedExercises(phases))
    }

    @Test
    fun `reps phase waits for user`() {
        var state = RunnerState.start(phases).advance(phases, 33_000).state
        assertEquals(5, state.index)
        state = state.advance(phases, 60_000).state
        assertEquals(5, state.index)
        state = state.completeReps(phases).state
        assertEquals(6, state.index)
        assertEquals(2, state.completedExercises(phases))
    }

    @Test
    fun `pause stops the clock`() {
        val paused = RunnerState.start(phases).togglePause()
        assertEquals(paused, paused.advance(phases, 10_000).state)
    }

    @Test
    fun `skipping does not count as completed and finishes at end`() {
        var state = RunnerState.start(phases)
        repeat(phases.size) { state = state.skip(phases).state }
        assertTrue(state.finished)
        assertEquals(0, state.completedExercises(phases))
    }

    @Test
    fun `previous restarts current timed phase or goes back`() {
        var state = RunnerState.start(phases).advance(phases, 3_000 + 5_000).state // mitten im Plank
        state = state.previous(phases).state
        assertEquals(1, state.index)
        assertEquals(10_000, state.remainingMs)
        state = state.previous(phases).state // direkt nach Neustart: zur vorherigen Übung (keine vorhanden)
        assertEquals(0, state.index)
        assertFalse(state.finished)
    }

    @Test
    fun `templates resolve all exercises`() {
        PlanTemplates.all.forEach { template ->
            val plan = template.toPlan("id", 0) { "step" }
            assertEquals(template.key, template.steps.size, plan.steps.size)
            assertTrue(estimatedSeconds(plan.steps, ExerciseLibrary::find) > 60)
        }
    }
}
