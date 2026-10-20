package de.fitplan.app.core.logic

import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.model.WorkoutSession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun WorkoutPlan.isScheduledOn(date: LocalDate): Boolean =
    active && steps.isNotEmpty() && date.dayOfWeek.value in days

fun Supplement.isDueOn(date: LocalDate): Boolean =
    active && date.dayOfWeek.value in days && (endEpochDay == null || date.toEpochDay() <= endEpochDay)

fun Supplement.isDueOn(date: LocalDate, slot: TimeSlot): Boolean = slot in slots && isDueOn(date)

fun AppData.plan(id: String): WorkoutPlan? = plans.firstOrNull { it.id == id }

fun AppData.supplement(id: String): Supplement? = supplements.firstOrNull { it.id == id }

fun AppData.sessionsOn(epochDay: Long): List<WorkoutSession> = sessions.filter { it.epochDay == epochDay }

fun AppData.sessionFor(planId: String, epochDay: Long): WorkoutSession? =
    sessions.lastOrNull { it.planId == planId && it.epochDay == epochDay }

fun AppData.isPlanDone(planId: String, epochDay: Long): Boolean = sessionFor(planId, epochDay) != null

fun AppData.plansScheduledOn(date: LocalDate): List<WorkoutPlan> = plans.filter { it.isScheduledOn(date) }

fun AppData.isTaken(supplementId: String, epochDay: Long, slot: TimeSlot): Boolean =
    intakes.any { it.supplementId == supplementId && it.epochDay == epochDay && it.slot == slot }

fun AppData.dueSupplements(date: LocalDate, slot: TimeSlot): List<Supplement> =
    supplements.filter { it.isDueOn(date, slot) }

fun AppData.pendingSupplements(date: LocalDate, slot: TimeSlot): List<Supplement> {
    val day = date.toEpochDay()
    return dueSupplements(date, slot).filterNot { isTaken(it.id, day, slot) }
}

fun minuteToTime(minuteOfDay: Int): LocalTime = LocalTime.of((minuteOfDay / 60) % 24, minuteOfDay % 60)

enum class AlarmKind { WORKOUT, WORKOUT_FOLLOW_UP, SUPPLEMENT }

/** Ein zu planender Wecker. [key] ist stabil und dient als Identität beim Ersetzen/Löschen. */
data class PlannedAlarm(val kind: AlarmKind, val refId: String, val at: LocalDateTime) {
    val key: String get() = "${kind.name}:$refId"
}

private const val LOOKAHEAD_DAYS = 8L

/** Nächste Haupt-Erinnerung eines Plans. Ist das Training heute schon erledigt, geht es morgen weiter. */
fun nextWorkoutReminder(data: AppData, plan: WorkoutPlan, now: LocalDateTime): LocalDateTime? {
    if (!plan.reminder.enabled) return null
    val time = minuteToTime(plan.reminder.minuteOfDay)
    for (offset in 0..LOOKAHEAD_DAYS) {
        val date = now.toLocalDate().plusDays(offset)
        if (!plan.isScheduledOn(date)) continue
        if (data.isPlanDone(plan.id, date.toEpochDay())) continue
        val at = date.atTime(time)
        if (at.isAfter(now)) return at
    }
    return null
}

/** Nachhak-Erinnerung: nur, wenn das Training am jeweiligen Tag noch offen ist. */
fun nextFollowUpReminder(data: AppData, plan: WorkoutPlan, now: LocalDateTime): LocalDateTime? {
    val reminder = plan.reminder
    if (!reminder.enabled || !reminder.followUp) return null
    if (reminder.followUpMinuteOfDay <= reminder.minuteOfDay) return null
    val time = minuteToTime(reminder.followUpMinuteOfDay)
    for (offset in 0..LOOKAHEAD_DAYS) {
        val date = now.toLocalDate().plusDays(offset)
        if (!plan.isScheduledOn(date)) continue
        if (data.isPlanDone(plan.id, date.toEpochDay())) continue
        val at = date.atTime(time)
        if (at.isAfter(now)) return at
    }
    return null
}

/** Nächste Supplement-Erinnerung für eine Tageszeit – übersprungen, wenn schon alles genommen wurde. */
fun nextSupplementReminder(data: AppData, slot: TimeSlot, now: LocalDateTime): LocalDateTime? {
    if (!data.settings.supplementReminders) return null
    val time = minuteToTime(data.settings.slotMinute(slot))
    for (offset in 0..LOOKAHEAD_DAYS) {
        val date = now.toLocalDate().plusDays(offset)
        if (data.pendingSupplements(date, slot).isEmpty()) continue
        val at = date.atTime(time)
        if (at.isAfter(now)) return at
    }
    return null
}

fun planAlarms(data: AppData, now: LocalDateTime): List<PlannedAlarm> = buildList {
    data.plans.forEach { plan ->
        nextWorkoutReminder(data, plan, now)?.let { add(PlannedAlarm(AlarmKind.WORKOUT, plan.id, it)) }
        nextFollowUpReminder(data, plan, now)?.let { add(PlannedAlarm(AlarmKind.WORKOUT_FOLLOW_UP, plan.id, it)) }
    }
    TimeSlot.entries.forEach { slot ->
        nextSupplementReminder(data, slot, now)?.let { add(PlannedAlarm(AlarmKind.SUPPLEMENT, slot.name, it)) }
    }
}
