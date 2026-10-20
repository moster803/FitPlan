package de.fitplan.app.core

import de.fitplan.app.core.model.ALL_DAYS
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.model.Reminder
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.model.WorkoutSession
import java.time.LocalDate

/** Montag, 19. Oktober 2026. */
val MONDAY: LocalDate = LocalDate.of(2026, 10, 19)

fun plan(
    id: String = "p1",
    days: Set<Int> = ALL_DAYS,
    created: LocalDate = MONDAY.minusDays(30),
    reminder: Reminder = Reminder(minuteOfDay = 8 * 60, followUp = true, followUpMinuteOfDay = 19 * 60),
) = WorkoutPlan(
    id = id,
    name = "Rücken",
    days = days,
    steps = listOf(PlanStep("s1", "cat_cow", ExerciseMode.REPS, reps = 10)),
    reminder = reminder,
    createdEpochDay = created.toEpochDay(),
)

fun session(date: LocalDate, planId: String = "p1", feeling: Int? = null) = WorkoutSession(
    id = "session-${date.toEpochDay()}-$planId",
    planId = planId,
    epochDay = date.toEpochDay(),
    finishedAt = 0,
    durationSeconds = 600,
    completedExercises = 1,
    totalExercises = 1,
    feeling = feeling,
)

fun supplement(
    id: String = "s1",
    name: String = "Magnesium",
    slots: Set<TimeSlot> = setOf(TimeSlot.EVENING),
    trackStock: Boolean = false,
    stock: Int = 0,
    units: Int = 1,
    days: Set<Int> = ALL_DAYS,
) = Supplement(
    id = id,
    name = name,
    slots = slots,
    trackStock = trackStock,
    stock = stock,
    unitsPerIntake = units,
    days = days,
    createdEpochDay = MONDAY.minusDays(30).toEpochDay(),
)
