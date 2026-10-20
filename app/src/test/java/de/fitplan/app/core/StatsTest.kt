package de.fitplan.app.core

import de.fitplan.app.core.logic.DayState
import de.fitplan.app.core.logic.currentPlanStreak
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.dayState
import de.fitplan.app.core.logic.longestStreak
import de.fitplan.app.core.logic.supplementAdherence
import de.fitplan.app.core.logic.week
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Intake
import de.fitplan.app.core.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsTest {

    @Test
    fun `streak counts consecutive days and ignores open today`() {
        val data = AppData(
            plans = listOf(plan()),
            sessions = (1L..5L).map { session(MONDAY.minusDays(it)) },
        )
        assertEquals(5, data.currentStreak(MONDAY))
    }

    @Test
    fun `streak includes today when done`() {
        val data = AppData(
            plans = listOf(plan()),
            sessions = (0L..2L).map { session(MONDAY.minusDays(it)) },
        )
        assertEquals(3, data.currentStreak(MONDAY))
    }

    @Test
    fun `missed scheduled day breaks streak`() {
        val data = AppData(
            plans = listOf(plan()),
            sessions = listOf(session(MONDAY.minusDays(1)), session(MONDAY.minusDays(3))),
        )
        assertEquals(1, data.currentStreak(MONDAY))
    }

    @Test
    fun `rest days do not break streak`() {
        // Plan nur Montag, Mittwoch, Freitag.
        val data = AppData(
            plans = listOf(plan(days = setOf(1, 3, 5))),
            sessions = listOf(
                session(MONDAY.minusDays(3)), // Freitag
                session(MONDAY.minusDays(5)), // Mittwoch
                session(MONDAY.minusDays(7)), // Montag
            ),
        )
        assertEquals(3, data.currentStreak(MONDAY))
    }

    @Test
    fun `days before plan creation are not missed`() {
        val data = AppData(
            plans = listOf(plan(created = MONDAY.minusDays(2))),
            sessions = listOf(session(MONDAY.minusDays(1)), session(MONDAY.minusDays(2))),
        )
        assertEquals(2, data.currentStreak(MONDAY))
        assertEquals(DayState.REST, data.dayState(MONDAY.minusDays(5), MONDAY))
    }

    @Test
    fun `longest streak finds best run`() {
        val data = AppData(
            plans = listOf(plan(created = MONDAY.minusDays(20))),
            sessions = (10L..15L).map { session(MONDAY.minusDays(it)) } + listOf(session(MONDAY.minusDays(1))),
        )
        assertEquals(6, data.longestStreak(MONDAY))
        assertEquals(1, data.currentStreak(MONDAY))
    }

    @Test
    fun `plan streak only counts its own sessions`() {
        val back = plan("back")
        val office = plan("office", days = setOf(1, 2, 3, 4, 5))
        val data = AppData(
            plans = listOf(back, office),
            sessions = listOf(
                session(MONDAY.minusDays(1), planId = "back"),
                session(MONDAY.minusDays(2), planId = "back"),
                session(MONDAY.minusDays(3), planId = "office"), // Freitag
            ),
        )
        assertEquals(2, data.currentPlanStreak(back, MONDAY))
        // Wochenende zählt für den Büro-Plan nicht, Freitag war erledigt.
        assertEquals(1, data.currentPlanStreak(office, MONDAY))
    }

    @Test
    fun `week shows states from monday to sunday`() {
        val today = MONDAY.plusDays(2) // Mittwoch
        val data = AppData(plans = listOf(plan()), sessions = listOf(session(MONDAY)))
        val states = data.week(today).map { it.second }
        assertEquals(
            listOf(DayState.DONE, DayState.MISSED, DayState.OPEN, DayState.OPEN, DayState.OPEN, DayState.OPEN, DayState.OPEN),
            states,
        )
    }

    @Test
    fun `adherence counts taken versus due`() {
        val data = AppData(
            supplements = listOf(supplement(slots = setOf(TimeSlot.MORNING, TimeSlot.EVENING))),
            intakes = listOf(
                Intake("s1", MONDAY.toEpochDay(), TimeSlot.MORNING, 0),
                Intake("s1", MONDAY.toEpochDay(), TimeSlot.EVENING, 0),
                Intake("s1", MONDAY.plusDays(1).toEpochDay(), TimeSlot.MORNING, 0),
            ),
        )
        assertEquals(0.75f, data.supplementAdherence(MONDAY, MONDAY.plusDays(1))!!, 0.001f)
        assertNull(AppData().supplementAdherence(MONDAY, MONDAY))
    }

    @Test
    fun `supplements are not due before they were added`() {
        val added = supplement().copy(createdEpochDay = MONDAY.toEpochDay())
        val data = AppData(
            supplements = listOf(added),
            intakes = listOf(Intake("s1", MONDAY.toEpochDay(), TimeSlot.EVENING, 0)),
        )
        // Sonntag zählt nicht mit – sonst läge die Quote bei 50 %.
        assertEquals(1f, data.supplementAdherence(MONDAY.minusDays(1), MONDAY)!!, 0.001f)
    }
}
