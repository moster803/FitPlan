package de.fitplan.app.core.data

import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.logic.MINUTES_PER_DAY
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.WorkoutPlan

/**
 * Bringt gelesene oder importierte Daten in einen gültigen Zustand, damit ein bearbeitetes oder
 * fremdes Backup die Oberfläche nicht zum Absturz bringt: Uhrzeiten, Wochentage, Bewertungen,
 * Mengen und doppelte IDs.
 */
fun AppData.sanitized(): AppData {
    val builtInIds = ExerciseLibrary.all.mapTo(HashSet()) { it.id }
    return copy(
        settings = settings.copy(
            slotTimes = settings.slotTimes.mapValues { (_, minute) -> minuteOfDay(minute) },
            getReadySeconds = settings.getReadySeconds.coerceIn(0, 60),
        ),
        customExercises = customExercises.filter { it.id !in builtInIds }.distinctBy { it.id }.map { it.sanitized() },
        plans = plans.distinctBy { it.id }.map { it.sanitized() },
        supplements = supplements.distinctBy { it.id }.map { it.sanitized() },
        sessions = sessions.distinctBy { it.id }.map { session -> session.copy(feeling = session.feeling?.takeIf { it in 1..5 }) },
    )
}

private fun minuteOfDay(minute: Int) = minute.coerceIn(0, MINUTES_PER_DAY - 1)

private fun weekdays(days: Set<Int>): Set<Int> = days.filterTo(LinkedHashSet()) { it in 1..7 }

private fun Exercise.sanitized() = copy(seconds = seconds.coerceAtLeast(1), reps = reps.coerceAtLeast(1))

private fun WorkoutPlan.sanitized() = copy(
    days = weekdays(days),
    steps = steps.distinctBy { it.id }.map { step ->
        step.copy(
            seconds = step.seconds.coerceAtLeast(1),
            reps = step.reps.coerceAtLeast(1),
            sets = step.sets.coerceAtLeast(1),
            restSeconds = step.restSeconds.coerceAtLeast(0),
        )
    },
    reminder = reminder.copy(
        minuteOfDay = minuteOfDay(reminder.minuteOfDay),
        followUpMinuteOfDay = minuteOfDay(reminder.followUpMinuteOfDay),
    ),
    scheduleHistory = scheduleHistory.map { it.copy(days = weekdays(it.days)) },
)

private fun Supplement.sanitized() = copy(
    days = weekdays(days),
    unitsPerIntake = unitsPerIntake.coerceAtLeast(1),
    stock = stock.coerceAtLeast(0),
    packSize = packSize.coerceAtLeast(1),
    refillWarningDays = refillWarningDays.coerceAtLeast(0),
)
