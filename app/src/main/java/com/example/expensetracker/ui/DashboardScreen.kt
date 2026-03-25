package com.example.expensetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val order by viewModel.dashboardOrder.collectAsState()
    val score by viewModel.userScore.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var darkTheme by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart Life Assistant", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6200EE)),
                actions = {
                    IconButton(onClick = { darkTheme = !darkTheme }) {
                        Icon(
                            if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Color.White
                        )
                    }
                    Box(modifier = Modifier.padding(end = 16.dp).clip(CircleShape).background(Color(0xFF03DAC6)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("LVL ${score / 100} • $score XP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.autoSuggestTasks() },
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SmartToy, contentDescription = "Smart Suggest")
                    Text("Suggest", fontSize = 10.sp)
                }
            }
        }
    ) { padding -> 
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            item {
                AiInsightCard(aiInsights, isAnalyzing) {
                    viewModel.generateSmartInsights()
                }
            }
            
            items(order) { section ->
                when (section) {
                    "tasks" -> TaskSection(viewModel)
                    "finance" -> FinanceSection(viewModel)
                    "mood" -> MoodSection(viewModel)
                }
            }
        }
    }
}

@Composable
fun AiInsightCard(insight: String?, isLoading: Boolean, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF6200EE))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Smart Insights", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(insight ?: "Analyzing your routines to help you improve...", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh, enabled = !isLoading, shape = RoundedCornerShape(12.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Refresh Analysis")
                }
            }
        }
    }
}

@Composable
fun TaskSection(viewModel: MainViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val streaks by viewModel.streaks.collectAsState()
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Daily Routine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text("${streaks} day streak") },
                    leadingIcon = { Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize), tint = Color(0xFFFF5722)) }
                )
            }
            tasks.take(3).forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.completeTask(task.id) })
                    Text(task.title, textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                }
            }
            if (tasks.isEmpty()) {
                Text("No tasks for today. Add some to stay productive!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun FinanceSection(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val limit by viewModel.monthlyLimit.collectAsState()
    val total = expenses.sumOf { it.amount }
    
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Expenses", fontWeight = FontWeight.Bold)
            val progress = if (limit > 0) (total / limit).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Text("$${total.toInt()} / $${limit.toInt()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MoodSection(viewModel: MainViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mood Tracker", fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf(Emotion.HAPPY, Emotion.SAD, Emotion.STRESSED, Emotion.TIRED, Emotion.CALM).forEach { emotion ->
                    IconButton(onClick = { viewModel.addMood(MoodEntry(emotion = emotion)) }) {
                        Text(when(emotion) {
                            Emotion.HAPPY -> "😊"
                            Emotion.SAD -> "😢"
                            Emotion.STRESSED -> "😫"
                            Emotion.TIRED -> "😴"
                            else -> "😐"
                        })
                    }
                }
            }
        }
    }
}
