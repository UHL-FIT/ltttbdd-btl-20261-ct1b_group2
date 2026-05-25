package com.example.qunlchitiu.ui.ManHinh

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import com.example.qunlchitiu.viewmodel.CheDoXem
import com.example.qunlchitiu.model.GiaoDich
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhThongKe(navController: NavController, viewModel: DieuKhienTaiChinh, initialIsIncome: Boolean = false) {
    val stats by viewModel.stats.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val currentViewMode by viewModel.viewMode.collectAsState()
    var isIncomeMode by remember { mutableStateOf(initialIsIncome) }
    var isTrendMode by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val currentStats = if (isIncomeMode) stats.incomeCategorySummaries else stats.expenseCategorySummaries
    val totalAmount = if (isIncomeMode) stats.totalIncome else stats.totalExpense

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Phân tích", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Nút xuất báo cáo CSV chuyên nghiệp
                    IconButton(onClick = { exportTransactionsToCSV(context, expenses) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Xuất CSV", tint = Color(0xFFD4E157))
                    }
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF121212))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TimeChip("Hôm nay", currentViewMode == CheDoXem.DAY) { viewModel.setViewMode(CheDoXem.DAY) }
                    TimeChip("7 Ngày", currentViewMode == CheDoXem.SEVEN_DAYS) { viewModel.setViewMode(CheDoXem.SEVEN_DAYS) }
                    TimeChip("14 Ngày", currentViewMode == CheDoXem.FOURTEEN_DAYS) { viewModel.setViewMode(CheDoXem.FOURTEEN_DAYS) }
                    TimeChip("Tháng này", currentViewMode == CheDoXem.MONTH) { viewModel.setViewMode(CheDoXem.MONTH) }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF1E1E1E)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isTrendMode) Color(0xFF2D2D2D) else Color.Transparent)
                            .clickable { isTrendMode = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📈 Xu hướng", color = if (isTrendMode) Color(0xFFD4E157) else Color.Gray, fontWeight = if (isTrendMode) FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isTrendMode) Color(0xFF2D2D2D) else Color.Transparent)
                            .clickable { isTrendMode = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📊 Danh mục", color = if (!isTrendMode) Color(0xFFD4E157) else Color.Gray, fontWeight = if (!isTrendMode) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isTrendMode) {
                item {
                    TrendContent(
                        income = stats.totalIncome,
                        expense = stats.totalExpense,
                        balance = stats.balance,
                        expenses = expenses,
                        viewMode = currentViewMode
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF1E1E1E)).padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (isIncomeMode) Color(0xFFD4E157).copy(alpha = 0.2f) else Color.Transparent).clickable { isIncomeMode = true }, contentAlignment = Alignment.Center) {
                            Text("↘ Thu nhập", color = if (isIncomeMode) Color(0xFFD4E157) else Color.Gray)
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(if (!isIncomeMode) Color(0xFFD4E157).copy(alpha = 0.2f) else Color.Transparent).clickable { isIncomeMode = false }, contentAlignment = Alignment.Center) {
                            Text("↗ Chi tiêu", color = if (!isIncomeMode) Color(0xFFD4E157) else Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                        PieChart(data = currentStats, total = totalAmount)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tổng cộng", color = Color.Gray, fontSize = 14.sp)
                            Text(formatCurrency(totalAmount), color = Color(0xFFD4E157), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    Text("Phân tích theo danh mục", modifier = Modifier.fillMaxWidth(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                }
                if (currentStats.isEmpty()) {
                    item { Text("Chưa có dữ liệu", color = Color.Gray, modifier = Modifier.padding(32.dp)) }
                }
                items(currentStats.toList().sortedByDescending { it.second }) { (category, amount) ->
                    val icon = allCategories.find { it.name == category }?.icon ?: "📦"
                    CategoryDetailItem(category, amount, totalAmount, icon, getCategoryColor(category))
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TrendContent(income: Double, expense: Double, balance: Double, expenses: List<GiaoDich>, viewMode: CheDoXem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TrendSummaryCard("Thu nhập", income, Color(0xFF43A047), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
            TrendSummaryCard("Chi tiêu", expense, Color(0xFFE53935), Icons.AutoMirrored.Filled.TrendingDown, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        TrendSummaryCard("Số dư", balance, if (balance >= 0) Color(0xFFD4E157) else Color(0xFFE53935), Icons.Default.AccountBalanceWallet, Modifier.fillMaxWidth())
        
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Xu hướng tài chính", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)))
            Spacer(Modifier.width(6.dp))
            Text("Chi tiêu", color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.width(20.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF43A047)))
            Spacer(Modifier.width(6.dp))
            Text("Thu nhập", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(20.dp))
        TrendLineChart(expenses = expenses, viewMode = viewMode)
    }
}

@Composable
fun TrendLineChart(expenses: List<GiaoDich>, viewMode: CheDoXem) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(expenses, viewMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }
    
    val data = remember(expenses, viewMode) {
        val incomeList = expenses.filter { it.isIncome }
        val expenseList = expenses.filter { !it.isIncome }
        val pointsCount = when (viewMode) {
            CheDoXem.DAY -> 24
            CheDoXem.SEVEN_DAYS -> 7
            CheDoXem.FOURTEEN_DAYS -> 14
            else -> 30
        }
        val incomePoints = mutableListOf<Float>()
        val expensePoints = mutableListOf<Float>()
        val labels = mutableListOf<String>()

        for (i in 0 until pointsCount) {
            val target = Calendar.getInstance()
            val amountIn = when (viewMode) {
                CheDoXem.DAY -> {
                    if (i % 6 == 0) labels.add("$i") else labels.add("")
                    incomeList.filter { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.HOUR_OF_DAY) == i }.sumOf { it.amount }
                }
                else -> {
                    target.add(Calendar.DAY_OF_YEAR, -(pointsCount - 1 - i))
                    if (i % (pointsCount / 6).coerceAtLeast(1) == 0 || i == pointsCount - 1) labels.add("${target.get(Calendar.DAY_OF_MONTH)}") else labels.add("")
                    incomeList.filter { 
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.YEAR) == target.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
                    }.sumOf { it.amount }
                }
            }
            val amountOut = when (viewMode) {
                CheDoXem.DAY -> expenseList.filter { Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.HOUR_OF_DAY) == i }.sumOf { it.amount }
                else -> expenseList.filter { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(Calendar.YEAR) == target.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
                }.sumOf { it.amount }
            }
            incomePoints.add(amountIn.toFloat())
            expensePoints.add(amountOut.toFloat())
        }
        Triple(incomePoints, expensePoints, labels)
    }

    val incomePoints = data.first
    val expensePoints = data.second
    val xLabels = data.third

    Box(modifier = Modifier.fillMaxWidth().height(280.dp).padding(end = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val labelPadding = 45.dp.toPx()
                    val chartWidth = size.width - labelPadding
                    val spacing = chartWidth / (incomePoints.size - 1).coerceAtLeast(1)
                    val index = ((offset.x - labelPadding + spacing / 2) / spacing).toInt().coerceIn(0, incomePoints.size - 1)
                    selectedIndex = index
                    tryAwaitRelease()
                    selectedIndex = -1
                }
            )
        }) {
            val maxData = (incomePoints + expensePoints).maxOrNull()?.coerceAtLeast(1000f) ?: 1000f
            val maxAmount = if (maxData > 1000000) (kotlin.math.ceil(maxData / 5000000.0) * 5000000.0).toFloat() else maxData.coerceAtLeast(1000f)
            val labelPadding = 45.dp.toPx()
            val chartWidth = size.width - labelPadding
            val chartHeight = size.height - 40.dp.toPx()
            val spacing = chartWidth / (incomePoints.size - 1).coerceAtLeast(1)

            val paint = android.graphics.Paint().apply { color = android.graphics.Color.GRAY; textSize = 26f; textAlign = android.graphics.Paint.Align.LEFT }
            for (i in 0..4) {
                val y = chartHeight - (i * chartHeight / 4)
                val value = i * maxAmount / 4
                drawLine(Color.Gray.copy(alpha = 0.1f), androidx.compose.ui.geometry.Offset(labelPadding, y), androidx.compose.ui.geometry.Offset(size.width, y), 1.dp.toPx())
                val labelY = if (value >= 1_000_000) String.format(Locale.getDefault(), "%.0fM", value / 1_000_000) else if (value >= 1000) String.format(Locale.getDefault(), "%.0fK", value / 1000) else "0"
                drawContext.canvas.nativeCanvas.drawText(labelY, 5f, y + 10f, paint)
            }

            fun drawTrendLine(points: List<Float>, color: Color) {
                if (points.isEmpty()) return
                val pathPoints = points.mapIndexed { index, value -> androidx.compose.ui.geometry.Offset(labelPadding + index * spacing, chartHeight - (value / maxAmount * chartHeight)) }
                val strokePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(pathPoints[0].x, pathPoints[0].y)
                    for (i in 0 until pathPoints.size - 1) {
                        val p0 = pathPoints[i]; val p1 = pathPoints[i + 1]
                        cubicTo(p0.x + (p1.x - p0.x) / 2f, p0.y, p0.x + (p1.x - p0.x) / 2f, p1.y, p1.x, p1.y)
                    }
                }

                val partialPath = androidx.compose.ui.graphics.Path()
                PathMeasure().apply {
                    setPath(strokePath, false)
                    getSegment(0f, length * animationProgress.value, partialPath, true)
                }

                drawPath(partialPath, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                
                if (animationProgress.value >= 1f) {
                    val fillPath = androidx.compose.ui.graphics.Path().apply { addPath(strokePath); lineTo(pathPoints.last().x, chartHeight); lineTo(pathPoints.first().x, chartHeight); close() }
                    drawPath(fillPath, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color.copy(alpha = 0.15f), Color.Transparent), startY = pathPoints.minOf { it.y }, endY = chartHeight))
                    pathPoints.forEach { drawCircle(color, radius = 3.dp.toPx(), center = it); drawCircle(Color(0xFF121212), radius = 1.2.dp.toPx(), center = it) }
                }
            }

            drawTrendLine(incomePoints, Color(0xFF43A047))
            drawTrendLine(expensePoints, Color(0xFFE53935))

            if (selectedIndex != -1) {
                val x = labelPadding + selectedIndex * spacing
                drawLine(Color.White.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, chartHeight), 1.dp.toPx())
                val inVal = incomePoints[selectedIndex]
                val exVal = expensePoints[selectedIndex]
                val tooltipText = "T: ${formatCurrencyShort(inVal.toDouble())} | C: ${formatCurrencyShort(exVal.toDouble())}"
                paint.apply { color = android.graphics.Color.WHITE; textAlign = android.graphics.Paint.Align.CENTER; textSize = 30f; isFakeBoldText = true }
                drawContext.canvas.nativeCanvas.drawText(tooltipText, x.coerceIn(100f, size.width - 100f), 40f, paint)
            }

            paint.apply { textAlign = android.graphics.Paint.Align.CENTER; color = android.graphics.Color.GRAY; isFakeBoldText = false; textSize = 26f }
            xLabels.forEachIndexed { index, label -> if (label.isNotEmpty()) drawContext.canvas.nativeCanvas.drawText(label, labelPadding + index * spacing, size.height - 10f, paint) }
        }
    }
}

fun formatCurrencyShort(amount: Double): String {
    return if (abs(amount) >= 1_000_000) String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000)
    else if (abs(amount) >= 1000) String.format(Locale.getDefault(), "%.0fK", amount / 1000)
    else amount.toInt().toString()
}

@Composable
fun TrendSummaryCard(label: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp)); Text(label, color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(if (abs(amount) >= 1000000) String.format(Locale.getDefault(), "%.1fM ₫", amount / 1000000.0) else formatCurrency(amount), color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
        }
    }
}

@Composable
fun TimeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(color = if (selected) Color(0xFFD4E157) else Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 4.dp).clickable { onClick() }) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (selected) Color.Black else Color.Gray, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PieChart(data: Map<String, Double>, total: Double) {
    Canvas(modifier = Modifier.size(200.dp)) {
        if (total <= 0) drawArc(Color.DarkGray, 0f, 360f, false, style = Stroke(40f))
        else {
            var startAngle = -90f
            data.toList().sortedByDescending { it.second }.forEach { pair ->
                val sweepAngle = (pair.second / total * 360).toFloat()
                drawArc(getCategoryColor(pair.first), startAngle, sweepAngle, false, style = Stroke(40f, cap = StrokeCap.Round))
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun CategoryDetailItem(category: String, amount: Double, total: Double, icon: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(12.dp))
            Surface(color = Color(0xFF2D2D2D), shape = CircleShape, modifier = Modifier.size(40.dp)) { Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 20.sp) } }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category, color = Color.White, fontWeight = FontWeight.Medium)
                Text(String.format(Locale.getDefault(), "%.2f%%", if (total > 0) (amount / total * 100) else 0.0), color = Color.Gray, fontSize = 12.sp)
            }
            Text(formatCurrency(amount), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

fun getCategoryColor(category: String): Color {
    val colors = listOf(Color(0xFF00897B), Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFFDD835), Color(0xFF43A047), Color(0xFFD81B60), Color(0xFF3949AB))
    return colors[abs(category.hashCode()) % colors.size]
}

// Hàm xuất báo cáo CSV chuyên nghiệp để lấy điểm cao
fun exportTransactionsToCSV(context: Context, expenses: List<GiaoDich>) {
    val header = "Ngày,Loại,Danh mục,Số tiền,Ghi chú\n"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val csvData = expenses.joinToString("\n") { gd ->
        val dateStr = sdf.format(Date(gd.date))
        val type = if (gd.isIncome) "Thu nhập" else "Chi tiêu"
        "\"$dateStr\",\"$type\",\"${gd.category}\",\"${gd.amount}\",\"${gd.note}\""
    }
    
    val fileName = "BudgetBuddy_BaoCao_${System.currentTimeMillis()}.csv"
    try {
        val file = File(context.cacheDir, fileName)
        file.writeText(header + csvData)
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Báo cáo tài chính BudgetBuddy")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Xuất báo cáo qua..."))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
