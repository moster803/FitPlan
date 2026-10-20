package de.fitplan.app.core.library

import de.fitplan.app.core.model.ALL_DAYS
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.model.Reminder
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.SupplementForm
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WEEKDAYS
import de.fitplan.app.core.model.WorkoutPlan

data class PlanTemplate(
    val key: String,
    val name: String,
    val emoji: String,
    val color: Int,
    val description: String,
    val days: Set<Int>,
    val reminder: Reminder,
    val steps: List<StepSpec>,
)

data class StepSpec(
    val exerciseId: String,
    val sets: Int = 1,
    val seconds: Int? = null,
    val reps: Int? = null,
    val rest: Int = 15,
)

object PlanTemplates {

    val dailyBack = PlanTemplate(
        key = "daily_back",
        name = "Rücken täglich",
        emoji = "🧘",
        color = 0,
        description = "Ausgewogene 12-Minuten-Routine aus Mobilisation, Kräftigung und Dehnung.",
        days = ALL_DAYS,
        reminder = Reminder(minuteOfDay = 8 * 60, followUpMinuteOfDay = 19 * 60),
        steps = listOf(
            StepSpec("cat_cow", reps = 10),
            StepSpec("pelvic_tilt", reps = 12),
            StepSpec("bird_dog", sets = 2, reps = 8),
            StepSpec("glute_bridge", sets = 2, reps = 12),
            StepSpec("dead_bug", sets = 2, reps = 8),
            StepSpec("plank", sets = 2, seconds = 30, rest = 20),
            StepSpec("knees_to_chest", seconds = 30),
            StepSpec("lying_twist", seconds = 30),
            StepSpec("childs_pose", seconds = 45, rest = 0),
        ),
    )

    val strength = PlanTemplate(
        key = "back_strength",
        name = "Rücken-Kräftigung",
        emoji = "💪",
        color = 2,
        description = "Intensiver für einen stabilen Rumpf – dreimal pro Woche.",
        days = setOf(1, 3, 5),
        reminder = Reminder(minuteOfDay = 18 * 60, followUp = false),
        steps = listOf(
            StepSpec("cat_cow", reps = 10),
            StepSpec("bird_dog", sets = 3, reps = 10, rest = 20),
            StepSpec("glute_bridge", sets = 3, reps = 15, rest = 20),
            StepSpec("dead_bug", sets = 3, reps = 10, rest = 20),
            StepSpec("plank", sets = 3, seconds = 40, rest = 30),
            StepSpec("side_plank", sets = 2, seconds = 25, rest = 20),
            StepSpec("superman", sets = 3, reps = 10, rest = 20),
            StepSpec("childs_pose", seconds = 60, rest = 0),
        ),
    )

    val office = PlanTemplate(
        key = "office_break",
        name = "Büro-Pause",
        emoji = "🪑",
        color = 4,
        description = "5 Minuten am Schreibtisch – ohne Matte, ohne Umziehen.",
        days = WEEKDAYS,
        reminder = Reminder(minuteOfDay = 14 * 60, followUp = false),
        steps = listOf(
            StepSpec("chin_tuck", reps = 10, rest = 5),
            StepSpec("shoulder_squeeze", reps = 12, rest = 5),
            StepSpec("seated_twist", seconds = 20, rest = 5),
            StepSpec("side_bend", seconds = 20, rest = 5),
            StepSpec("belly_breathing", seconds = 60, rest = 0),
        ),
    )

    val evening = PlanTemplate(
        key = "evening_relax",
        name = "Abend-Entspannung",
        emoji = "🌙",
        color = 5,
        description = "Sanfte Dehnungen zum Runterkommen vor dem Schlafen.",
        days = ALL_DAYS,
        reminder = Reminder(minuteOfDay = 21 * 60 + 30, followUp = false),
        steps = listOf(
            StepSpec("knees_to_chest", seconds = 30, rest = 10),
            StepSpec("figure_four", seconds = 30, rest = 10),
            StepSpec("hamstring_strap", seconds = 30, rest = 10),
            StepSpec("lying_twist", seconds = 30, rest = 10),
            StepSpec("childs_pose", seconds = 60, rest = 10),
            StepSpec("belly_breathing", seconds = 90, rest = 0),
        ),
    )

    val all = listOf(dailyBack, strength, office, evening)

    fun find(key: String): PlanTemplate? = all.firstOrNull { it.key == key }

    fun PlanTemplate.toPlan(id: String, today: Long, newId: () -> String): WorkoutPlan = WorkoutPlan(
        id = id,
        name = name,
        emoji = emoji,
        color = color,
        days = days,
        reminder = reminder,
        createdEpochDay = today,
        steps = steps.mapNotNull { spec ->
            val exercise = ExerciseLibrary.find(spec.exerciseId) ?: return@mapNotNull null
            val mode = if (spec.seconds != null) ExerciseMode.TIMED else if (spec.reps != null) ExerciseMode.REPS else exercise.mode
            PlanStep(
                id = newId(),
                exerciseId = exercise.id,
                mode = mode,
                seconds = spec.seconds ?: exercise.seconds,
                reps = spec.reps ?: exercise.reps,
                sets = spec.sets,
                restSeconds = spec.rest,
            )
        },
    )
}

data class SupplementTemplate(
    val key: String,
    val name: String,
    val emoji: String,
    val dose: String,
    val form: SupplementForm,
    val slots: Set<TimeSlot>,
    val hint: IntakeHint,
    val unitsPerIntake: Int = 1,
    val packSize: Int = 60,
    val tip: String,
    val color: Int,
)

fun SupplementTemplate.toSupplement(id: String, today: Long): Supplement = Supplement(
    id = id,
    name = name,
    emoji = emoji,
    dose = dose,
    form = form,
    slots = slots,
    hint = hint,
    unitsPerIntake = unitsPerIntake,
    packSize = packSize,
    stock = packSize,
    templateKey = key,
    color = color,
    createdEpochDay = today,
)

/**
 * Häufige Nahrungsergänzungsmittel mit üblichen Einnahme-Empfehlungen.
 * Die Hinweise sind allgemein gehalten und ersetzen keine ärztliche Beratung.
 */
object SupplementTemplates {
    val all = listOf(
        SupplementTemplate(
            "vitamin_d", "Vitamin D3", "☀️", "1000 IE", SupplementForm.DROPS, setOf(TimeSlot.MORNING), IntakeHint.WITH_MEAL,
            unitsPerIntake = 1, packSize = 200,
            tip = "Fettlöslich – am besten zu einer Mahlzeit mit etwas Fett einnehmen.", color = 1,
        ),
        SupplementTemplate(
            "vitamin_d_k2", "Vitamin D3 + K2", "🌞", "1 Kapsel", SupplementForm.CAPSULE, setOf(TimeSlot.MORNING), IntakeHint.WITH_MEAL,
            packSize = 120, tip = "Beide Vitamine sind fettlöslich – zum Frühstück oder Mittagessen.", color = 1,
        ),
        SupplementTemplate(
            "magnesium", "Magnesium", "🌙", "300 mg", SupplementForm.CAPSULE, setOf(TimeSlot.EVENING), IntakeHint.ANY,
            packSize = 120, tip = "Viele nehmen Magnesium abends. Bei hoher Dosis auf zwei Einnahmen verteilen.", color = 5,
        ),
        SupplementTemplate(
            "omega3", "Omega-3", "🐟", "1 Kapsel", SupplementForm.CAPSULE, setOf(TimeSlot.NOON), IntakeHint.WITH_MEAL,
            packSize = 90, tip = "Zu einer Mahlzeit verbessert die Aufnahme und verhindert fischiges Aufstoßen.", color = 3,
        ),
        SupplementTemplate(
            "zinc", "Zink", "🛡️", "10 mg", SupplementForm.TABLET, setOf(TimeSlot.EVENING), IntakeHint.WITH_MEAL,
            packSize = 100, tip = "Auf nüchternen Magen kann Zink Übelkeit auslösen – lieber zum Essen.", color = 6,
        ),
        SupplementTemplate(
            "iron", "Eisen", "🩸", "1 Tablette", SupplementForm.TABLET, setOf(TimeSlot.MORNING), IntakeHint.EMPTY_STOMACH,
            packSize = 50, tip = "Nicht zusammen mit Kaffee, Tee, Milch oder Calcium. Vitamin C kann die Aufnahme verbessern.", color = 2,
        ),
        SupplementTemplate(
            "b12", "Vitamin B12", "⚡", "500 µg", SupplementForm.TABLET, setOf(TimeSlot.MORNING), IntakeHint.ANY,
            packSize = 90, tip = "Morgens eingenommen passt B12 gut in die Routine.", color = 4,
        ),
        SupplementTemplate(
            "vitamin_c", "Vitamin C", "🍊", "500 mg", SupplementForm.TABLET, setOf(TimeSlot.MORNING), IntakeHint.ANY,
            packSize = 100, tip = "Kann zusammen mit Eisen dessen Aufnahme unterstützen.", color = 1,
        ),
        SupplementTemplate(
            "calcium", "Calcium", "🦴", "500 mg", SupplementForm.TABLET, setOf(TimeSlot.EVENING), IntakeHint.WITH_MEAL,
            packSize = 60, tip = "Zeitversetzt zu Eisen einnehmen.", color = 0,
        ),
        SupplementTemplate(
            "multi", "Multivitamin", "💊", "1 Tablette", SupplementForm.TABLET, setOf(TimeSlot.MORNING), IntakeHint.WITH_MEAL,
            packSize = 90, tip = "Zum Frühstück – fettlösliche Vitamine brauchen eine Mahlzeit.", color = 3,
        ),
        SupplementTemplate(
            "creatine", "Kreatin", "🏋️", "5 g", SupplementForm.POWDER, setOf(TimeSlot.MORNING), IntakeHint.ANY,
            unitsPerIntake = 5, packSize = 500, tip = "Täglich nehmen – der genaue Zeitpunkt ist zweitrangig. Viel trinken.", color = 2,
        ),
        SupplementTemplate(
            "protein", "Protein-Shake", "🥤", "30 g", SupplementForm.POWDER, setOf(TimeSlot.NOON), IntakeHint.ANY,
            unitsPerIntake = 30, packSize = 1000, tip = "Praktisch nach dem Training oder als Snack.", color = 4,
        ),
        SupplementTemplate(
            "collagen", "Kollagen", "✨", "10 g", SupplementForm.POWDER, setOf(TimeSlot.MORNING), IntakeHint.ANY,
            unitsPerIntake = 10, packSize = 300, tip = "Lässt sich gut in Kaffee, Tee oder Joghurt einrühren.", color = 6,
        ),
        SupplementTemplate(
            "probiotic", "Probiotikum", "🦠", "1 Kapsel", SupplementForm.CAPSULE, setOf(TimeSlot.MORNING), IntakeHint.EMPTY_STOMACH,
            packSize = 30, tip = "Hinweise auf der Packung beachten – oft vor dem Frühstück.", color = 3,
        ),
        SupplementTemplate(
            "folic_acid", "Folsäure", "🌿", "400 µg", SupplementForm.TABLET, setOf(TimeSlot.MORNING), IntakeHint.ANY,
            packSize = 90, tip = "Am besten täglich zur gleichen Zeit.", color = 3,
        ),
        SupplementTemplate(
            "curcuma", "Kurkuma", "🟡", "1 Kapsel", SupplementForm.CAPSULE, setOf(TimeSlot.NOON), IntakeHint.WITH_MEAL,
            packSize = 60, tip = "Zu einer Mahlzeit einnehmen.", color = 1,
        ),
    )

    fun find(key: String?): SupplementTemplate? = all.firstOrNull { it.key == key }
}

/** Kleine Alltagstipps für einen gesunden Rücken, einer pro Tag. */
object BackTips {
    val all = listOf(
        "Steh alle 30 bis 45 Minuten kurz auf – Bewegung ist Nahrung für die Bandscheiben.",
        "Heb schwere Dinge aus den Beinen: in die Knie gehen, Rücken gerade, Last nah am Körper.",
        "Schon 10 Minuten Spazierengehen entlasten einen verspannten Rücken.",
        "Bildschirm auf Augenhöhe – so bleibt dein Nacken entspannt.",
        "Trag Taschen abwechselnd links und rechts oder nutze einen Rucksack.",
        "Regelmäßig ist wichtiger als intensiv: lieber jeden Tag 10 Minuten als einmal pro Woche eine Stunde.",
        "Achte beim Sitzen darauf, dass deine Füße flach auf dem Boden stehen.",
        "Tief in den Bauch atmen entspannt auch die Rückenmuskulatur.",
        "Beim Zähneputzen leicht in die Knie gehen – eine kleine Übung für zwischendurch.",
        "Wechsle beim Arbeiten öfter die Position – die beste Haltung ist die nächste.",
        "Ausreichend trinken hält die Bandscheiben elastisch.",
        "Schlaf auf einer Matratze, auf der deine Wirbelsäule gerade liegt.",
        "Starke Bauchmuskeln sind die beste Stütze für deinen Rücken.",
        "Bei Schmerzen gilt: sanft bewegen statt komplett schonen – im Zweifel ärztlich abklären.",
        "Telefonierst du? Steh dabei auf und geh ein paar Schritte.",
        "Feiere kleine Erfolge – jede erledigte Einheit zählt.",
    )

    fun forDay(epochDay: Long): String = all[Math.floorMod(epochDay, all.size.toLong()).toInt()]
}
