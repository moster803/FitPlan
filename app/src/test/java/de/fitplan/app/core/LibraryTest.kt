package de.fitplan.app.core

import de.fitplan.app.core.library.BackTips
import de.fitplan.app.core.library.Evidence
import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.model.Approach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryTest {

    @Test
    fun `every built-in exercise is evidence based and complete`() {
        val ids = ExerciseLibrary.all.map { it.id }
        assertEquals("IDs müssen eindeutig sein", ids.size, ids.toSet().size)
        ExerciseLibrary.all.forEach { exercise ->
            assertTrue(exercise.id, exercise.builtIn)
            assertNotNull("${exercise.id} braucht eine Übungsform mit Studiengrundlage", exercise.approach)
            assertTrue(exercise.id, exercise.description.isNotBlank())
            assertTrue(exercise.id, exercise.steps.size >= 3)
        }
    }

    @Test
    fun `every approach has exercises and resolvable sources`() {
        Approach.entries.forEach { approach ->
            val info = Evidence.info(approach)
            assertTrue(approach.name, ExerciseLibrary.all.any { it.approach == approach })
            assertTrue(approach.name, info.sourceIds.isNotEmpty())
            info.sourceIds.forEach { assertNotNull("$approach → $it", Evidence.source(it)) }
        }
        Evidence.keyFindings.forEach { finding ->
            finding.sourceIds.forEach { assertNotNull(it, Evidence.source(it)) }
        }
        assertTrue(Evidence.sources.all { it.url.startsWith("https://") })
    }

    @Test
    fun `templates only use library exercises`() {
        PlanTemplates.all.forEach { template ->
            template.steps.forEach { step ->
                assertNotNull("${template.key} → ${step.exerciseId}", ExerciseLibrary.find(step.exerciseId))
            }
        }
        assertEquals(PlanTemplates.all.size, PlanTemplates.all.map { it.key }.toSet().size)
    }

    @Test
    fun `daily tips cycle through all entries`() {
        val tips = (0L until BackTips.all.size.toLong()).map(BackTips::forDay).toSet()
        assertEquals(BackTips.all.size, tips.size)
        assertEquals(BackTips.forDay(-1), BackTips.all.last())
    }
}
