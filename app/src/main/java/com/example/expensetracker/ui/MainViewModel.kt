package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.*
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    // Data States from Repository
    val expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val moods = repository.allMoods.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val streaks = repository.activeStreakCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI & Logic States
    private val _aiInsights = MutableStateFlow<String?>(null)
    val aiInsights = _aiInsights.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _userScore = MutableStateFlow(0)
    val userScore = _userScore.asStateFlow()

    private val _monthlyLimit = MutableStateFlow(1000.0)
    val monthlyLimit = _monthlyLimit.asStateFlow()

    // AI Configuration (Replace API Key)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "YOUR_API_KEY_HERE"
    )

    // Dynamic Dashboard Priority Logic
    val dashboardOrder = combine(expenses, tasks, moods) { expenses, tasks, moods ->
        val scores = mutableMapOf(
            "finance" to expenses.size * 0.5,
            "tasks" to tasks.count { !it.isCompleted } * 1.2,
            "mood" to if (moods.lastOrNull()?.emotion == Emotion.SAD) 2.0 else 0.5
        )
        scores.entries.sortedByDescending { it.value }.map { it.key }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("tasks", "finance", "mood"))

    // Actions
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            updateScore(10)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun autoSuggestTasks() {
        viewModelScope.launch {
            val suggestedTasks = listOf(
                Task(title = "Morning Meditation", dueDate = System.currentTimeMillis()),
                Task(title = "Check Budget", dueDate = System.currentTimeMillis()),
                Task(title = "Hydrate", dueDate = System.currentTimeMillis())
            )
            suggestedTasks.forEach { repository.addTask(it) }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == taskId }
            if (task != null) {
                repository.completeTask(task)
                updateScore(50)
            }
        }
    }

    fun addMood(mood: MoodEntry) {
        viewModelScope.launch {
            repository.addMood(mood)
            updateScore(5)
        }
    }

    private fun updateScore(points: Int) {
        _userScore.update { it + points }
    }

    fun generateSmartInsights() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val dataContext = """
                    Expenses: ${expenses.value.takeLast(5).joinToString { "${it.category}: $${it.amount}" }}
                    Tasks: ${tasks.value.count { it.isCompleted }}/${tasks.value.size} completed.
                    Recent Mood: ${moods.value.lastOrNull()?.emotion ?: "None"}
                """.trimIndent()

                val prompt = "Analyze this user data and provide 3 smart, actionable lifestyle & financial tips: $dataContext"
                val response = generativeModel.generateContent(prompt)
                _aiInsights.value = response.text
            } catch (e: Exception) {
                _aiInsights.value = "AI Insight: You're doing great! Keep tracking to see patterns."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
