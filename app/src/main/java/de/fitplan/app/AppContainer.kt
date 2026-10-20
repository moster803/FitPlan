package de.fitplan.app

import android.content.Context
import android.util.Log
import de.fitplan.app.core.data.FileStorage
import de.fitplan.app.core.data.FitRepository
import de.fitplan.app.reminders.Notifications
import de.fitplan.app.reminders.ReminderScheduler
import de.fitplan.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/** Einfache manuelle Dependency Injection – ein Objekt pro App-Prozess. */
class AppContainer(context: Context) {
    /** Ein Fehler in einer Hintergrundaufgabe (Speichern, Wecker, Widget) wird protokolliert, statt die App zu beenden. */
    val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, error -> Log.e(TAG, "Fehler in einer Hintergrundaufgabe", error) },
    )
    val repository = FitRepository(FileStorage(File(context.filesDir, DATA_FILE)), Dispatchers.IO)
    val notifications = Notifications(context)
    val scheduler = ReminderScheduler(context)
    val widgets = WidgetUpdater(context)

    companion object {
        const val DATA_FILE = "fitplan-data.json"
        const val TAG = "FitPlan"
    }
}

val Context.container: AppContainer get() = (applicationContext as FitPlanApp).container
