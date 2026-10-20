package de.fitplan.app.core

import de.fitplan.app.core.data.AppJson
import de.fitplan.app.core.data.DataStorage
import de.fitplan.app.core.data.FileStorage
import de.fitplan.app.core.data.FitRepository
import de.fitplan.app.core.data.SystemZoneClock
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.logic.DayState
import de.fitplan.app.core.logic.currentStreak
import de.fitplan.app.core.logic.dayState
import de.fitplan.app.core.logic.daysLeft
import de.fitplan.app.core.logic.interactionHints
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.logic.longestStreak
import de.fitplan.app.core.model.ALL_DAYS
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Reminder
import de.fitplan.app.core.model.Settings
import de.fitplan.app.core.model.TimeSlot
import de.fitplan.app.core.model.WEEKDAYS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.ZoneId
import java.util.TimeZone

class RepositoryTest {

    private class MemoryStorage(var content: String? = null) : DataStorage {
        var corrupt: String? = null
        override fun read() = content
        override fun write(content: String) {
            this.content = content
        }
        override fun backupCorrupt(content: String) {
            corrupt = content
        }
    }

    private val zone = ZoneId.of("Europe/Berlin")
    private val clock = Clock.fixed(MONDAY.atTime(9, 0).atZone(zone).toInstant(), zone)

    private fun repo(storage: DataStorage = MemoryStorage()) = FitRepository(storage, Dispatchers.Unconfined, clock)

    @Test
    fun `json roundtrip keeps everything`() {
        val data = AppData(
            plans = listOf(plan()),
            supplements = listOf(supplement(trackStock = true, stock = 30)),
            sessions = listOf(session(MONDAY, feeling = 4)),
        )
        assertEquals(data, AppJson.decode(AppJson.encode(data)))
    }

    @Test
    fun `unknown fields are ignored for forward compatibility`() {
        val decoded = AppJson.decode("""{"schemaVersion":1,"neuesFeld":true,"settings":{"name":"Mo","x":1}}""")
        assertEquals("Mo", decoded.settings.name)
    }

    @Test
    fun `corrupt data is backed up instead of lost`() = runTest {
        val storage = MemoryStorage("{kaputt")
        val loaded = repo(storage).load()
        assertEquals(AppData(), loaded)
        assertEquals("{kaputt", storage.corrupt)
    }

    @Test
    fun `toggle intake adjusts stock and reports low stock`() = runTest {
        val storage = MemoryStorage(AppJson.encode(AppData(supplements = listOf(supplement(trackStock = true, stock = 8)))))
        val repo = repo(storage)
        val day = MONDAY.toEpochDay()

        val change = repo.toggleIntake("s1", day, TimeSlot.EVENING)
        var data = repo.current()
        assertTrue(data.isTaken("s1", day, TimeSlot.EVENING))
        assertEquals(7, data.supplements[0].stock)
        assertEquals(1, change.lowStock.size) // 7 Tage ≤ Warnschwelle 7

        repo.toggleIntake("s1", day, TimeSlot.EVENING)
        data = repo.current()
        assertFalse(data.isTaken("s1", day, TimeSlot.EVENING))
        assertEquals(8, data.supplements[0].stock)
        // Gespeichert wurde auch.
        assertEquals(data, AppJson.decode(storage.content!!))
    }

    @Test
    fun `take all marks only pending due supplements`() = runTest {
        val storage = MemoryStorage(
            AppJson.encode(
                AppData(
                    supplements = listOf(
                        supplement("a", slots = setOf(TimeSlot.MORNING)),
                        supplement("b", slots = setOf(TimeSlot.MORNING), days = setOf(2)), // nur dienstags
                        supplement("c", slots = setOf(TimeSlot.EVENING)),
                    ),
                ),
            ),
        )
        val repo = repo(storage)
        repo.takeAll(MONDAY, TimeSlot.MORNING)
        val data = repo.current()
        assertEquals(listOf("a"), data.intakes.map { it.supplementId })
    }

    @Test
    fun `onboarding creates plan with chosen reminder time`() = runTest {
        val repo = repo()
        repo.completeOnboarding("  Mo ", listOf(PlanTemplates.dailyBack), reminderMinute = 7 * 60, supplements = emptyList())
        val data = repo.current()
        assertTrue(data.settings.onboardingDone)
        assertEquals("Mo", data.settings.name)
        assertEquals(7 * 60, data.plans.single().reminder.minuteOfDay)
        assertEquals(19 * 60, data.plans.single().reminder.followUpMinuteOfDay)
        assertEquals(MONDAY.toEpochDay(), data.plans.single().createdEpochDay)
    }

    @Test
    fun `deleting custom exercise removes it from plans`() = runTest {
        val repo = repo()
        val plan = repo.addPlanFromTemplate(PlanTemplates.dailyBack)
        repo.upsertCustomExercise(de.fitplan.app.core.model.Exercise(id = "custom", name = "Eigene"))
        repo.upsertPlan(plan.copy(steps = plan.steps + plan.steps.first().copy(id = "x", exerciseId = "custom")))
        repo.deleteCustomExercise("custom")
        val data = repo.current()
        assertTrue(data.customExercises.isEmpty())
        assertEquals(plan.steps.size, data.plans.single().steps.size)
    }

    @Test
    fun `file storage writes atomically and reads back`() {
        val dir = Files.createTempDirectory("fitplan").toFile()
        val file = File(dir, "data.json")
        val storage = FileStorage(file)
        assertEquals(null, storage.read())
        storage.write("eins")
        storage.write("zwei")
        assertEquals("zwei", storage.read())
        assertFalse(File(dir, "data.json.tmp").exists())
        dir.deleteRecursively()
    }

    @Test
    fun `stock days left and interaction hints`() {
        val iron = supplement("i", name = "Eisen", slots = setOf(TimeSlot.MORNING))
        val calcium = supplement("c", name = "Calcium", slots = setOf(TimeSlot.MORNING, TimeSlot.EVENING), trackStock = true, stock = 20)
        assertEquals(10, calcium.daysLeft())
        val hints = interactionHints(listOf(iron, calcium))
        assertEquals(1, hints.size)
        assertEquals(TimeSlot.MORNING, hints[0].slot)
        assertNotNull(hints[0].message)
    }

    @Test
    fun `undo restores only what was deducted`() = runTest {
        // Kreatin: 5 g pro Einnahme, nur noch 3 g da.
        val repo = repo(MemoryStorage(AppJson.encode(AppData(supplements = listOf(supplement(trackStock = true, stock = 3, units = 5))))))
        val day = MONDAY.toEpochDay()
        repo.toggleIntake("s1", day, TimeSlot.EVENING)
        assertEquals(0, repo.current().supplements[0].stock)
        repo.toggleIntake("s1", day, TimeSlot.EVENING)
        assertEquals(3, repo.current().supplements[0].stock)
    }

    @Test
    fun `import rejects json of other apps and keeps data`() = runTest {
        val repo = repo(MemoryStorage(AppJson.encode(AppData(plans = listOf(plan())))))
        assertTrue(runCatching { repo.importJson("""{"foo":1}""") }.isFailure)
        assertTrue(runCatching { repo.importJson("[]") }.isFailure)
        assertEquals(1, repo.current().plans.size)
    }

    @Test
    fun `import repairs values that would crash the app`() = runTest {
        val broken = AppData(
            settings = Settings(slotTimes = mapOf(TimeSlot.MORNING to -5)),
            plans = listOf(
                plan(days = setOf(0, 3, 8), reminder = Reminder(minuteOfDay = -30, followUpMinuteOfDay = 1500)),
                plan(), // doppelte ID
            ),
            sessions = listOf(session(MONDAY, feeling = 6)),
        )
        val repo = repo()
        repo.importJson(AppJson.encode(broken))
        val data = repo.current()
        val plan = data.plans.single()
        assertEquals(setOf(3), plan.days)
        assertEquals(0, plan.reminder.minuteOfDay)
        assertEquals(24 * 60 - 1, plan.reminder.followUpMinuteOfDay)
        assertNull(data.sessions.single().feeling)
        assertEquals(0, data.settings.slotMinute(TimeSlot.MORNING))
        assertTrue(data.settings.onboardingDone)
    }

    @Test
    fun `finishing onboarding twice creates plans once`() = runTest {
        val repo = repo()
        repeat(2) { repo.completeOnboarding("Mo", listOf(PlanTemplates.dailyBack), reminderMinute = 7 * 60, supplements = listOf(supplement())) }
        val data = repo.current()
        assertEquals(1, data.plans.size)
        assertEquals(1, data.supplements.size)
    }

    @Test
    fun `quick log counts once per day`() = runTest {
        val repo = repo(MemoryStorage(AppJson.encode(AppData(plans = listOf(plan())))))
        val first = repo.logSession("p1", 600, 1, 1, quickLog = true)
        val second = repo.logSession("p1", 600, 1, 1, quickLog = true)
        assertEquals(first, second)
        assertEquals(1, repo.current().sessions.size)
        // „Schon erledigt“ aus der Benachrichtigung von gestern zählt für gestern.
        repo.logSession("p1", 600, 1, 1, quickLog = true, epochDay = MONDAY.minusDays(1).toEpochDay())
        assertEquals(listOf(MONDAY.toEpochDay(), MONDAY.minusDays(1).toEpochDay()), repo.current().sessions.map { it.epochDay })
    }

    @Test
    fun `adding training days does not turn past days into missed ones`() = runTest {
        val weekdaysPlan = plan(days = WEEKDAYS, created = MONDAY.minusDays(21))
        // Drei Wochen lang jeden Werktag trainiert.
        val sessions = (1L..21L).map { MONDAY.minusDays(it) }.filter { it.dayOfWeek.value <= 5 }.map { session(it) }
        val repo = repo(MemoryStorage(AppJson.encode(AppData(plans = listOf(weekdaysPlan), sessions = sessions))))
        assertEquals(15, repo.current().currentStreak(MONDAY))

        repo.upsertPlan(weekdaysPlan.copy(days = ALL_DAYS))
        val data = repo.current()
        assertEquals(15, data.currentStreak(MONDAY))
        assertEquals(15, data.longestStreak(MONDAY))
        // Am Sonntag galt noch der alte Rhythmus, ab heute der neue.
        assertEquals(DayState.REST, data.dayState(MONDAY.minusDays(1), MONDAY))
        assertEquals(DayState.OPEN, data.dayState(MONDAY, MONDAY))
    }

    @Test
    fun `system clock follows time zone changes`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
            assertEquals(ZoneId.of("Europe/Berlin"), SystemZoneClock.zone)
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            assertEquals(ZoneId.of("America/New_York"), SystemZoneClock.zone)
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
