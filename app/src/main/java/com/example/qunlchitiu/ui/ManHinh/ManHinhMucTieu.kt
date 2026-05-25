package com.example.qunlchitiu.ui.ManHinh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.MucTieuTietKiem
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhMucTieu(navController: NavController, viewModel: DieuKhienTaiChinh) {
    val goals by viewModel.savingsGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu tiết kiệm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00796B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = Color(0xFF00796B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        if (showAddGoalDialog) {
            AddGoalDialog(
                onDismiss = { showAddGoalDialog = false },
                onConfirm = { name, target, icon, deadline ->
                    viewModel.addSavingsGoal(name, target, icon, deadline)
                    showAddGoalDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có mục tiêu nào", color = Color.Gray)
                        TextButton(onClick = { showAddGoalDialog = true }) {
                            Text("Thêm ngay", color = Color(0xFF00796B))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(goals) { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            onAddMoney = { amount -> viewModel.addToSavings(goal.id, amount) },
                            onDelete = { viewModel.deleteSavingsGoal(goal) },
                            onEdit = { name, target, icon, deadline ->
                                viewModel.updateSavingsGoal(goal.copy(name = name, targetAmount = target, icon = icon, targetDate = deadline))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsGoalCard(goal: MucTieuTietKiem, onAddMoney: (Double) -> Unit, onDelete: () -> Unit, onEdit: (String, Double, String, Long) -> Unit) {
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    val percent = (progress * 100).toInt()
    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    
    // Calculate days remaining
    val currentTime = System.currentTimeMillis()
    val diffInMillis = goal.targetDate - currentTime
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis).coerceAtLeast(0)
    
    // Calculate suggested monthly savings
    val monthsRemaining = (daysRemaining / 30.0).coerceAtLeast(1.0)
    val monthlySuggestion = remainingAmount / monthsRemaining

    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isDeposit by remember { mutableStateOf(true) }

    if (showAddMoneyDialog) {
        BudgetInputDialog(
            title = if (isDeposit) "Nạp tiền: ${goal.name}" else "Rút tiền: ${goal.name}",
            initialValue = 0.0,
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { 
                onAddMoney(if (isDeposit) it else -it)
                showAddMoneyDialog = false
            }
        )
    }

    if (showEditDialog) {
        AddGoalDialog(
            title = "Chỉnh sửa mục tiêu",
            initialName = goal.name,
            initialTarget = goal.targetAmount,
            initialIcon = goal.icon,
            initialDays = daysRemaining.toString(),
            onDismiss = { showEditDialog = false },
            onConfirm = { name, target, icon, deadline ->
                onEdit(name, target, icon, deadline)
                showEditDialog = false
            },
            onDelete = {
                onDelete()
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(goal.icon, fontSize = 28.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF004D40))
                    Text(
                        "Còn $daysRemaining ngày • Hạn: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(goal.targetDate))}", 
                        color = Color.Gray, 
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Progress Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = Color(0xFF00796B),
                    trackColor = Color(0xFFEEEEEE)
                )
                Spacer(Modifier.width(12.dp))
                Text("$percent%", fontWeight = FontWeight.Bold, color = Color(0xFF00796B))
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Summary Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Đã có", formatCurrency(goal.currentAmount), Color(0xFF2E7D32))
                StatItem("Mục tiêu", formatCurrency(goal.targetAmount), Color.Black)
                StatItem("Còn lại", formatCurrency(remainingAmount), Color.Red)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
            
            // Suggestion & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tiết kiệm định kỳ", fontSize = 11.sp, color = Color.Gray)
                    Text("${formatCurrency(monthlySuggestion)}/tháng", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00796B))
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Withdraw Button
                    IconButton(
                        onClick = { 
                            isDeposit = false
                            showAddMoneyDialog = true 
                        },
                        modifier = Modifier.background(Color(0xFFFBE9E7), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Withdraw", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                    
                    // Deposit Button
                    IconButton(
                        onClick = { 
                            isDeposit = true
                            showAddMoneyDialog = true 
                        },
                        modifier = Modifier.background(Color(0xFFE0F2F1), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Deposit", tint = Color(0xFF00796B), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = valueColor, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    title: String = "Mục tiêu tiết kiệm mới",
    initialName: String = "",
    initialTarget: Double = 0.0,
    initialIcon: String = "💰",
    initialDays: String = "30",
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var target by remember { mutableStateOf(if (initialTarget > 0) initialTarget.toLong().toString() else "") }
    var days by remember { mutableStateOf(initialDays) }
    var icon by remember { mutableStateOf(initialIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Tên mục tiêu (VD: Mua iPhone)") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = formatAmountInput(target),
                    onValueChange = { 
                        val clean = it.filter { char -> char.isDigit() }
                        target = clean 
                    },
                    label = { Text("Số tiền mục tiêu") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("₫") }
                )

                OutlinedTextField(
                    value = days, 
                    onValueChange = { if (it.all { c -> c.isDigit() }) days = it }, 
                    label = { Text("Thời hạn (số ngày từ bây giờ)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("ngày ", color = Color.Gray) }
                )

                Text("Chọn biểu tượng:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                val icons = listOf("💰", "🏠", "🚗", "📱", "✈️", "🎓", "💍", "💻", "🏍️")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(icons) { item ->
                        Surface(
                            onClick = { icon = item },
                            color = if (icon == item) Color(0xFFE0F2F1) else Color.Transparent,
                            shape = CircleShape,
                            border = if (icon == item) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00796B)) else null
                        ) {
                            Text(item, modifier = Modifier.padding(12.dp), fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("XÓA")
                    }
                }
                
                Button(
                    onClick = { 
                        val targetTime = System.currentTimeMillis() + (days.toLongOrNull() ?: 30L) * 24 * 60 * 60 * 1000
                        onConfirm(name, target.toDoubleOrNull() ?: 0.0, icon, targetTime) 
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Text(if (onDelete != null) "CẬP NHẬT" else "TẠO MỤC TIÊU")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("HỦY", color = Color.Gray) }
        }
    )
}
