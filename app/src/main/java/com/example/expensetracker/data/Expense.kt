package com.example.expensetracker.data

import java.util.UUID

import androidx.room.Entity

@Entity(tableName = "expenses", primaryKeys = ["id"])
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val category: String,
    var predictedRisk: Float = 0f  // 0-1 overspend risk score
)
