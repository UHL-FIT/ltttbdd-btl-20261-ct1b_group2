package com.example.qunlchitiu.ui.ManHinh

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.*
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import com.example.qunlchitiu.viewmodel.CheDoXem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val symbols = (formatter as java.text.DecimalFormat).decimalFormatSymbols
    symbols.currencySymbol = "₫"
    formatter.decimalFormatSymbols = symbols
    return formatter.format(amount)
}

fun formatAmountInput(input: String): String {
    if (input.isEmpty()) return ""
    val clean = input.filter { it.isDigit() }
    return if (clean.isEmpty()) "" else {
        val parsed = clean.toLongOrNull() ?: return ""
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        formatter.format(parsed)
    }
}

@Composable
fun BudgetInputDialog(
    title: String,
    initialValue: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(if (initialValue > 0) initialValue.toLong().toString() else "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = formatAmountInput(textValue),
                onValueChange = { 
                    val clean = it.filter { char -> char.isDigit() }
                    textValue = clean 
                },
                label = { Text("Số tiền") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                suffix = { Text("₫") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(textValue.toDoubleOrNull() ?: 0.0)
            }) { Text("Xác nhận") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhChinh(navController: NavController, viewModel: DieuKhienTaiChinh) {
    val stats by viewModel.stats.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val currentBudget by viewModel.currentBudget.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    
    var showModeDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Thiết lập ngân sách") },
            text = {
                OutlinedTextField(
                    value = formatAmountInput(budgetInput),
                    onValueChange = { 
                        val clean = it.filter { char -> char.isDigit() }
                        budgetInput = clean 
                    },
                    label = { Text("Hạn mức chi tiêu") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("₫") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = budgetInput.toDoubleOrNull() ?: 0.0
                    viewModel.setBudget(amount)
                    showBudgetDialog = false
                    budgetInput = ""
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Hủy") }
            }
        )
    }

    val dateDisplay = remember(selectedDate, viewMode) {
        when (viewMode) {
            CheDoXem.DAY -> SimpleDateFormat("EEE, dd 'thg' MM", Locale("vi")).format(selectedDate.time)
            CheDoXem.WEEK -> {
                val weekStart = (selectedDate.clone() as Calendar).apply { 
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                val weekEnd = (weekStart.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, 6)
                }
                val sdf = SimpleDateFormat("dd/MM", Locale("vi"))
                "${sdf.format(weekStart.time)} - ${sdf.format(weekEnd.time)}"
            }
            CheDoXem.MONTH -> SimpleDateFormat("'thg' MM yyyy", Locale("vi")).format(selectedDate.time)
            CheDoXem.YEAR -> SimpleDateFormat("yyyy", Locale("vi")).format(selectedDate.time)
            CheDoXem.SEVEN_DAYS -> "7 ngày qua"
            CheDoXem.FOURTEEN_DAYS -> "14 ngày qua"
        }
    }

    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = { Text("Chọn chế độ xem") },
            text = {
                Column {
                    ModeOption("Hàng ngày", viewMode == CheDoXem.DAY) { viewModel.setViewMode(CheDoXem.DAY); showModeDialog = false }
                    ModeOption("Hàng tuần", viewMode == CheDoXem.WEEK) { viewModel.setViewMode(CheDoXem.WEEK); showModeDialog = false }
                    ModeOption("Hàng tháng", viewMode == CheDoXem.MONTH) { viewModel.setViewMode(CheDoXem.MONTH); showModeDialog = false }
                    ModeOption("Hàng năm", viewMode == CheDoXem.YEAR) { viewModel.setViewMode(CheDoXem.YEAR); showModeDialog = false }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    TextButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(context, { _, y, m, d ->
                                viewModel.setDate(Calendar.getInstance().apply { set(y, m, d) }.timeInMillis)
                                showModeDialog = false
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Chọn một ngày")
                        }
                    }
                    TextButton(
                        onClick = { viewModel.goToToday(); showModeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Đi đến hôm nay")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        color = Color(0xFF6750A4),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { showModeDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            IconButton(onClick = { viewModel.moveDate(-1) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(dateDisplay, color = Color.White, fontSize = 16.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.moveDate(1) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFFD4AF37))
                    }
                }
            )
        },
        bottomBar = {
            BottomActionButtons(navController)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFBF8FF)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                BalanceHeader(stats.balance, expenses.size)
                IncomeExpenseCards(
                    income = stats.totalIncome,
                    expense = stats.totalExpense,
                    onIncomeClick = { navController.navigate("stats?isIncome=true") },
                    onExpenseClick = { navController.navigate("stats?isIncome=false") }
                )
                QuickActionCards(
                    budgetLimit = currentBudget?.limitAmount ?: 0.0,
                    currentExpense = stats.totalExpense,
                    savingsGoalsCount = savingsGoals.size,
                    remindersCount = reminders.filter { !it.isPaid }.size,
                    onBudgetClick = {
                        navController.navigate("budget_overview")
                    },
                    onSavingsClick = {
                        navController.navigate("savings_goals")
                    },
                    onRemindersClick = {
                        navController.navigate("reminders")
                    }
                )
                SectionHeader("Danh mục", "Xem phân tích") { navController.navigate("stats") }
                CategoryGrid(stats.expenseCategorySummaries, allCategories) { category ->
                    navController.navigate("transactions?category=$category")
                }
                SectionHeader("Giao dịch", "Xem tất cả") {
                    navController.navigate("transactions")
                }
                if (expenses.isEmpty()) {
                    EmptyTransactions()
                }
            }
            items(expenses) { expense ->
                val icon = allCategories.find { it.name == expense.category }?.icon ?: "📦"
                ExpenseItem(
                    expense = expense, 
                    icon = icon,
                    onClick = {
                        navController.navigate("add_expense?isIncome=${expense.isIncome}&expenseId=${expense.id}")
                    },
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
fun ModeOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(text, color = if (isSelected) Color(0xFF6750A4) else Color.Black)
    }
}

@Composable
fun BalanceHeader(balance: Double, count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = (if (balance >= 0) "+" else "") + formatCurrency(balance),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
        )
        Text(
            text = "$count giao dịch",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun IncomeExpenseCards(income: Double, expense: Double, onIncomeClick: () -> Unit, onExpenseClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            title = "Thu nhập",
            amount = income,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            isIncome = true,
            onClick = onIncomeClick
        )
        SummaryCard(
            title = "Chi tiêu",
            amount = expense,
            color = Color(0xFFE91E63),
            modifier = Modifier.weight(1f),
            isIncome = false,
            onClick = onExpenseClick
        )
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, modifier: Modifier, isIncome: Boolean, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(title, color = Color.Gray, fontSize = 14.sp)
            }
            Text(formatCurrency(amount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun QuickActionCards(
    budgetLimit: Double,
    currentExpense: Double,
    savingsGoalsCount: Int,
    remindersCount: Int,
    onBudgetClick: () -> Unit,
    onSavingsClick: () -> Unit,
    onRemindersClick: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val progress = if (budgetLimit > 0) (currentExpense / budgetLimit).coerceIn(0.0, 1.0) else 0.0
            val percent = (progress * 100).toInt()
            ActionCard(
                title = "Ngân sách",
                value = if (budgetLimit > 0) "$percent%" else "Chưa đặt",
                bgColor = Color(0xFF81C784),
                icon = Icons.Default.PieChart,
                onClick = onBudgetClick
            )
        }
        item { 
            ActionCard(
                title = "Tiết kiệm", 
                value = savingsGoalsCount.toString(), 
                bgColor = Color(0xFF64B5F6), 
                icon = Icons.Default.Savings, 
                onClick = onSavingsClick
            ) 
        }
        item { 
            ActionCard(
                title = "Nhắc nhở", 
                value = remindersCount.toString(),
                bgColor = Color(0xFFCE93D8), 
                icon = Icons.Default.NotificationsActive, 
                onClick = onRemindersClick
            ) 
        }
    }
}

@Composable
fun ActionCard(title: String, value: String, bgColor: Color, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp, 160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(bgColor, bgColor.copy(alpha = 0.7f))))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(
                color = Color.Black.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(6.dp), tint = Color.Black.copy(alpha = 0.6f))
            }
            Column {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(title, fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        TextButton(onClick = onActionClick) {
            Text("$action >", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CategoryGrid(summaries: Map<String, Double>, allCategories: List<DanhMuc>, onCategoryClick: (String) -> Unit) {
    val displayedCategories = allCategories.filter { !it.isIncome }.take(6)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(displayedCategories) { cat ->
            val amount = summaries[cat.name] ?: 0.0
            CategoryItem(cat.name, amount, cat.icon, Color(0xFFF5F5F5)) { onCategoryClick(cat.name) }
        }
    }
}

@Composable
fun CategoryItem(name: String, amount: Double, emoji: String, bgColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(110.dp, 140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 14.sp, color = Color.DarkGray)
            Text(
                text = if (amount >= 1000) "VND ${(amount/1000).toInt()}K" else "VND ${amount.toInt()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyTransactions() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Text("Chưa có giao dịch nào", color = Color.LightGray)
    }
}

@Composable
fun ExpenseItem(expense: GiaoDich, icon: String, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val isToday = remember(expense.date) {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = expense.date }
        today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    val dateDisplay = if (isToday) "Hôm nay " + timeFormatter.format(Date(expense.date)) 
                      else dateFormatter.format(Date(expense.date))

    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Surface(
                color = if (expense.isIncome) Color(0xFFE8F5E9) else Color(0xFFFCE4EC),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 20.sp)
                }
            }
        },
        headlineContent = { Text(expense.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(expense.note.ifEmpty { expense.category }, color = Color.Gray, fontSize = 12.sp) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (expense.isIncome) "+" else "-") + formatCurrency(expense.amount),
                        color = if (expense.isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = dateDisplay,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}

@Composable
fun BottomActionButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { navController.navigate("add_expense?isIncome=true&expenseId=-1") },
            modifier = Modifier.weight(1f).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E4B8B)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Thêm thu nhập", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = { navController.navigate("add_expense?isIncome=false&expenseId=-1") },
            modifier = Modifier.weight(1f).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E4B8B)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Thêm chi tiêu", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
