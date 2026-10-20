package de.fitplan.app.core.logic

import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import kotlin.math.floor

/** Durchschnittlicher Verbrauch in Einheiten pro Tag. */
fun Supplement.dailyUnits(): Double = unitsPerIntake.toDouble() * slots.size * days.size / 7.0

/** Wie viele Tage der Vorrat noch reicht, oder null wenn kein Vorrat erfasst wird. */
fun Supplement.daysLeft(): Int? {
    if (!trackStock) return null
    val perDay = dailyUnits()
    if (perDay <= 0.0) return null
    return floor(stock / perDay).toInt()
}

fun Supplement.isLowStock(): Boolean {
    val left = daysLeft() ?: return false
    return left <= refillWarningDays
}

data class InteractionHint(val slot: TimeSlot, val first: Supplement, val second: Supplement, val message: String)

private data class Rule(val a: List<String>, val b: List<String>, val message: String)

private val IRON = listOf("iron", "eisen")
private val RULES = listOf(
    Rule(IRON, listOf("calcium", "kalzium"), "Calcium kann die Eisenaufnahme hemmen – besser mit etwa 2 Stunden Abstand einnehmen."),
    Rule(IRON, listOf("zinc", "zink"), "Eisen und Zink konkurrieren bei der Aufnahme – besser zeitversetzt einnehmen."),
    Rule(IRON, listOf("magnesium"), "Magnesium kann die Eisenaufnahme verringern – besser zeitversetzt einnehmen."),
)

private fun Supplement.matches(keys: List<String>): Boolean {
    val name = name.lowercase()
    return keys.any { key -> templateKey == key || name.contains(key) }
}

/** Allgemeine Kombinations-Hinweise für Supplements, die zur selben Tageszeit eingeplant sind. */
fun interactionHints(supplements: List<Supplement>): List<InteractionHint> = buildList {
    val active = supplements.filter { it.active }
    TimeSlot.entries.forEach { slot ->
        val inSlot = active.filter { slot in it.slots }
        RULES.forEach { rule ->
            val first = inSlot.firstOrNull { it.matches(rule.a) } ?: return@forEach
            val second = inSlot.firstOrNull { it !== first && it.matches(rule.b) } ?: return@forEach
            add(InteractionHint(slot, first, second, rule.message))
        }
    }
}
