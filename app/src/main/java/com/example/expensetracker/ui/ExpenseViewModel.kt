package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.Expense
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor() : ViewModel() {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _monthlyLimit = MutableStateFlow(1000.0)
    val monthlyLimit: StateFlow<Double> = _monthlyLimit.asStateFlow()

    private val _notificationTime = MutableStateFlow("20:00")
    val notificationTime: StateFlow<String> = _notificationTime.asStateFlow()

    private val _aiFeedback = MutableStateFlow<String?>(null)
    val aiFeedback: StateFlow<String?> = _aiFeedback.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Others")

    // IMPORTANT: Replace with your actual Gemini API Key from Google AI Studio
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "YOUR_API_KEY_HERE" 
    )

    fun addExpense(title: String, amount: Double, category: String, date: Long) {
        val newExpense = Expense(title = title, amount = amount, category = category, date = date)
        _expenses.update { it + newExpense }
    }

    fun deleteExpense(expense: Expense) {
        _expenses.update { it - expense }
    }

    fun setMonthlyLimit(limit: Double) {
        _monthlyLimit.value = limit
    }

    fun setNotificationTime(time: String) {
        _notificationTime.value = time
    }

    fun analyzeExpenses() {
        if (_expenses.value.isEmpty()) {
            _aiFeedback.value = "Add some expenses first so I can analyze your spending!"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val expenseData = _expenses.value.joinToString("\n") { 
                    "${it.category}: $${it.amount} on ${it.title}" 
                }
                val prompt = """
                    I am an expense tracker app. Here is the user's spending data:
                    $expenseData
                    
                    Monthly Limit: ${_monthlyLimit.value}
                    Total Spent: ${_expenses.value.sumOf { it.amount }}
                    
                    Please provide a brief, professional, and helpful analysis. 
                    Tell the user:
                    1. What they are doing well.
                    2. Where they are overspending.
                    3. Actionable advice on what they should do next to save money.
                    Keep it under 100 words.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                _aiFeedback.value = response.text
            } catch (e: Exception) {
                _aiFeedback.value = "Error analyzing data: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
    
    fun clearFeedback() {
        _aiFeedback.value = null
    }
}
