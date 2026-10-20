package de.fitplan.app.ui

import de.fitplan.app.AppContainer
import de.fitplan.app.core.data.FitRepository
import de.fitplan.app.core.data.IntakeChange
import de.fitplan.app.core.model.TimeSlot
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Schreibende Aktionen aus der Oberfläche. Sie laufen im App-Scope, damit ein Speichern
 * nicht abbricht, wenn der Nutzer den Bildschirm verlässt.
 */
class AppActions(private val container: AppContainer) {

    val repository: FitRepository get() = container.repository

    fun launch(block: suspend FitRepository.() -> Unit): Job =
        container.appScope.launch { container.repository.block() }

    fun toggleIntake(supplementId: String, date: LocalDate, slot: TimeSlot) = container.appScope.launch {
        notifyLowStock(repository.toggleIntake(supplementId, date.toEpochDay(), slot))
        container.notifications.cancelSupplements(slot)
    }

    fun takeAll(date: LocalDate, slot: TimeSlot) = container.appScope.launch {
        notifyLowStock(repository.takeAll(date, slot))
        container.notifications.cancelSupplements(slot)
    }

    fun cancelWorkoutNotification(planId: String) = container.notifications.cancelWorkout(planId)

    private suspend fun notifyLowStock(change: IntakeChange) {
        if (repository.current().settings.lowStockAlerts) change.lowStock.forEach(container.notifications::showLowStock)
    }
}
