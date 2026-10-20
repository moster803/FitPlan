package de.fitplan.app.core.data

import de.fitplan.app.core.library.PlanTemplate
import de.fitplan.app.core.library.PlanTemplates.toPlan
import de.fitplan.app.core.logic.isDueOn
import de.fitplan.app.core.logic.isLowStock
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.logic.sessionFor
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.Intake
import de.fitplan.app.core.model.PastSchedule
import de.fitplan.app.core.model.Settings
import de.fitplan.app.core.model.Supplement
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.model.WorkoutSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** Speicherort für die serialisierten App-Daten. Auf Android eine Datei im internen Speicher. */
interface DataStorage {
    fun read(): String?
    fun write(content: String)

    /** Sichert unlesbare Daten, statt sie still zu überschreiben. */
    fun backupCorrupt(content: String)
}

object AppJson {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }

    fun encode(data: AppData): String = json.encodeToString(AppData.serializer(), data)

    fun decode(text: String): AppData = json.decodeFromString(AppData.serializer(), text)

    private val prettyJson = Json(json) { prettyPrint = true }

    fun encodePretty(data: AppData): String = prettyJson.encodeToString(AppData.serializer(), data)
}

fun newId(): String = UUID.randomUUID().toString()

/** Systemuhr, die die Zeitzone bei jedem Aufruf neu liest – nach einer Reise stimmt „heute“ sofort. */
object SystemZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = system(zone)

    override fun instant(): Instant = Instant.now()
}

/** Ergebnis einer Einnahme-Änderung; [lowStock] enthält Supplements, deren Vorrat gerade knapp geworden ist. */
data class IntakeChange(val lowStock: List<Supplement>)

/**
 * Zentrale Datenquelle der App. Alle Änderungen laufen serialisiert über einen Mutex
 * und werden sofort gespeichert.
 */
class FitRepository(
    private val storage: DataStorage,
    private val ioDispatcher: CoroutineDispatcher,
    val clock: Clock = SystemZoneClock,
) {
    private val mutex = Mutex()
    private val state = MutableStateFlow<AppData?>(null)

    val data: Flow<AppData> = state.filterNotNull()

    fun today(): LocalDate = LocalDate.now(clock)

    fun nowMillis(): Long = clock.millis()

    suspend fun load(): AppData = mutex.withLock { loadLocked() }

    private suspend fun loadLocked(): AppData {
        state.value?.let { return it }
        val loaded = withContext(ioDispatcher) {
            val text = storage.read()
            if (text.isNullOrBlank()) {
                AppData()
            } else {
                runCatching { AppJson.decode(text).sanitized() }.getOrElse {
                    storage.backupCorrupt(text)
                    AppData()
                }
            }
        }
        state.value = loaded
        return loaded
    }

    suspend fun current(): AppData = state.value ?: load()

    /** Wendet [transform] atomar an und speichert das Ergebnis. Sichtbar wird die Änderung erst, wenn sie gespeichert ist. */
    suspend fun update(transform: (AppData) -> AppData): AppData = mutex.withLock {
        val before = loadLocked()
        val after = transform(before)
        if (after != before) {
            withContext(ioDispatcher) { storage.write(AppJson.encode(after)) }
            state.value = after
        }
        after
    }

    // region Einstellungen

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        update { it.copy(settings = transform(it.settings)) }
    }

    suspend fun completeOnboarding(name: String, templates: List<PlanTemplate>, reminderMinute: Int?, supplements: List<Supplement>) {
        val today = today().toEpochDay()
        update { data ->
            // Doppeltes Tippen auf „Fertig“ legt nichts doppelt an.
            if (data.settings.onboardingDone) return@update data
            val plans = templates.mapIndexed { index, template ->
                val plan = template.toPlan(newId(), today, ::newId)
                if (index == 0 && reminderMinute != null) {
                    // Nachhaken abends um 19 Uhr bzw. drei Stunden nach einer späten Erinnerung.
                    val followUp = if (reminderMinute < 17 * 60) 19 * 60 else (reminderMinute + 3 * 60).coerceAtMost(21 * 60 + 30)
                    plan.copy(
                        reminder = plan.reminder.copy(
                            minuteOfDay = reminderMinute,
                            followUp = plan.reminder.followUp && followUp > reminderMinute,
                            followUpMinuteOfDay = followUp,
                        ),
                    )
                } else {
                    plan
                }
            }
            data.copy(
                settings = data.settings.copy(onboardingDone = true, name = name.trim()),
                plans = data.plans + plans,
                supplements = data.supplements + supplements,
            )
        }
    }

    // endregion

    // region Pläne & Übungen

    suspend fun upsertPlan(plan: WorkoutPlan) {
        val today = today().toEpochDay()
        update { data ->
            val old = data.plans.firstOrNull { it.id == plan.id }
            if (old == null) {
                data.copy(plans = data.plans + plan)
            } else {
                val updated = plan.keepingScheduleOf(old, today)
                data.copy(plans = data.plans.map { if (it.id == plan.id) updated else it })
            }
        }
    }

    /**
     * Ändern sich Trainingstage oder Aktivität, wird der bisherige Rhythmus bis gestern festgehalten.
     * Sonst würden z. B. neu hinzugefügte Wochenendtage rückwirkend als verpasst zählen und die Serie brechen.
     */
    private fun WorkoutPlan.keepingScheduleOf(old: WorkoutPlan, today: Long): WorkoutPlan {
        val history = old.scheduleHistory
        val oldActive = old.active && old.steps.isNotEmpty()
        val unchanged = old.days == days && oldActive == (active && steps.isNotEmpty())
        // Bei mehreren Änderungen am selben Tag gilt der Stand vor der ersten.
        val alreadyKeptToday = history.any { it.untilEpochDay >= today }
        return if (unchanged || alreadyKeptToday || old.createdEpochDay >= today) {
            copy(scheduleHistory = history)
        } else {
            copy(scheduleHistory = history + PastSchedule(today, old.days, oldActive))
        }
    }

    suspend fun deletePlan(planId: String) {
        update { data -> data.copy(plans = data.plans.filterNot { it.id == planId }) }
    }

    suspend fun addPlanFromTemplate(template: PlanTemplate): WorkoutPlan {
        val plan = template.toPlan(newId(), today().toEpochDay(), ::newId)
        upsertPlan(plan)
        return plan
    }

    suspend fun upsertCustomExercise(exercise: Exercise) {
        val custom = exercise.copy(builtIn = false)
        update { data ->
            val exists = data.customExercises.any { it.id == custom.id }
            data.copy(
                customExercises = if (exists) {
                    data.customExercises.map { if (it.id == custom.id) custom else it }
                } else {
                    data.customExercises + custom
                },
            )
        }
    }

    suspend fun deleteCustomExercise(exerciseId: String) {
        update { data ->
            data.copy(
                customExercises = data.customExercises.filterNot { it.id == exerciseId },
                plans = data.plans.map { plan -> plan.copy(steps = plan.steps.filterNot { it.exerciseId == exerciseId }) },
            )
        }
    }

    // endregion

    // region Trainingseinheiten

    suspend fun logSession(
        planId: String,
        durationSeconds: Int,
        completedExercises: Int,
        totalExercises: Int,
        quickLog: Boolean = false,
        epochDay: Long = today().toEpochDay(),
    ): WorkoutSession {
        val session = WorkoutSession(
            id = newId(),
            planId = planId,
            epochDay = epochDay,
            finishedAt = nowMillis(),
            durationSeconds = durationSeconds,
            completedExercises = completedExercises,
            totalExercises = totalExercises,
            quickLog = quickLog,
        )
        var result = session
        update { data ->
            // Schnelles Abhaken zählt pro Tag nur einmal, auch bei doppeltem Tippen.
            val existing = if (quickLog) data.sessionFor(planId, epochDay) else null
            if (existing != null) {
                result = existing
                data
            } else {
                data.copy(sessions = data.sessions + session)
            }
        }
        return result
    }

    suspend fun setFeeling(sessionId: String, feeling: Int?) {
        update { data ->
            data.copy(sessions = data.sessions.map { if (it.id == sessionId) it.copy(feeling = feeling?.coerceIn(1, 5)) else it })
        }
    }

    suspend fun deleteSession(sessionId: String) {
        update { data -> data.copy(sessions = data.sessions.filterNot { it.id == sessionId }) }
    }

    // endregion

    // region Supplements

    suspend fun upsertSupplement(supplement: Supplement) {
        update { data ->
            val exists = data.supplements.any { it.id == supplement.id }
            data.copy(
                supplements = if (exists) {
                    data.supplements.map { if (it.id == supplement.id) supplement else it }
                } else {
                    data.supplements + supplement
                },
            )
        }
    }

    suspend fun deleteSupplement(supplementId: String) {
        update { data ->
            data.copy(
                supplements = data.supplements.filterNot { it.id == supplementId },
                intakes = data.intakes.filterNot { it.supplementId == supplementId },
            )
        }
    }

    suspend fun refill(supplementId: String, amount: Int) {
        update { data ->
            data.copy(supplements = data.supplements.map { if (it.id == supplementId) it.copy(stock = (it.stock + amount).coerceAtLeast(0), trackStock = true) else it })
        }
    }

    /** Hakt eine Einnahme ab bzw. nimmt sie zurück und passt den Vorrat an. */
    suspend fun toggleIntake(supplementId: String, epochDay: Long, slot: TimeSlot): IntakeChange {
        var lowStock = emptyList<Supplement>()
        update { data ->
            val supplement = data.supplements.firstOrNull { it.id == supplementId } ?: return@update data
            val intake = data.intakes.firstOrNull { it.supplementId == supplementId && it.epochDay == epochDay && it.slot == slot }
            if (intake != null) {
                // Genau das zurückbuchen, was beim Abhaken abgezogen wurde – bei leerem Vorrat also nichts.
                val restore = intake.units ?: supplement.unitsPerIntake
                data.copy(
                    intakes = data.intakes.filterNot { it.supplementId == supplementId && it.epochDay == epochDay && it.slot == slot },
                    supplements = data.supplements.map { if (it.id == supplementId) it.withStockDelta(+restore) else it },
                )
            } else {
                val updated = supplement.withStockDelta(-supplement.unitsPerIntake)
                if (!supplement.isLowStock() && updated.isLowStock()) lowStock = listOf(updated)
                data.copy(
                    intakes = data.intakes + Intake(supplementId, epochDay, slot, nowMillis(), units = supplement.stock - updated.stock),
                    supplements = data.supplements.map { if (it.id == supplementId) updated else it },
                )
            }
        }
        return IntakeChange(lowStock)
    }

    /** Markiert alle fälligen, noch offenen Supplements einer Tageszeit als genommen. */
    suspend fun takeAll(date: LocalDate, slot: TimeSlot): IntakeChange {
        val epochDay = date.toEpochDay()
        val lowStock = mutableListOf<Supplement>()
        update { data ->
            val pending = data.supplements.filter { it.isDueOn(date, slot) && !data.isTaken(it.id, epochDay, slot) }
            if (pending.isEmpty()) return@update data
            val pendingIds = pending.map { it.id }.toSet()
            val intakes = mutableListOf<Intake>()
            val supplements = data.supplements.map { supplement ->
                if (supplement.id !in pendingIds) return@map supplement
                val updated = supplement.withStockDelta(-supplement.unitsPerIntake)
                if (!supplement.isLowStock() && updated.isLowStock()) lowStock += updated
                intakes += Intake(supplement.id, epochDay, slot, nowMillis(), units = supplement.stock - updated.stock)
                updated
            }
            data.copy(intakes = data.intakes + intakes, supplements = supplements)
        }
        return IntakeChange(lowStock)
    }

    private fun Supplement.withStockDelta(delta: Int): Supplement =
        if (trackStock) copy(stock = (stock + delta).coerceAtLeast(0)) else this

    // endregion

    // region Backup

    suspend fun exportJson(): String = AppJson.encodePretty(current())

    /**
     * Ersetzt alle Daten durch ein Backup. Wirft eine Exception bei ungültigem Inhalt – auch bei
     * JSON-Dateien anderer Apps, die sonst als leeres Backup alle Daten löschen würden.
     */
    suspend fun importJson(text: String) {
        val root = AppJson.json.parseToJsonElement(text)
        require(root is JsonObject && "schemaVersion" in root && "settings" in root) { "Kein FitPlan-Backup" }
        val imported = AppJson.json.decodeFromJsonElement(AppData.serializer(), root).sanitized()
        update { imported.copy(settings = imported.settings.copy(onboardingDone = true)) }
    }

    suspend fun resetAll() {
        update { AppData() }
    }

    // endregion
}
