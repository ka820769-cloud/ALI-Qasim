package com.example.data.model

import kotlin.math.roundToInt

object CalorieCalculator {

    /**
     * Standard ACSM (American College of Sports Medicine) Metabolic Equivalent formula:
     * Calories (kcal) = MET * 3.5 * weightKg / 200 * durationMinutes * intensityMultiplier
     */
    fun calculateCalories(
        exerciseType: ExerciseType,
        durationMinutes: Int,
        intensity: WorkoutIntensity,
        userWeightKg: Float = 70f
    ): Int {
        if (durationMinutes <= 0) return 0
        val effectiveMet = exerciseType.baseMet * intensity.multiplier
        val calories = (effectiveMet * 3.5f * userWeightKg / 200f) * durationMinutes
        return calories.roundToInt().coerceAtLeast(1)
    }

    /**
     * Calorie burn rate in kcal/min
     */
    fun calculateBurnRate(
        exerciseType: ExerciseType,
        intensity: WorkoutIntensity,
        userWeightKg: Float = 70f
    ): Float {
        val effectiveMet = exerciseType.baseMet * intensity.multiplier
        return (effectiveMet * 3.5f * userWeightKg / 200f)
    }
}
