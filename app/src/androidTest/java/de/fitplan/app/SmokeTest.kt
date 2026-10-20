package de.fitplan.app

import android.Manifest
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import de.fitplan.app.core.logic.dueSupplements
import de.fitplan.app.core.model.ThemeMode
import de.fitplan.app.core.model.TimeSlot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/**
 * Klickt einmal durch die ganze App – von der Einrichtung über ein Training bis zu den
 * Einstellungen – und legt dabei Screenshots ab.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    private val outputDir: File by lazy {
        val custom = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let(::File)
        val dir = custom ?: File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "screenshots")
        dir.apply { mkdirs() }
    }

    /** Speichert eine verkleinerte JPEG-Aufnahme, klein genug für das CI-Log. */
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap()
        val width = 432
        val scaled = Bitmap.createScaledBitmap(bitmap, width, bitmap.height * width / bitmap.width, true)
        File(outputDir, "$name.jpg").outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 72, it) }
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun exists(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun scrollTo(text: String) {
        compose.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text))
    }

    private fun tab(label: String): SemanticsNodeInteraction =
        compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))

    private fun back() {
        // Echte Zurück-Taste, damit auch Bottom Sheets in ihrem eigenen Fenster reagieren.
        Espresso.pressBack()
        compose.waitForIdle()
    }

    @Test
    fun walkThroughApp() {
        try {
            walkThrough()
        } catch (error: Throwable) {
            runCatching { screenshot("99_fehler") }
            throw error
        }
    }

    private fun walkThrough() {
        // Einrichtung
        waitForText("Willkommen bei FitPlan")
        compose.onNodeWithText("Wie dürfen wir dich nennen? (optional)").performTextInput("Mo")
        screenshot("01_willkommen")
        compose.onNodeWithText("Los geht's").performClick()
        waitForText("Dein Training")
        screenshot("02_training_auswahl")
        compose.onNodeWithText("Weiter").performClick()
        waitForText("Nimmst du Nahrungsergänzung?")
        compose.onNodeWithText("☀️ Vitamin D3").performClick()
        compose.onNodeWithText("🌙 Magnesium").performClick()
        screenshot("03_supplement_auswahl")
        compose.onNodeWithText("Weiter").performClick()
        waitForText("Damit Erinnerungen ankommen")
        screenshot("04_erinnerungen")
        compose.onNodeWithText("Fertig").performClick()

        // Startseite
        waitForText("Training heute")
        screenshot("05_heute")
        val app = compose.activity.container
        val data = runBlocking { app.repository.current() }
        assertEquals("Mo", data.settings.name)
        assertEquals(1, data.plans.size)
        assertEquals(2, data.supplements.size)

        // Erinnerungen und Benachrichtigungen dürfen nicht abstürzen.
        app.scheduler.reschedule(data)
        app.notifications.showWorkout(data, data.plans.first(), followUp = false, streak = 3)
        val today = LocalDate.now()
        app.notifications.showSupplements(TimeSlot.MORNING, data.dueSupplements(today, TimeSlot.MORNING), today)
        app.notifications.showTest()

        scrollTo("Vitamin D3")
        compose.onNodeWithText("Vitamin D3").performClick()
        screenshot("06_heute_supplement_abgehakt")

        // Geführtes Training. Die Phasen laufen in Echtzeit; ohne Vorbereitungszeit beginnt der
        // Plan mit einer Wiederholungs-Übung, die auf „Erledigt“ wartet – das macht den Test unabhängig vom Tempo.
        runBlocking { app.repository.updateSettings { it.copy(getReadySeconds = 0, voice = false, sound = false) } }
        scrollTo("Starten")
        compose.onNodeWithText("Starten").performClick()
        waitForText("Erledigt")
        screenshot("08_player_uebung")
        compose.onNodeWithText("Erledigt").performClick()
        screenshot("09_player_pause")
        var guard = 0
        while (!exists("Geschafft!") && guard++ < 120) {
            compose.onNodeWithContentDescription("Überspringen").performClick()
            compose.waitForIdle()
        }
        waitForText("Geschafft!")
        compose.onNodeWithText("🙂").performClick()
        screenshot("10_geschafft")
        compose.onNodeWithText("Fertig").performClick()
        waitForText("Erledigt um", substring = true)
        waitForText("🙂", substring = true)
        screenshot("11_heute_erledigt")
        val afterWorkout = runBlocking { app.repository.current() }
        assertEquals(1, afterWorkout.sessions.size)
        assertEquals(4, afterWorkout.sessions.single().feeling)

        // Training
        tab("Training").performClick()
        waitForText("Übungsbibliothek")
        screenshot("12_training")
        compose.onNodeWithText("Rücken täglich").performClick()
        waitForText("Plan bearbeiten")
        screenshot("13_plan_editor")
        back()
        waitForText("Übungsbibliothek")
        compose.onNodeWithText("Neuer Plan").performClick()
        waitForText("Neuen Plan erstellen")
        screenshot("14_vorlagen")
        back()
        compose.onNodeWithText("Übungsbibliothek").performClick()
        waitForText("Katze-Kuh")
        compose.onNodeWithText("Katze-Kuh").performClick()
        screenshot("15_bibliothek")
        back()

        // Supplements
        tab("Supplements").performClick()
        waitForText("Diese Woche eingenommen")
        screenshot("16_supplements")
        compose.onNodeWithText("Magnesium").performClick()
        waitForText("Supplement bearbeiten")
        screenshot("17_supplement_editor")
        back()

        // Verlauf
        tab("Verlauf").performClick()
        waitForText("Rückengefühl")
        screenshot("18_verlauf")

        // Einstellungen
        tab("Heute").performClick()
        waitForText("Training heute")
        compose.onNodeWithContentDescription("Einstellungen").performClick()
        waitForText("Erinnerungen")
        screenshot("19_einstellungen")
        back()

        // Dunkles Design
        runBlocking { app.repository.updateSettings { it.copy(themeMode = ThemeMode.DARK) } }
        waitForText("Training heute")
        screenshot("20_heute_dunkel")
        tab("Supplements").performClick()
        waitForText("Diese Woche eingenommen")
        screenshot("21_supplements_dunkel")

        assertTrue(outputDir.listFiles().orEmpty().isNotEmpty())
    }
}
