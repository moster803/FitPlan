package de.fitplan.app.core.logic

import de.fitplan.app.core.library.ExerciseLibrary
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.Exercise
import de.fitplan.app.core.model.WorkoutPlan
import de.fitplan.app.core.workout.estimatedSeconds

fun AppData.findExercise(id: String): Exercise? = ExerciseLibrary.find(id) ?: customExercises.firstOrNull { it.id == id }

fun AppData.allExercises(): List<Exercise> = ExerciseLibrary.all + customExercises

fun AppData.estimatedSeconds(plan: WorkoutPlan): Int = estimatedSeconds(plan.steps, ::findExercise)

fun AppData.estimatedMinutes(plan: WorkoutPlan): Int = (estimatedSeconds(plan) + 59) / 60
