package com.example.qunlchitiu.ui.ManHinh

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.DanhMuc
import com.example.qunlchitiu.model.GiaoDich
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManHinhThemGiaoDich(
    navController: NavController, 
    viewModel: DieuKhienTaiChinh, 
    isIncome: Boolean,
    expenseId: Int = -1
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingExpense by remember { mutableStateOf<GiaoDich?>(null) }
    
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val allCategories by viewModel.allCategories.collectAsState()
    val categories = allCategories.filter { it.isIncome == isIncome }
    
    var categoryName by remember { mutableStateOf("") }
    var categoryIcon by remember { mutableStateOf("❓") }
    
    var showCategorySheet by remember { mutableStateOf(false) }
    var showManageSheet by remember { mutableStateOf(false) }
    var showAddCategorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (categoryName.isEmpty() && categories.isNotEmpty()) {
            categoryName = categories[0].name
            categoryIcon = categories[0].icon
        }
    }

    LaunchedEffect(expenseId) {
        if (expenseId != -1) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                existingExpense = expense
                title = expense.title
                amount = if (expense.amount == 0.0) "" else expense.amount.toInt().toString()
                categoryName = expense.category
                note = expense.note
                selectedDate = expense.date
                // Find icon for category
                categoryIcon = allCategories.find { it.name == expense.category }?.icon ?: "❓"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giao dịch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (expenseId != -1 && existingExpense != null) {
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteExpense(existingExpense!!)
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Xóa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            val amountDouble = amount.toDoubleOrNull()
                            if (title.isBlank() || amountDouble == null || amountDouble <= 0) {
                                // error handling
                            } else {
                                if (expenseId != -1 && existingExpense != null) {
                                    viewModel.updateTransaction(
                                        existingExpense!!.copy(
                                            title = title,
                                            amount = amountDouble,
                                            category = categoryName,
                                            note = note,
                                            date = selectedDate
                                        )
                                    )
                                } else {
                                    viewModel.addTransaction(title, amountDouble, categoryName, isIncome, selectedDate, note)
                                }
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text(if (expenseId != -1) "Cập nhật" else "Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Nhập tên giao dịch", fontSize = 24.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal)
            )

            TextField(
                value = formatAmountInput(amount),
                onValueChange = { 
                    val clean = it.filter { char -> char.isDigit() }
                    amount = clean 
                },
                placeholder = { Text("0", fontSize = 48.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("₫", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)) }
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                onClick = { showCategorySheet = true },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F5F5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(categoryIcon, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(categoryName, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Note, Date, Time inputs
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày") },
                    modifier = Modifier.weight(1f).clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            cal.set(y, m, d)
                            selectedDate = cal.timeInMillis
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = timeFormatter.format(Date(selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giờ") },
                    modifier = Modifier.weight(1f).clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        TimePickerDialog(context, { _, h, min ->
                            cal.set(Calendar.HOUR_OF_DAY, h)
                            cal.set(Calendar.MINUTE, min)
                            selectedDate = cal.timeInMillis
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledLabelColor = Color.Gray),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Chọn một danh mục", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showCategorySheet = false }) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = categoryName == cat.name,
                            onClick = { categoryName = cat.name; categoryIcon = cat.icon; showCategorySheet = false },
                            label = { Text(cat.name) },
                            leadingIcon = { Text(cat.icon) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF6750A4).copy(alpha = 0.1f), selectedLabelColor = Color(0xFF6750A4))
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                OutlinedButton(onClick = { showCategorySheet = false; showManageSheet = true }, modifier = Modifier.align(Alignment.CenterHorizontally), shape = RoundedCornerShape(24.dp)) {
                    Icon(Icons.Default.Settings, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Quản lý Danh mục")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showManageSheet) {
        var manageTabIsIncome by remember { mutableStateOf(isIncome) }
        val filteredCats = allCategories.filter { it.isIncome == manageTabIsIncome }
        ModalBottomSheet(onDismissRequest = { showManageSheet = false }, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Danh mục", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showManageSheet = false }) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(16.dp))
                TabRow(selectedTabIndex = if (manageTabIsIncome) 1 else 0, containerColor = Color(0xFFF5F5F5), modifier = Modifier.height(48.dp).fillMaxWidth(), indicator = {}, divider = {}) {
                    Tab(selected = !manageTabIsIncome, onClick = { manageTabIsIncome = false }, modifier = Modifier.padding(4.dp)) {
                        Surface(color = if (!manageTabIsIncome) Color.White else Color.Transparent, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxSize()) {
                            Box(contentAlignment = Alignment.Center) { Text("Chi tiêu", color = if (!manageTabIsIncome) Color(0xFF6750A4) else Color.Gray) }
                        }
                    }
                    Tab(selected = manageTabIsIncome, onClick = { manageTabIsIncome = true }, modifier = Modifier.padding(4.dp)) {
                        Surface(color = if (manageTabIsIncome) Color.White else Color.Transparent, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxSize()) {
                            Box(contentAlignment = Alignment.Center) { Text("Thu nhập", color = if (manageTabIsIncome) Color(0xFF6750A4) else Color.Gray) }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Danh mục Mặc định", color = Color.Gray, fontSize = 14.sp)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredCats) { cat ->
                        ListItem(
                            headlineContent = { Text(cat.name) },
                            supportingContent = { Text("Danh mục mặc định", fontSize = 12.sp, color = Color.Gray) },
                            leadingContent = {
                                Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 20.sp) }
                                }
                            },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) }
                        )
                    }
                }
                Button(onClick = { showAddCategorySheet = true }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))) {
                    Text("Thêm Danh mục")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showAddCategorySheet) {
        var newCatName by remember { mutableStateOf("") }
        var newCatIcon by remember { mutableStateOf("📦") }
        var newCatIsIncome by remember { mutableStateOf(isIncome) }
        var showTypePicker by remember { mutableStateOf(false) }
        var showEmojiPicker by remember { mutableStateOf(false) }

        val commonEmojis = listOf(
            "💰", "💵", "💸", "🏦", "📈", "💳", "🛒", "🍔", "☕", "🍽️",
            "🏠", "💡", "🚗", "🚌", "✈️", "🏥", "💊", "🏋️", "🎓", "🎮",
            "🎁", "🎬", "🎵", "📱", "💻", "🧼", "🧹", "🛠️", "📅", "📦"
        )

        ModalBottomSheet(onDismissRequest = { showAddCategorySheet = false }, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Thêm Danh mục", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showAddCategorySheet = false }) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(32.dp))
                Surface(
                    color = Color(0xFFF5F5F5), 
                    shape = CircleShape, 
                    modifier = Modifier.size(80.dp).clickable { showEmojiPicker = true }
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(newCatIcon, fontSize = 40.sp) }
                }
                Text("Nhấn để thay đổi", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                TextField(
                    value = newCatName, 
                    onValueChange = { if (it.length <= 30) newCatName = it }, 
                    placeholder = { Text("Nhập tên danh mục") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5)), 
                    suffix = { Text("${newCatName.length}/30", color = Color.Gray, fontSize = 12.sp) }
                )
                Spacer(Modifier.height(16.dp))
                Surface(onClick = { showTypePicker = true }, color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.SwapHoriz, null, tint = Color.Gray); Spacer(Modifier.width(12.dp)); Text("Loại Danh mục") }
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(if (newCatIsIncome) "Thu nhập" else "Chi tiêu", fontWeight = FontWeight.Bold); Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) }
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { 
                        if (newCatName.isNotBlank()) { 
                            viewModel.addCategory(newCatName, newCatIcon, newCatIsIncome)
                            showAddCategorySheet = false 
                        } 
                    }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp), 
                    shape = RoundedCornerShape(28.dp), 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Xong")
                }
            }
            if (showTypePicker) {
                ModalBottomSheet(onDismissRequest = { showTypePicker = false }, containerColor = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Loại Danh mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(16.dp))
                        ListItem(headlineContent = { Text("Chi tiêu") }, leadingContent = { Icon(Icons.Default.ArrowUpward, null, tint = Color.Red) }, trailingContent = { RadioButton(selected = !newCatIsIncome, onClick = { newCatIsIncome = false; showTypePicker = false }) }, modifier = Modifier.clickable { newCatIsIncome = false; showTypePicker = false })
                        ListItem(headlineContent = { Text("Thu nhập") }, leadingContent = { Icon(Icons.Default.ArrowDownward, null, tint = Color.Green) }, trailingContent = { RadioButton(selected = newCatIsIncome, onClick = { newCatIsIncome = true; showTypePicker = false }) }, modifier = Modifier.clickable { newCatIsIncome = true; showTypePicker = false })
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            if (showEmojiPicker) {
                ModalBottomSheet(onDismissRequest = { showEmojiPicker = false }, containerColor = Color.White) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Chọn biểu tượng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(16.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            commonEmojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable { newCatIcon = emoji; showEmojiPicker = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 32.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
