package com.example.expensetracker.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.Expense
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val monthlyLimit by viewModel.monthlyLimit.collectAsState()
    val notificationTime by viewModel.notificationTime.collectAsState()
    val aiFeedback by viewModel.aiFeedback.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Expense Tracker", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EE)
                ),
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF03DAC6),
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ExpenseSummary(expenses, monthlyLimit)
                
                AiAnalysisCard(
                    feedback = aiFeedback,
                    isAnalyzing = isAnalyzing,
                    onAnalyzeClick = { viewModel.analyzeExpenses() },
                    onClearClick = { viewModel.clearFeedback() }
                )

                if (expenses.isNotEmpty()) {
                    MonthlyChart(expenses, viewModel.categories)
                }
                
                Text(
                    "Recent Expenses",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                expenses.reversed().forEach { expense ->
                    ExpenseItem(expense, onDelete = { viewModel.deleteExpense(it) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddDialog) {
            AddExpenseDialog(
                categories = viewModel.categories,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, amount, category, date ->
                    viewModel.addExpense(title, amount, category, date)
                    showAddDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentLimit = monthlyLimit,
                currentTime = notificationTime,
                onDismiss = { showSettingsDialog = false },
                onSave = { limit, time ->
                    viewModel.setMonthlyLimit(limit)
                    viewModel.setNotificationTime(time)
                    showSettingsDialog = false
                }
            )
        }
    }
}

@Composable
fun AiAnalysisCard(
    feedback: String?,
    isAnalyzing: Boolean,
    onAnalyzeClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF6200EE),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI Financial Advisor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (feedback != null) {
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (feedback != null) {
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    "Get personalized insights and saving tips based on your spending habits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isAnalyzing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Analyze Spending Patterns")
                }
            }
        }
    }
}

@Composable
fun MonthlyChart(expenses: List<Expense>, categories: List<String>) {
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { expense -> expense.amount } }
    
    val total = categoryTotals.values.sum()
    val colors = listOf(
        Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFF607D8B)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = 0f
                categoryTotals.forEach { (category, amount) ->
                    val index = categories.indexOf(category).let { if (it == -1) categories.size else it }
                    val sweepAngle = (amount.toFloat() / total.toFloat()) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                categoryTotals.keys.forEach { category ->
                    val index = categories.indexOf(category).let { if (it == -1) categories.size else it }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(colors[index % colors.size]))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseSummary(expenses: List<Expense>, limit: Double) {
    val calendar = Calendar.getInstance()
    val currentMonthTotal = expenses.filter {
        val expCal = Calendar.getInstance().apply { timeInMillis = it.date }
        expCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
        expCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
    }.sumOf { it.amount }
    
    val progress = if (limit > 0) (currentMonthTotal / limit).toFloat().coerceIn(0f, 1f) else 0f
    val color = if (currentMonthTotal > limit) Color(0xFFF44336) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Monthly Spending", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$${"%.2f".format(Locale.getDefault(), currentMonthTotal)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ $${"%.0f".format(Locale.getDefault(), limit)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = color,
                trackColor = Color(0xFFEEEEEE)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (currentMonthTotal > limit) "You've exceeded your limit!" else "${(progress * 100).toInt()}% of monthly limit used",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentMonthTotal > limit) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: (Expense) -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val colors = listOf(
        Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFF607D8B)
    )
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Others")
    val categoryIndex = categories.indexOf(expense.category)
    val categoryColor = if (categoryIndex != -1) colors[categoryIndex] else Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(expense.title, fontWeight = FontWeight.Bold) },
            supportingContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(categoryColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${expense.category} • ${dateFormat.format(Date(expense.date))}")
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${"%.2f".format(Locale.getDefault(), expense.amount)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { onDelete(expense) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun AddExpenseDialog(
    categories: List<String>,
    onDismiss: () -> Unit, 
    onConfirm: (String, Double, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What did you spend on?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Category", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(selectedCategory)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Date: ${dateFormat.format(Date(selectedDate))}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(title, amountDouble, selectedCategory, selectedDate)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SettingsDialog(
    currentLimit: Double,
    currentTime: String,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var limit by remember { mutableStateOf(currentLimit.toString()) }
    var time by remember { mutableStateOf(currentTime) }

    val context = LocalContext.current
    val timeParts = time.split(":")
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        timeParts[0].toInt(),
        timeParts[1].toInt(),
        true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Monthly Budget ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Notification Reminder", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Time: $time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(limit.toDoubleOrNull() ?: currentLimit, time)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
