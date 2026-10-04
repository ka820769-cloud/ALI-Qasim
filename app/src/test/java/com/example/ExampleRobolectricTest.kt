package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CalorieCalculator
import com.example.data.model.ExerciseType
import com.example.data.model.WorkoutIntensity
import com.example.data.repository.FitnessRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PulseSync", appName)
    }

    @Test
    fun `test calorie calculation formula accuracy`() {
        // Running 30 mins at moderate intensity for 70kg person
        // 9.8 MET * 3.5 * 70 / 200 * 30 * 1.0 = ~360 kcal
        val runningCalories = CalorieCalculator.calculateCalories(
            exerciseType = ExerciseType.RUNNING,
            durationMinutes = 30,
            intensity = WorkoutIntensity.MODERATE,
            userWeightKg = 70f
        )
        assertTrue("Running calories should be around 360", runningCalories in 340..380)

        // Yoga 60 mins at low intensity
        val yogaCalories = CalorieCalculator.calculateCalories(
            exerciseType = ExerciseType.YOGA,
            durationMinutes = 60,
            intensity = WorkoutIntensity.LOW,
            userWeightKg = 70f
        )
        assertTrue("Yoga calories should be positive and realistic", yogaCalories in 120..220)
    }

    @Test
    fun `test set and save daily calorie and duration goals`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repo = FitnessRepository(db.workoutDao(), db.dailySnapshotDao(), context)

        // Set and save customized goals
        repo.setDailyCalorieGoal(750)
        repo.setDailyDurationGoal(60)

        // Verify saved goals persist
        assertEquals(750, repo.getDailyCalorieGoal())
        assertEquals(60, repo.getDailyDurationGoal())

        // Test progress bar calculations
        val currentCalories = 450
        val targetCalories = repo.getDailyCalorieGoal()
        val calorieProgressRatio = (currentCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.6f, calorieProgressRatio, 0.001f)

        val currentDuration = 45
        val targetDuration = repo.getDailyDurationGoal()
        val durationProgressRatio = (currentDuration.toFloat() / targetDuration.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.75f, durationProgressRatio, 0.001f)
    }
}
