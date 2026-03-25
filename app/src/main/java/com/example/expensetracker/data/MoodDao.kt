package com.example.expensetracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Query("SELECT AVG(productivityImpact) FROM mood_entries")
    fun getAverageProductivityImpact(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM mood_entries WHERE productivityImpact < 0")
    fun getLowMoodDays(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry)

    @Update
    suspend fun updateMood(mood: MoodEntry)

    @Delete
    suspend fun deleteMood(mood: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun clearAll()
}

