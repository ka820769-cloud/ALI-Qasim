package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_snapshots")
data class DailySnapshotEntity(
    @PrimaryKey
    val dateString: String, // Format: YYYY-MM-DD
    val totalCaloriesBurned: Int = 0,
    val loggedWorkoutCalories: Int = 0,
    val activeSteps: Int = 0,
    val activeMinutes: Int = 0,
    val avgHeartRate: Int = 72,
    val restingHeartRate: Int = 62,
    val spO2Avg: Int = 98,
    val sleepHours: Float = 7.5f,
    val lastUpdated: Long = System.currentTimeMillis()
)
