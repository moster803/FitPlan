package de.fitplan.app.core.model

import kotlinx.serialization.Serializable

/** ISO-Wochentage: 1 = Montag … 7 = Sonntag. */
val ALL_DAYS: Set<Int> = (1..7).toSet()
val WEEKDAYS: Set<Int> = (1..5).toSet()

@Serializable
enum class ExerciseCategory { MOBILITY, STRENGTH, STRETCH, RELAX, ENDURANCE }

/** Übungsform, deren Wirksamkeit in Studien untersucht wurde – Grundlage für die Evidenz-Angaben. */
@Serializable
enum class Approach { PILATES, MOTOR_CONTROL, STRENGTH, YOGA, STRETCHING, MCKENZIE, WALKING, NECK }

@Serializable
enum class ExerciseMode { TIMED, REPS }

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val description: String = "",
    val steps: List<String> = emptyList(),
    val tip: String = "",
    val category: ExerciseCategory = ExerciseCategory.MOBILITY,
    val mode: ExerciseMode = ExerciseMode.TIMED,
    val seconds: Int = 30,
    val reps: Int = 10,
    /** Übung wird für linke und rechte Seite getrennt ausgeführt. */
    val perSide: Boolean = false,
    val builtIn: Boolean = false,
    /** Studienbelegte Übungsform; eigene Übungen haben keine. */
    val approach: Approach? = null,
    /** Benötigte Hilfsmittel, leer wenn keine. */
    val equipment: String = "",
)

@Serializable
data class PlanStep(
    val id: String,
    val exerciseId: String,
    val mode: ExerciseMode,
    val seconds: Int = 30,
    val reps: Int = 10,
    val sets: Int = 1,
    val restSeconds: Int = 15,
)

@Serializable
data class Reminder(
    val enabled: Boolean = true,
    val minuteOfDay: Int = 8 * 60,
    /** Zweite, sanfte Erinnerung, falls das Training bis dahin noch nicht erledigt ist. */
    val followUp: Boolean = true,
    val followUpMinuteOfDay: Int = 19 * 60,
)

@Serializable
data class WorkoutPlan(
    val id: String,
    val name: String,
    val emoji: String = "🧘",
    val color: Int = 0,
    val days: Set<Int> = ALL_DAYS,
    val steps: List<PlanStep> = emptyList(),
    val reminder: Reminder = Reminder(),
    val createdEpochDay: Long,
    val active: Boolean = true,
)

@Serializable
enum class TimeSlot { MORNING, NOON, EVENING, NIGHT }

@Serializable
enum class IntakeHint { ANY, WITH_MEAL, EMPTY_STOMACH, BEFORE_BED }

@Serializable
enum class SupplementForm { CAPSULE, TABLET, POWDER, DROPS, LIQUID, GUMMY }

@Serializable
data class Supplement(
    val id: String,
    val name: String,
    val emoji: String = "💊",
    val dose: String = "",
    val form: SupplementForm = SupplementForm.CAPSULE,
    val slots: Set<TimeSlot> = setOf(TimeSlot.MORNING),
    val days: Set<Int> = ALL_DAYS,
    val hint: IntakeHint = IntakeHint.ANY,
    /** Einheiten pro Einnahme (Kapseln, Tropfen, Gramm …), Basis für die Vorratsrechnung. */
    val unitsPerIntake: Int = 1,
    val trackStock: Boolean = false,
    val stock: Int = 0,
    val packSize: Int = 60,
    val refillWarningDays: Int = 7,
    /** Letzter Einnahmetag einer Kur, danach wird das Supplement nicht mehr fällig. */
    val endEpochDay: Long? = null,
    val notes: String = "",
    val active: Boolean = true,
    val templateKey: String? = null,
    val color: Int = 0,
    val createdEpochDay: Long,
)

@Serializable
data class WorkoutSession(
    val id: String,
    val planId: String,
    val epochDay: Long,
    val finishedAt: Long,
    val durationSeconds: Int,
    val completedExercises: Int,
    val totalExercises: Int,
    /** Rückengefühl nach dem Training, 1 (schlecht) bis 5 (super). */
    val feeling: Int? = null,
    /** Ohne geführtes Training abgehakt, z. B. nach der Physiotherapie. */
    val quickLog: Boolean = false,
)

@Serializable
data class Intake(
    val supplementId: String,
    val epochDay: Long,
    val slot: TimeSlot,
    val takenAt: Long,
)

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

val DEFAULT_SLOT_TIMES: Map<TimeSlot, Int> = mapOf(
    TimeSlot.MORNING to 7 * 60 + 30,
    TimeSlot.NOON to 12 * 60 + 30,
    TimeSlot.EVENING to 18 * 60 + 30,
    TimeSlot.NIGHT to 22 * 60,
)

@Serializable
data class Settings(
    val onboardingDone: Boolean = false,
    val name: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val slotTimes: Map<TimeSlot, Int> = DEFAULT_SLOT_TIMES,
    val supplementReminders: Boolean = true,
    val lowStockAlerts: Boolean = true,
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val voice: Boolean = true,
    val getReadySeconds: Int = 5,
) {
    fun slotMinute(slot: TimeSlot): Int = slotTimes[slot] ?: DEFAULT_SLOT_TIMES.getValue(slot)
}

@Serializable
data class AppData(
    val schemaVersion: Int = CURRENT_SCHEMA,
    val settings: Settings = Settings(),
    val customExercises: List<Exercise> = emptyList(),
    val plans: List<WorkoutPlan> = emptyList(),
    val supplements: List<Supplement> = emptyList(),
    val sessions: List<WorkoutSession> = emptyList(),
    val intakes: List<Intake> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA = 1
    }
}
