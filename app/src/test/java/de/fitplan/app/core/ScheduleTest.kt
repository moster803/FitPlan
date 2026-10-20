package de.fitplan.app.core

import de.fitplan.app.core.logic.AlarmKind
import de.fitplan.app.core.logic.nextFollowUpReminder
import de.fitplan.app.core.logic.nextSupplementReminder
import de.fitplan.app.core.logic.nextWorkoutReminder
import de.fitplan.app.core.logic.planAlarms
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Intake
import de.fitplan.app.core.model.Reminder
import de.fitplan.app.core.model.Settings
import de.fitplan.app.core.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ScheduleTest {

    @Test
    fun `workout reminder fires today when still ahead`() {
        val data = AppData(plans = listOf(plan()))
        val now = MONDAY.atTime(6, 0)
        assertEquals(MONDAY.atTime(8, 0), nextWorkoutReminder(data, data.plans[0], now))
    }

    @Test
    fun `workout reminder moves to tomorrow after time passed`() {
        val data = AppData(plans = listOf(plan()))
        val now = MONDAY.atTime(9, 0)
        assertEquals(MONDAY.plusDays(1).atTime(8, 0), nextWorkoutReminder(data, data.plans[0], now))
    }

    @Test
    fun `workout reminder skips today when already done`() {
        val data = AppData(plans = listOf(plan()), sessions = listOf(session(MONDAY)))
        val now = MONDAY.atTime(6, 0)
        assertEquals(MONDAY.plusDays(1).atTime(8, 0), nextWorkoutReminder(data, data.plans[0], now))
    }

    @Test
    fun `workout reminder respects weekdays`() {
        val data = AppData(plans = listOf(plan(days = setOf(3)))) // nur Mittwoch
        val now = MONDAY.atTime(9, 0)
        assertEquals(MONDAY.plusDays(2).atTime(8, 0), nextWorkoutReminder(data, data.plans[0], now))
    }

    @Test
    fun `disabled reminder schedules nothing`() {
        val data = AppData(plans = listOf(plan(reminder = Reminder(enabled = false))))
        assertNull(nextWorkoutReminder(data, data.plans[0], MONDAY.atTime(6, 0)))
        assertNull(nextFollowUpReminder(data, data.plans[0], MONDAY.atTime(6, 0)))
    }

    @Test
    fun `follow up only when workout open`() {
        val open = AppData(plans = listOf(plan()))
        assertEquals(MONDAY.atTime(19, 0), nextFollowUpReminder(open, open.plans[0], MONDAY.atTime(12, 0)))

        val done = open.copy(sessions = listOf(session(MONDAY)))
        assertEquals(MONDAY.plusDays(1).atTime(19, 0), nextFollowUpReminder(done, done.plans[0], MONDAY.atTime(12, 0)))
    }

    @Test
    fun `supplement reminder skips slot when everything taken`() {
        val data = AppData(
            supplements = listOf(supplement(slots = setOf(TimeSlot.EVENING))),
            intakes = listOf(Intake("s1", MONDAY.toEpochDay(), TimeSlot.EVENING, 0)),
        )
        val now = MONDAY.atTime(10, 0)
        val evening = LocalTime.of(18, 30)
        assertEquals(MONDAY.plusDays(1).atTime(evening), nextSupplementReminder(data, TimeSlot.EVENING, now))
        assertNull(nextSupplementReminder(data, TimeSlot.MORNING, now))
    }

    @Test
    fun `supplement reminders can be turned off`() {
        val data = AppData(
            settings = Settings(supplementReminders = false),
            supplements = listOf(supplement()),
        )
        assertNull(nextSupplementReminder(data, TimeSlot.EVENING, MONDAY.atTime(10, 0)))
    }

    @Test
    fun `ended course is no longer due`() {
        val data = AppData(supplements = listOf(supplement().copy(endEpochDay = MONDAY.toEpochDay())))
        assertEquals(MONDAY.atTime(18, 30), nextSupplementReminder(data, TimeSlot.EVENING, MONDAY.atTime(10, 0)))
        assertNull(nextSupplementReminder(data, TimeSlot.EVENING, MONDAY.atTime(20, 0)))
    }

    @Test
    fun `plan alarms contain all kinds`() {
        val data = AppData(plans = listOf(plan()), supplements = listOf(supplement()))
        val alarms = planAlarms(data, MONDAY.atTime(6, 0))
        assertEquals(setOf(AlarmKind.WORKOUT, AlarmKind.WORKOUT_FOLLOW_UP, AlarmKind.SUPPLEMENT), alarms.map { it.kind }.toSet())
        assertTrue(alarms.all { it.at.isAfter(MONDAY.atTime(6, 0)) })
    }
}
