package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getWorkoutsBetween(startTime: Long, endTime: Long): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM workouts WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalCaloriesBetween(startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(*) FROM workouts")
    fun getWorkoutCount(): Flow<Int>
}
