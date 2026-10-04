package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseType: String,
    val exerciseName: String,
    val durationMinutes: Int,
    val intensity: String,
    val caloriesBurned: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val userWeightKgAtLog: Float = 70f
)
