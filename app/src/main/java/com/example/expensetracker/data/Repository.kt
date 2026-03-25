package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class Repository(
    private val taskDao: TaskDao,
    private val expenseDao: ExpenseDao,
    private val moodDao: MoodDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = taskDao.getPendingTasks()
    val activeStreakCount: Flow<Int> = taskDao.getActiveStreakCount()
    val averageHabitStrength: Flow<Double?> = taskDao.getAverageHabitStrength()

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val averageRisk: Flow<Double?> = expenseDao.getAverageRisk()

    val allMoods: Flow<List<MoodEntry>> = moodDao.getAllMoods()
    val averageProductivityImpact: Flow<Double?> = moodDao.getAverageProductivityImpact()
    val lowMoodDays: Flow<Int> = moodDao.getLowMoodDays()

    // Dynamic insights combine
    val dashboardInsights = combine(
        activeStreakCount,
        averageHabitStrength,
        averageRisk,
        averageProductivityImpact
    ) { streak, habit, risk, prod ->
        listOf(streak, habit ?: 0.0, risk ?: 0.0, prod ?: 0.0)
    }

    // Analytics methods
    suspend fun addTask(task: Task) = taskDao.insertTask(task)
    suspend fun completeTask(task: Task) {
        val updated = task.copy(isCompleted = true, streakDays = if (task.streakDays > 0) task.streakDays + 1 else 1)
        taskDao.updateTask(updated)
    }
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    suspend fun addMood(mood: MoodEntry) = moodDao.insertMood(mood)
    suspend fun updateMood(mood: MoodEntry) = moodDao.updateMood(mood)
    suspend fun deleteMood(mood: MoodEntry) = moodDao.deleteMood(mood)

    suspend fun clearAll() {
        taskDao.clearAll()
        expenseDao.clearAll()
        moodDao.clearAll()
    }
}

