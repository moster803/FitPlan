package de.fitplan.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class FitPlanApp : Application() {

    lateinit var container: AppContainer
        private set

    @OptIn(FlowPreview::class)
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifications.createChannels()
        container.appScope.launch {
            container.repository.load()
            // Jede Datenänderung plant Erinnerungen neu und aktualisiert das Widget.
            container.repository.data
                .distinctUntilChanged()
                .debounce(300)
                .collect { data ->
                    // Ein Fehler darf die Sammlung nicht beenden, sonst würden spätere Änderungen nicht mehr geplant.
                    runCatching { container.scheduler.reschedule(data) }
                        .onFailure { Log.e(AppContainer.TAG, "Erinnerungen konnten nicht geplant werden", it) }
                    container.widgets.updateAll()
                }
        }
    }
}
