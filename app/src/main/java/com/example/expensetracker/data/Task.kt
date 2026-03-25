package com.example.expensetracker.data

import androidx.room.Entity
import java.util.UUID

enum class Priority { LOW, MEDIUM, HIGH }

@Entity(tableName = "tasks", primaryKeys = ["id"])
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val streakDays: Int = 0,
    val habitStrength: Float = 0f
)
