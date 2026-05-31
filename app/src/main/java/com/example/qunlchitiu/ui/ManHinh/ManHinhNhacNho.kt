package com.example.qunlchitiu.ui.ManHinh

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.qunlchitiu.model.NhacNho
import com.example.qunlchitiu.viewmodel.DieuKhienTaiChinh
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhNhacNho(navController: NavController, viewModel: DieuKhienTaiChinh) {
    val reminders by viewModel.reminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<NhacNho?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhắc nhở hóa đơn") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingReminder = null
                showDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chưa có nhắc nhở nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderItem(
                        reminder = reminder,
                        onTogglePaid = { viewModel.toggleReminderPaid(reminder) },
                        onClick = {
                            editingReminder = reminder
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf(editingReminder?.title ?: "") }
        var amountStr by remember { mutableStateOf(editingReminder?.amount?.toLong()?.toString() ?: "") }
        var selectedDate by remember { mutableLongStateOf(editingReminder?.dueDate ?: System.currentTimeMillis()) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingReminder == null) "Thêm nhắc nhở mới" else "Sửa nhắc nhở") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tên hóa đơn") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = formatAmountInput(amountStr), 
                        onValueChange = { 
                            val clean = it.filter { char -> char.isDigit() }
                            amountStr = clean 
                        }, 
                        label = { Text("Số tiền") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        suffix = { Text("₫") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            DatePickerDialog(context, { _, y, m, d ->
                                val selected = Calendar.getInstance().apply { set(y, m, d) }
                                selectedDate = selected.timeInMillis
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ngày hạn: ${dateFormatter.format(Date(selectedDate))}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amount > 0) {
                        if (editingReminder == null) {
                            viewModel.addReminder(title, amount, selectedDate)
                        } else {
                            viewModel.updateReminder(editingReminder!!.copy(title = title, amount = amount, dueDate = selectedDate))
                        }
                        showDialog = false
                    }
                }) { Text(if (editingReminder == null) "Lưu" else "Cập nhật") }
            },
            dismissButton = {
                Row {
                    if (editingReminder != null) {
                        TextButton(onClick = {
                            viewModel.deleteReminder(editingReminder!!)
                            showDialog = false
                        }) {
                            Text("Xóa", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("Hủy") }
                }
            }
        )
    }
}

@Composable
fun ReminderItem(reminder: NhacNho, onTogglePaid: () -> Unit, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(checked = reminder.isPaid, onCheckedChange = { onTogglePaid() })
                Column {
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.Bold,
                        style = if (reminder.isPaid) MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge
                    )
                    Text("Hạn: ${dateFormatter.format(Date(reminder.dueDate))}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text(formatCurrency(reminder.amount), fontWeight = FontWeight.Bold, color = if (reminder.isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error)
        }
    }
}
