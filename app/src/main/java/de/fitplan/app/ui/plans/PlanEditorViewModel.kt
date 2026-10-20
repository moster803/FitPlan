package de.fitplan.app.ui.plans

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.fitplan.app.core.data.newId
import de.fitplan.app.core.library.PlanTemplates
import de.fitplan.app.core.library.PlanTemplates.toPlan
import de.fitplan.app.core.logic.plan
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.PlanStep
import de.fitplan.app.core.model.WorkoutPlan
import java.time.LocalDate

/** Hält den Entwurf eines Plans, bis er gespeichert wird. */
class PlanEditorViewModel(val original: WorkoutPlan, val isNew: Boolean) : ViewModel() {

    var plan by mutableStateOf(original)
        private set

    val isDirty: Boolean get() = isNew || plan != original

    val canSave: Boolean get() = plan.name.isNotBlank() && plan.steps.isNotEmpty() && plan.days.isNotEmpty()

    fun update(transform: (WorkoutPlan) -> WorkoutPlan) {
        plan = transform(plan)
    }

    fun addExercise(exercise: Exercise) = update {
        it.copy(
            steps = it.steps + PlanStep(
                id = newId(),
                exerciseId = exercise.id,
                mode = exercise.mode,
                seconds = exercise.seconds,
                reps = exercise.reps,
                sets = 1,
                restSeconds = 15,
            ),
        )
    }

    fun updateStep(step: PlanStep) = update { plan -> plan.copy(steps = plan.steps.map { if (it.id == step.id) step else it }) }

    fun removeStep(stepId: String) = update { plan -> plan.copy(steps = plan.steps.filterNot { it.id == stepId }) }

    fun moveStep(index: Int, delta: Int) = update { plan ->
        val target = index + delta
        if (target !in plan.steps.indices) return@update plan
        val steps = plan.steps.toMutableList()
        steps.add(target, steps.removeAt(index))
        plan.copy(steps = steps)
    }

    companion object {
        fun create(data: AppData, planId: String?, templateKey: String?, today: LocalDate): PlanEditorViewModel {
            data.plan(planId ?: "")?.let { return PlanEditorViewModel(it, isNew = false) }
            val template = templateKey?.let(PlanTemplates::find)
            val plan = template?.toPlan(newId(), today.toEpochDay(), ::newId) ?: WorkoutPlan(
                id = newId(),
                name = "",
                emoji = "🧘",
                color = data.plans.size % 8,
                createdEpochDay = today.toEpochDay(),
            )
            return PlanEditorViewModel(plan, isNew = true)
        }
    }
}
