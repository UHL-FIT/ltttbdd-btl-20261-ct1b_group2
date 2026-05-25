package com.example.qunlchitiu.ui.ManHinh

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.NganSachDanhMuc
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhNganSach(navController: NavController, viewModel: DieuKhienTaiChinh) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetDialog by remember { mutableStateOf(false) }
    var editingCategoryBudget by remember { mutableStateOf<NganSachDanhMuc?>(null) }
    
    var budgetInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Đặt ngân sách tổng") },
            text = {
                OutlinedTextField(
                    value = formatAmountInput(budgetInput),
                    onValueChange = { 
                        val clean = it.filter { char -> char.isDigit() }
                        budgetInput = clean 
                    },
                    label = { Text("Số tiền hạn mức") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Ngân sách") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingCategoryBudget = null
                showCategoryBudgetDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ngân sách tổng hàng tháng", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        val limit = currentBudget?.limitAmount ?: 0.0
                        val spent = stats.totalExpense
                        val remaining = limit - spent
                        val progress = if (limit > 0) (spent / limit).coerceIn(0.0, 1.0) else 0.0

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Đã chi: ${formatCurrency(spent)}")
                            Text("Hạn mức: ${formatCurrency(limit)}")
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = if (progress > 0.9) Color.Red else MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (remaining >= 0) "Còn lại: ${formatCurrency(remaining)}" else "Vượt mức: ${formatCurrency(-remaining)}",
                            fontWeight = FontWeight.Bold,
                            color = if (remaining >= 0) Color.Unspecified else Color.Red
                        )
                        
                        Button(
                            onClick = { showBudgetDialog = true },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Thay đổi")
                        }
                    }
                }
            }

            item {
                Text("Ngân sách theo danh mục", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (categoryBudgets.isEmpty()) {
                item {
                    Text("Chưa có ngân sách riêng cho danh mục nào", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            items(categoryBudgets) { cb ->
                val spent = stats.expenseCategorySummaries[cb.categoryName] ?: 0.0
                val progress = if (cb.limitAmount > 0) (spent / cb.limitAmount).coerceIn(0.0, 1.0) else 0.0
                
                Card(
                    onClick = {
                        editingCategoryBudget = cb
                        showCategoryBudgetDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cb.categoryName, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${formatCurrency(spent)} / ${formatCurrency(cb.limitAmount)}", fontSize = 14.sp)
                            Text("${(progress * 100).toInt()}%", fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (progress > 0.9) Color.Red else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }

    if (showCategoryBudgetDialog) {
        var amountStr by remember { mutableStateOf(editingCategoryBudget?.limitAmount?.toLong()?.toString() ?: "") }
        var selectedCat by remember { mutableStateOf(editingCategoryBudget?.categoryName ?: (allCategories.firstOrNull { !it.isIncome }?.name ?: "")) }

        AlertDialog(
            onDismissRequest = { showCategoryBudgetDialog = false },
            title = { Text(if (editingCategoryBudget == null) "Thêm ngân sách danh mục" else "Sửa ngân sách danh mục") },
            text = {
                Column {
                    if (editingCategoryBudget == null) {
                        Text("Chọn danh mục:")
                        val scrollState = rememberScrollState()
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(vertical = 8.dp)) {
                            allCategories.filter { !it.isIncome }.forEach { cat ->
                                FilterChip(
                                    selected = selectedCat == cat.name,
                                    onClick = { selectedCat = cat.name },
                                    label = { Text(cat.name) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text("Danh mục: ${editingCategoryBudget?.categoryName}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = formatAmountInput(amountStr),
                        onValueChange = { 
                            val clean = it.filter { char -> char.isDigit() }
                            amountStr = clean 
                        },
                        label = { Text("Hạn mức chi tiêu") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        suffix = { Text("₫") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (selectedCat.isNotEmpty() && amount > 0) {
                        viewModel.setCategoryBudget(selectedCat, amount)
                        showCategoryBudgetDialog = false
                        amountStr = ""
                    }
                }) { Text(if (editingCategoryBudget == null) "Lưu" else "Cập nhật") }
            },
            dismissButton = {
                Row {
                    if (editingCategoryBudget != null) {
                        TextButton(onClick = {
                            viewModel.deleteCategoryBudget(editingCategoryBudget!!)
                            showCategoryBudgetDialog = false
                        }) {
                            Text("Xóa", color = Color.Red)
                        }
                    }
                    TextButton(onClick = { showCategoryBudgetDialog = false }) { Text("Hủy") }
                }
            }
        )
    }
}
