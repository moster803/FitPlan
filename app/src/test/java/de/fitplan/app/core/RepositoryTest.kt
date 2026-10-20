package de.fitplan.app.core

import de.fitplan.app.core.data.AppJson
import de.fitplan.app.core.data.DataStorage
import de.fitplan.app.core.data.FileStorage
import de.fitplan.app.core.data.FitRepository
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.logic.daysLeft
import de.fitplan.app.core.logic.interactionHints
import de.fitplan.app.core.logic.isTaken
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.TimeSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.ZoneId

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
}
