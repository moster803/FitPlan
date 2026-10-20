package de.fitplan.app.core.logic

import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class DayState {
    /** Mindestens ein Training erledigt. */
    DONE,

    /** Training war geplant, wurde aber nicht gemacht. */
    MISSED,

    /** Training ist geplant, der Tag ist aber noch nicht vorbei. */
    OPEN,

    /** Kein Training geplant. */
    REST,
}

private const val MAX_STREAK_LOOKBACK = 3650

/** Erster Tag, ab dem Trainingstage gezählt werden. */
private fun AppData.firstTrackedDay(): Long? {
    val firstPlan = plans.minOfOrNull { it.createdEpochDay }
    val firstSession = sessions.minOfOrNull { it.epochDay }
    return listOfNotNull(firstPlan, firstSession).minOrNull()
}

fun AppData.dayState(date: LocalDate, today: LocalDate): DayState {
    val day = date.toEpochDay()
    if (sessions.any { it.epochDay == day }) return DayState.DONE
    val scheduled = plans.any { it.wasScheduledOn(date) && it.createdEpochDay <= day }
    return when {
        !scheduled -> DayState.REST
        date.isBefore(today) -> DayState.MISSED
        else -> DayState.OPEN
    }
}

/**
 * Aktuelle Serie: aufeinanderfolgende Trainingstage bis heute. Tage ohne geplantes Training
 * unterbrechen die Serie nicht, und ein heute noch offenes Training auch nicht.
 */
fun AppData.currentStreak(today: LocalDate): Int {
    val first = firstTrackedDay() ?: return 0
    var streak = 0
    var date = today
    repeat(MAX_STREAK_LOOKBACK) {
        if (date.toEpochDay() < first) return streak
        when (dayState(date, today)) {
            DayState.DONE -> streak++
            DayState.MISSED -> return streak
            DayState.OPEN, DayState.REST -> Unit
        }
        date = date.minusDays(1)
    }
    return streak
}

/** Serie eines einzelnen Plans: nur dessen Einheiten und Trainingstage zählen. */
fun AppData.currentPlanStreak(plan: WorkoutPlan, today: LocalDate): Int {
    val planSessions = sessions.filter { it.planId == plan.id }
    val first = minOf(plan.createdEpochDay, planSessions.minOfOrNull { it.epochDay } ?: Long.MAX_VALUE)
    var streak = 0
    var date = today
    repeat(MAX_STREAK_LOOKBACK) {
        val day = date.toEpochDay()
        if (day < first) return streak
        when {
            planSessions.any { it.epochDay == day } -> streak++
            plan.wasScheduledOn(date) && date.isBefore(today) -> return streak
        }
        date = date.minusDays(1)
    }
    return streak
}

fun AppData.longestStreak(today: LocalDate): Int {
    val first = firstTrackedDay() ?: return 0
    var best = 0
    var running = 0
    var date = LocalDate.ofEpochDay(maxOf(first, today.toEpochDay() - MAX_STREAK_LOOKBACK))
    while (!date.isAfter(today)) {
        when (dayState(date, today)) {
            DayState.DONE -> {
                running++
                best = maxOf(best, running)
            }
            DayState.MISSED -> running = 0
            DayState.OPEN, DayState.REST -> Unit
        }
        date = date.plusDays(1)
    }
    return best
}

fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun AppData.week(today: LocalDate): List<Pair<LocalDate, DayState>> {
    val start = weekStart(today)
    return (0L..6L).map { offset ->
        val date = start.plusDays(offset)
        date to dayState(date, today)
    }
}

data class MonthSummary(val sessions: Int, val minutes: Int, val activeDays: Int)

fun AppData.monthSummary(anyDayInMonth: LocalDate): MonthSummary {
    val first = anyDayInMonth.withDayOfMonth(1).toEpochDay()
    val last = anyDayInMonth.withDayOfMonth(anyDayInMonth.lengthOfMonth()).toEpochDay()
    val inMonth = sessions.filter { it.epochDay in first..last }
    return MonthSummary(
        sessions = inMonth.size,
        minutes = inMonth.sumOf { it.durationSeconds } / 60,
        activeDays = inMonth.map { it.epochDay }.distinct().size,
    )
}

fun AppData.totalMinutes(): Int = sessions.sumOf { it.durationSeconds } / 60

/** Anteil der genommenen an den fälligen Einnahmen im Zeitraum (inklusive), oder null ohne fällige Einnahmen. */
fun AppData.supplementAdherence(from: LocalDate, to: LocalDate): Float? {
    var due = 0
    var taken = 0
    var date = from
    while (!date.isAfter(to)) {
        val day = date.toEpochDay()
        TimeSlot.entries.forEach { slot ->
            dueSupplements(date, slot).forEach { supplement ->
                due++
                if (isTaken(supplement.id, day, slot)) taken++
            }
        }
        date = date.plusDays(1)
    }
    return if (due == 0) null else taken.toFloat() / due
}

data class SupplementDay(val date: LocalDate, val due: Int, val taken: Int)

fun AppData.supplementDays(today: LocalDate, count: Int): List<SupplementDay> =
    (count - 1 downTo 0).map { back ->
        val date = today.minusDays(back.toLong())
        val day = date.toEpochDay()
        var due = 0
        var taken = 0
        TimeSlot.entries.forEach { slot ->
            dueSupplements(date, slot).forEach {
                due++
                if (isTaken(it.id, day, slot)) taken++
            }
        }
        SupplementDay(date, due, taken)
    }

data class FeelingPoint(val epochDay: Long, val feeling: Int)

/** Rückengefühl je Tag (Durchschnitt, falls mehrere Einträge) für die letzten [days] Tage. */
fun AppData.feelingHistory(today: LocalDate, days: Int): List<FeelingPoint> {
    val from = today.toEpochDay() - days + 1
    return sessions
        .filter { it.feeling != null && it.epochDay in from..today.toEpochDay() }
        .groupBy { it.epochDay }
        .map { (day, list) -> FeelingPoint(day, Math.round(list.mapNotNull { it.feeling }.average()).toInt()) }
        .sortedBy { it.epochDay }
}
