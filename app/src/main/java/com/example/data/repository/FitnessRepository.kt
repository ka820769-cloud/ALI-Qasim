package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.DailySnapshotDao
import com.example.data.local.DailySnapshotEntity
import com.example.data.local.WorkoutDao
import com.example.data.local.WorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FitnessRepository(
    private val workoutDao: WorkoutDao,
    private val dailySnapshotDao: DailySnapshotDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pulsesync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_WEIGHT = "pref_user_weight_kg"
        private const val KEY_CALORIE_GOAL = "pref_daily_calorie_goal"
        private const val KEY_DURATION_GOAL = "pref_daily_duration_goal"
        private const val KEY_STEP_GOAL = "pref_daily_step_goal"
        private const val DEFAULT_WEIGHT_KG = 70f
        private const val DEFAULT_CALORIE_GOAL = 650
        private const val DEFAULT_DURATION_GOAL = 45
        private const val DEFAULT_STEP_GOAL = 10000

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
        .flowOn(Dispatchers.IO)

    val workoutCount: Flow<Int> = workoutDao.getWorkoutCount()
        .flowOn(Dispatchers.IO)

    fun getTodaySnapshot(): Flow<DailySnapshotEntity?> {
        return dailySnapshotDao.getSnapshotForDate(getTodayDateString())
            .flowOn(Dispatchers.IO)
    }

    val recentSnapshots: Flow<List<DailySnapshotEntity>> = dailySnapshotDao.getRecentSnapshots()
        .flowOn(Dispatchers.IO)

    fun getUserWeightKg(): Float {
        return prefs.getFloat(KEY_USER_WEIGHT, DEFAULT_WEIGHT_KG)
    }

    fun setUserWeightKg(weightKg: Float) {
        prefs.edit().putFloat(KEY_USER_WEIGHT, weightKg.coerceIn(30f, 250f)).apply()
    }

    fun getDailyCalorieGoal(): Int {
        return prefs.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)
    }

    fun setDailyCalorieGoal(goal: Int) {
        prefs.edit().putInt(KEY_CALORIE_GOAL, goal.coerceIn(100, 5000)).apply()
    }

    fun getDailyDurationGoal(): Int {
        return prefs.getInt(KEY_DURATION_GOAL, DEFAULT_DURATION_GOAL)
    }

    fun setDailyDurationGoal(minutes: Int) {
        prefs.edit().putInt(KEY_DURATION_GOAL, minutes.coerceIn(10, 360)).apply()
    }

    fun getDailyStepGoal(): Int {
        return prefs.getInt(KEY_STEP_GOAL, DEFAULT_STEP_GOAL)
    }

    fun setDailyStepGoal(goal: Int) {
        prefs.edit().putInt(KEY_STEP_GOAL, goal.coerceIn(1000, 50000)).apply()
    }

    suspend fun logWorkout(workout: WorkoutEntity): Long = withContext(Dispatchers.IO) {
        val id = workoutDao.insertWorkout(workout)
        val todayStr = getTodayDateString()
        var snapshot = dailySnapshotDao.getSnapshotForDateDirect(todayStr)
        if (snapshot == null) {
            snapshot = DailySnapshotEntity(
                dateString = todayStr,
                totalCaloriesBurned = 350 + workout.caloriesBurned,
                loggedWorkoutCalories = workout.caloriesBurned,
                activeSteps = 5400,
                activeMinutes = workout.durationMinutes,
                avgHeartRate = workout.avgHeartRate ?: 74,
                restingHeartRate = 62,
                lastUpdated = System.currentTimeMillis()
            )
            dailySnapshotDao.insertOrUpdateSnapshot(snapshot)
        } else {
            val updated = snapshot.copy(
                totalCaloriesBurned = snapshot.totalCaloriesBurned + workout.caloriesBurned,
                loggedWorkoutCalories = snapshot.loggedWorkoutCalories + workout.caloriesBurned,
                activeMinutes = snapshot.activeMinutes + workout.durationMinutes,
                lastUpdated = System.currentTimeMillis()
            )
            dailySnapshotDao.insertOrUpdateSnapshot(updated)
        }
        id
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) = withContext(Dispatchers.IO) {
        workoutDao.deleteWorkout(workout)
        val todayStr = getTodayDateString()
        val snapshot = dailySnapshotDao.getSnapshotForDateDirect(todayStr)
        if (snapshot != null) {
            val newLoggedCal = (snapshot.loggedWorkoutCalories - workout.caloriesBurned).coerceAtLeast(0)
            val newTotalCal = (snapshot.totalCaloriesBurned - workout.caloriesBurned).coerceAtLeast(0)
            val newActiveMin = (snapshot.activeMinutes - workout.durationMinutes).coerceAtLeast(0)
            dailySnapshotDao.insertOrUpdateSnapshot(
                snapshot.copy(
                    loggedWorkoutCalories = newLoggedCal,
                    totalCaloriesBurned = newTotalCal,
                    activeMinutes = newActiveMin,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
        val todayStr = getTodayDateString()
        val existing = dailySnapshotDao.getSnapshotForDateDirect(todayStr)
        if (existing == null) {
            val snapshot = DailySnapshotEntity(
                dateString = todayStr,
                totalCaloriesBurned = 380,
                loggedWorkoutCalories = 0,
                activeSteps = 6120,
                activeMinutes = 32,
                avgHeartRate = 72,
                restingHeartRate = 61,
                spO2Avg = 98,
                sleepHours = 7.4f,
                lastUpdated = System.currentTimeMillis()
            )
            dailySnapshotDao.insertOrUpdateSnapshot(snapshot)
        }

        val totalWorkoutCal = workoutDao.getTotalCaloriesBetween(0, Long.MAX_VALUE)
        if (totalWorkoutCal == 0) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24L * 60L * 60L * 1000L

            workoutDao.insertWorkout(
                WorkoutEntity(
                    exerciseType = "RUNNING",
                    exerciseName = "Outdoor Run",
                    durationMinutes = 40,
                    intensity = "HIGH",
                    caloriesBurned = 430,
                    notes = "Interval sprints around park loop",
                    timestamp = now - (oneDayMs * 1),
                    avgHeartRate = 152,
                    maxHeartRate = 175
                )
            )
            workoutDao.insertWorkout(
                WorkoutEntity(
                    exerciseType = "CYCLING",
                    exerciseName = "Road Cycling",
                    durationMinutes = 55,
                    intensity = "MODERATE",
                    caloriesBurned = 385,
                    notes = "Scenic ridge route with light headwinds",
                    timestamp = now - (oneDayMs * 2),
                    avgHeartRate = 134,
                    maxHeartRate = 158
                )
            )
            workoutDao.insertWorkout(
                WorkoutEntity(
                    exerciseType = "WEIGHTLIFTING",
                    exerciseName = "Upper Body Strength",
                    durationMinutes = 45,
                    intensity = "MODERATE",
                    caloriesBurned = 236,
                    notes = "Bench press 4x8, dumbbell rows, shoulder press",
                    timestamp = now - (oneDayMs * 4),
                    avgHeartRate = 118,
                    maxHeartRate = 142
                )
            )
            workoutDao.insertWorkout(
                WorkoutEntity(
                    exerciseType = "HIIT",
                    exerciseName = "HIIT Circuit",
                    durationMinutes = 30,
                    intensity = "MAXIMUM",
                    caloriesBurned = 357,
                    notes = "Tabata burpees, kettlebell swings, box jumps",
                    timestamp = now - (oneDayMs * 5),
                    avgHeartRate = 162,
                    maxHeartRate = 184
                )
            )
            workoutDao.insertWorkout(
                WorkoutEntity(
                    exerciseType = "YOGA",
                    exerciseName = "Vinyasa Flow",
                    durationMinutes = 35,
                    intensity = "LOW",
                    caloriesBurned = 110,
                    notes = "Morning mobility & deep stretch",
                    timestamp = now - (oneDayMs * 6),
                    avgHeartRate = 92,
                    maxHeartRate = 108
                )
            )
        }
    }
}
