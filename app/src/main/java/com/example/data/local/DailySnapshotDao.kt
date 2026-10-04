package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySnapshotDao {

    @Query("SELECT * FROM daily_snapshots WHERE dateString = :dateString LIMIT 1")
    fun getSnapshotForDate(dateString: String): Flow<DailySnapshotEntity?>

    @Query("SELECT * FROM daily_snapshots WHERE dateString = :dateString LIMIT 1")
    suspend fun getSnapshotForDateDirect(dateString: String): DailySnapshotEntity?

    @Query("SELECT * FROM daily_snapshots ORDER BY dateString DESC LIMIT 7")
    fun getRecentSnapshots(): Flow<List<DailySnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSnapshot(snapshot: DailySnapshotEntity)

    @Query("UPDATE daily_snapshots SET loggedWorkoutCalories = loggedWorkoutCalories + :calories, totalCaloriesBurned = totalCaloriesBurned + :calories WHERE dateString = :dateString")
    suspend fun addCaloriesToDate(dateString: String, calories: Int)
}
