package com.example.expensetracker.data

import androidx.room.Entity
import java.util.UUID

enum class Emotion { HAPPY, SAD, STRESSED, TIRED, ENERGETIC, CALM, ANGRY }

@Entity(tableName = "mood_entries", primaryKeys = ["id"])
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emotion: Emotion,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    var productivityImpact: Float = 0f  // Derived -1 to 1 based on task corr
)
