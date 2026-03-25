package com.example.expensetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.MoodEntry
import com.example.expensetracker.data.Task

@Composable
fun WeeklyReviewScreen(viewModel: MainViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val moods by viewModel.moods.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Weekly Review",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        aiInsights ?: "Generating weekly insights...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            WeeklyStats(expenses, tasks, moods)
        }

        item {
            TrendChartsStub()
        }
    }
}

@Composable
private fun WeeklyStats(expenses: List<Expense>, tasks: List<Task>, moods: List<MoodEntry>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Key Stats", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatCard("Expenses", "$${expenses.sumOf { it.amount }.toInt()}")
                StatCard("Tasks", "${tasks.count { it.isCompleted }}/${tasks.size}")
                StatCard("Moods", "${moods.size}")
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TrendChartsStub() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trends (Charts coming)", fontWeight = FontWeight.Bold)
            // Placeholder for compose charts
            Row {
                Box(modifier = Modifier.weight(1f).height(100.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(100.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)))
            }
        }
    }
}
