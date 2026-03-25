package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsEngine @Inject constructor(private val repository: Repository) {
    // Mood-Task correlation: avg complete rate on low/high mood days
    val moodProductivityCorrelation: Flow<Float> = repository.averageProductivityImpact.map { impact ->
        (impact?.toFloat() ?: 0f) * 100  // Simple scaling, extend w/ joins
    }

    // Overspend prediction based on recent trends
    suspend fun predictOverspend(risk: Float, recentExpenses: List<Expense>): Float {
        val weeklyTrend = recentExpenses
            .filter { Calendar.getInstance().timeInMillis - it.date < 7*24*60*60*1000L }
            .sumOf { it.amount }
        val avgRisk = repository.averageRisk.first() ?: 1.0
        return (risk + (weeklyTrend / avgRisk).toFloat() / 10f).coerceIn(0f, 1f)
    }

    // Optimal task time based on mood/productivity
    fun optimalTaskTime(moods: List<MoodEntry>): Long {
        // Pseudo: high productivity mornings
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
        }.timeInMillis
    }

    // Habit improvement suggestion
    fun habitSuggestion(streak: Int, strength: Float): String {
        return when {
            streak > 7 -> "Great streak! Add harder tasks."
            strength < 0.3f -> "Try daily reminders for consistency."
            else -> "Keep going!"
        }
    }
}
