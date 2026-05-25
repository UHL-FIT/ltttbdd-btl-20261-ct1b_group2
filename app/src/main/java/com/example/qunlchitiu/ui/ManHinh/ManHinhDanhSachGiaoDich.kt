package com.example.qunlchitiu.ui.ManHinh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.GiaoDich
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManHinhDanhSachGiaoDich(
    navController: NavController,
    viewModel: DieuKhienTaiChinh,
    initialCategory: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(if (initialCategory != null) setOf(initialCategory) else emptySet<String>()) }
    var priceRange by remember { mutableStateOf(0f..10000000f) }
    
    // Thêm trạng thái lọc Loại giao dịch (Tất cả, Thu nhập, Chi tiêu)
    var selectedTypeFilter by remember { mutableStateOf("Tất cả") }
    
    // Thêm trạng thái lọc ngày
    val dateFilters = listOf("Tất cả", "Tháng này", "Tháng trước", "3 Tháng", "6 Tháng")
    var selectedDateFilter by remember { mutableStateOf("Tất cả") }
    
    val expenses by viewModel.allExpenses.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val filteredExpenses = expenses.filter {
        val expenseCal = Calendar.getInstance().apply { timeInMillis = it.date }
        val now = Calendar.getInstance()
        
        val dateMatch = when (selectedDateFilter) {
            "Tháng này" -> {
                expenseCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                expenseCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
            "Tháng trước" -> {
                val lastMonth = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                expenseCal.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR) &&
                expenseCal.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH)
            }
            "3 Tháng" -> {
                val threeMonthsAgo = (now.clone() as Calendar).apply { add(Calendar.MONTH, -3) }
                expenseCal.after(threeMonthsAgo)
            }
            "6 Tháng" -> {
                val sixMonthsAgo = (now.clone() as Calendar).apply { add(Calendar.MONTH, -6) }
                expenseCal.after(sixMonthsAgo)
            }
            else -> true // Tất cả
        }

        val typeMatch = when (selectedTypeFilter) {
            "Thu nhập" -> it.isIncome
            "Chi tiêu" -> !it.isIncome
            else -> true
        }

        dateMatch && typeMatch &&
        (selectedCategories.isEmpty() || selectedCategories.contains(it.category)) &&
        (it.amount >= priceRange.start && it.amount <= priceRange.endInclusive) &&
        (it.title.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true))
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tất cả giao dịch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Quick Date Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dateFilters) { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { selectedDateFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6750A4).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF6750A4)
                        )
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm giao dịch") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                }
            }

            // Active Filters Display
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF6750A4),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp).clickable { showFilterSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bộ lọc", color = Color.White, fontSize = 14.sp)
                    }
                }

                if (selectedCategories.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedCategories.toList()) { cat ->
                            InputChip(
                                selected = true,
                                onClick = { 
                                    selectedCategories = selectedCategories - cat 
                                    // Khi xóa category, nếu nó là Lương thì tự động bật lọc Thu nhập
                                    if (cat == "Lương") {
                                        selectedTypeFilter = "Thu nhập"
                                    }
                                },
                                label = { Text(cat) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                } else {
                    // Hiển thị nút Thu nhập/Chi tiêu khi không có danh mục nào được chọn
                    listOf("Thu nhập", "Chi tiêu").forEach { type ->
                        val isSelected = selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                selectedTypeFilter = if (isSelected) "Tất cả" else type 
                            },
                            label = { Text(type) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6750A4).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF6750A4)
                            )
                        )
                    }
                }
            }

            Text(
                "Đang hiển thị ${filteredExpenses.size} giao dịch",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )

            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, Modifier.size(64.dp), Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("Không tìm thấy giao dịch nào", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val grouped = filteredExpenses.groupBy { 
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        val today = Calendar.getInstance()
                        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && 
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) "Hôm nay"
                        else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.date)
                    }

                    grouped.forEach { (date, items) ->
                        item {
                            Text(
                                text = date,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E4B8B),
                                fontSize = 18.sp
                            )
                        }
                        items(items, key = { it.id }) { expense ->
                            val catIcon = allCategories.find { it.name == expense.category }?.icon ?: "📦"
                            ExpenseItemInList(expense, {
                                navController.navigate("add_expense?isIncome=${expense.isIncome}&expenseId=${expense.id}")
                            }, catIcon)
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        var tempPriceRange by remember { mutableStateOf(priceRange) }
        var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.9f)) { // Giới hạn chiều cao sheet
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lọc giao dịch", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showFilterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Phần nội dung có thể cuộn
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Số tiền", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${tempPriceRange.start.toInt()}đ", color = Color.Gray)
                            Text("${tempPriceRange.endInclusive.toInt()}đ", color = Color.Gray)
                        }
                        RangeSlider(
                            value = tempPriceRange,
                            onValueChange = { tempPriceRange = it },
                            valueRange = 0f..10000000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6750A4),
                                activeTrackColor = Color(0xFF6750A4)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allCategories.forEach { cat ->
                                val isSelected = tempSelectedCategories.contains(cat.name)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        tempSelectedCategories = if (isSelected) tempSelectedCategories - cat.name else tempSelectedCategories + cat.name
                                    },
                                    label = { Text(cat.name) },
                                    leadingIcon = { Text(cat.icon) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6750A4).copy(alpha = 0.15f),
                                        selectedLabelColor = Color(0xFF6750A4)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Phần nút cố định ở dưới
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tempPriceRange = 0f..10000000f
                                    tempSelectedCategories = emptySet()
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("Đặt lại", color = Color.Black)
                            }
                            Button(
                                onClick = {
                                    priceRange = tempPriceRange
                                    selectedCategories = tempSelectedCategories
                                    showFilterSheet = false
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text("Áp dụng bộ lọc")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItemInList(expense: GiaoDich, onClick: () -> Unit, icon: String = "📦") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(expense.note.ifEmpty { expense.category }, color = Color.Gray, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (expense.isIncome) "+" else "-") + formatCurrency(expense.amount),
                    color = if (expense.isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (Calendar.getInstance().apply { timeInMillis = expense.date }.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) "Hôm nay" else "",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
