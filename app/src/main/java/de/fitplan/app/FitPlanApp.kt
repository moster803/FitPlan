package de.fitplan.app

import android.app.Application
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
                    container.scheduler.reschedule(data)
                    container.widgets.updateAll()
                }
        }
    }
}
