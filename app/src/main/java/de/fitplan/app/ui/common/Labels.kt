package de.fitplan.app.ui.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import de.fitplan.app.R
import de.fitplan.app.core.model.ALL_DAYS
import de.fitplan.app.core.model.ExerciseCategory
import de.fitplan.app.core.model.ExerciseMode
import de.fitplan.app.core.model.IntakeHint
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.model.SupplementForm
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WEEKDAYS
import java.time.format.DateTimeFormatter
import java.util.Locale

val GERMAN: Locale = Locale.GERMANY

val TimeSlot.label: Int
    @StringRes get() = when (this) {
        TimeSlot.MORNING -> R.string.slot_morning
        TimeSlot.NOON -> R.string.slot_noon
        TimeSlot.EVENING -> R.string.slot_evening
        TimeSlot.NIGHT -> R.string.slot_night
    }

val TimeSlot.icon: ImageVector
    get() = when (this) {
        TimeSlot.MORNING -> Icons.Rounded.WbTwilight
        TimeSlot.NOON -> Icons.Rounded.LightMode
        TimeSlot.EVENING -> Icons.Rounded.NightsStay
        TimeSlot.NIGHT -> Icons.Rounded.Bedtime
    }

val IntakeHint.label: Int
    @StringRes get() = when (this) {
        IntakeHint.ANY -> R.string.hint_any
        IntakeHint.WITH_MEAL -> R.string.hint_with_meal
        IntakeHint.EMPTY_STOMACH -> R.string.hint_empty_stomach
        IntakeHint.BEFORE_BED -> R.string.hint_before_bed
    }

val IntakeHint.emoji: String
    get() = when (this) {
        IntakeHint.ANY -> "🕐"
        IntakeHint.WITH_MEAL -> "🍽️"
        IntakeHint.EMPTY_STOMACH -> "⏳"
        IntakeHint.BEFORE_BED -> "🛏️"
    }

val SupplementForm.label: Int
    @StringRes get() = when (this) {
        SupplementForm.CAPSULE -> R.string.form_capsule
        SupplementForm.TABLET -> R.string.form_tablet
        SupplementForm.POWDER -> R.string.form_powder
        SupplementForm.DROPS -> R.string.form_drops
        SupplementForm.LIQUID -> R.string.form_liquid
        SupplementForm.GUMMY -> R.string.form_gummy
    }

val SupplementForm.units: Int
    @PluralsRes get() = when (this) {
        SupplementForm.CAPSULE -> R.plurals.unit_capsule
        SupplementForm.TABLET -> R.plurals.unit_tablet
        SupplementForm.POWDER -> R.plurals.unit_gram
        SupplementForm.DROPS -> R.plurals.unit_drop
        SupplementForm.LIQUID -> R.plurals.unit_ml
        SupplementForm.GUMMY -> R.plurals.unit_gummy
    }

val SupplementForm.emoji: String
    get() = when (this) {
        SupplementForm.CAPSULE -> "💊"
        SupplementForm.TABLET -> "⚪"
        SupplementForm.POWDER -> "🥄"
        SupplementForm.DROPS -> "💧"
        SupplementForm.LIQUID -> "🧴"
        SupplementForm.GUMMY -> "🍬"
    }

val ExerciseCategory.label: Int
    @StringRes get() = when (this) {
        ExerciseCategory.MOBILITY -> R.string.category_mobility
        ExerciseCategory.STRENGTH -> R.string.category_strength
        ExerciseCategory.STRETCH -> R.string.category_stretch
        ExerciseCategory.RELAX -> R.string.category_relax
        ExerciseCategory.ENDURANCE -> R.string.category_endurance
    }

val ExerciseCategory.emoji: String
    get() = when (this) {
        ExerciseCategory.MOBILITY -> "🌀"
        ExerciseCategory.STRENGTH -> "💪"
        ExerciseCategory.STRETCH -> "🤸"
        ExerciseCategory.RELAX -> "🌿"
        ExerciseCategory.ENDURANCE -> "🚶"
    }

@Composable
fun unitText(form: SupplementForm, count: Int): String = pluralStringResource(form.units, count, count)

/** „08:00“ */
fun formatMinute(minuteOfDay: Int): String = "%02d:%02d".format(GERMAN, (minuteOfDay / 60) % 24, minuteOfDay % 60)

fun formatDuration(seconds: Int): String = "%d:%02d".format(GERMAN, seconds / 60, seconds % 60)

val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM", GERMAN)
val WEEKDAY_DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM", GERMAN)
val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", GERMAN)
val MONTH_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", GERMAN)

/** Kurze Wochentagsnamen, Index 0 = Montag. */
@Composable
fun weekdayShort(): Array<String> = stringArrayResource(R.array.weekday_short)

@Composable
fun daysSummary(days: Set<Int>): String {
    val names = weekdayShort()
    return when (days) {
        ALL_DAYS -> stringResource(R.string.days_daily)
        WEEKDAYS -> stringResource(R.string.days_weekdays)
        setOf(6, 7) -> stringResource(R.string.days_weekend)
        emptySet<Int>() -> stringResource(R.string.days_none)
        else -> days.sorted().joinToString(" · ") { names[it - 1] }
    }
}

@Composable
fun stepSummary(step: PlanStep, perSide: Boolean): String {
    val amount = when (step.mode) {
        ExerciseMode.TIMED -> stringResource(R.string.step_seconds, step.seconds)
        ExerciseMode.REPS -> stringResource(R.string.step_reps, step.reps)
    }
    val side = if (perSide) " " + stringResource(R.string.per_side) else ""
    val sets = if (step.sets > 1) "${step.sets} × " else ""
    return "$sets$amount$side"
}
