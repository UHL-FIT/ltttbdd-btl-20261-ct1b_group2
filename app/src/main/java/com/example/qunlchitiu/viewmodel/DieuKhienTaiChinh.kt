package com.example.qunlchitiu.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qunlchitiu.data.CoSoDuLieu
import com.example.qunlchitiu.data.TaiChinhRepository
import com.example.qunlchitiu.data.TiGiaService
import com.example.qunlchitiu.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ThongKeNganSach(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val expenseCategorySummaries: Map<String, Double> = emptyMap(),
    val incomeCategorySummaries: Map<String, Double> = emptyMap()
)

enum class CheDoXem { DAY, WEEK, MONTH, YEAR, SEVEN_DAYS, FOURTEEN_DAYS }

sealed class UIState<out T> {
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val message: String) : UIState<Nothing>()
}

class DieuKhienTaiChinh(application: Application) : AndroidViewModel(application) {
    private val dao = CoSoDuLieu.getDatabase(application).expenseDao()
    private val repository = TaiChinhRepository(dao, TiGiaService.create())

    private val _tiGiaState = MutableStateFlow<UIState<ResponseTiGia>>(UIState.Loading)
    val tiGiaState: StateFlow<UIState<ResponseTiGia>> = _tiGiaState

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate

    private val _viewMode = MutableStateFlow(CheDoXem.MONTH)
    val viewMode: StateFlow<CheDoXem> = _viewMode

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentBudget: StateFlow<NganSach?> = _selectedDate.flatMapLatest { date ->
        val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(date.time)
        dao.getBudgetForMonth(monthYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryBudgets: StateFlow<List<NganSachDanhMuc>> = _selectedDate.flatMapLatest { date ->
        val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(date.time)
        dao.getAllCategoryBudgets(monthYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<MucTieuTietKiem>> = dao.getAllSavingsGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<NhacNho>> = dao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedDefaultCategories()
        fetchExchangeRates()
    }

    fun fetchExchangeRates() {
        viewModelScope.launch {
            _tiGiaState.value = UIState.Loading
            try {
                val result = repository.getExchangeRates()
                _tiGiaState.value = UIState.Success(result)
            } catch (e: Exception) {
                _tiGiaState.value = UIState.Error("Không thể tải tỷ giá: ${e.message}")
            }
        }
    }

    private fun seedDefaultCategories() {
        viewModelScope.launch {
            val existing = dao.getAllCategoriesOnce()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    DanhMuc(name = "Lương", icon = "💵", isIncome = true, isDefault = true),
                    DanhMuc(name = "Đầu tư", icon = "📈", isIncome = true, isDefault = true),
                    DanhMuc(name = "Quà tặng", icon = "🎁", isIncome = true, isDefault = true),
                    DanhMuc(name = "Hoàn tiền", icon = "💸", isIncome = true, isDefault = true),
                    DanhMuc(name = "Thực phẩm", icon = "🛒", isIncome = false, isDefault = true),
                    DanhMuc(name = "Ăn uống", icon = "🍽️", isIncome = false, isDefault = true),
                    DanhMuc(name = "Nhà ở", icon = "🏠", isIncome = false, isDefault = true),
                    DanhMuc(name = "Tiện ích", icon = "💡", isIncome = false, isDefault = true),
                    DanhMuc(name = "Giao thông", icon = "🚗", isIncome = false, isDefault = true),
                    DanhMuc(name = "Du lịch", icon = "✈️", isIncome = false, isDefault = true),
                    DanhMuc(name = "Sức khỏe & Thể thao", icon = "🏋️", isIncome = false, isDefault = true)
                )
                defaults.forEach { dao.insertCategory(it) }
            }
        }
    }

    val allExpenses: StateFlow<List<GiaoDich>> = dao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<DanhMuc>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<GiaoDich>> = combine(
        dao.getAllExpenses(),
        _selectedDate,
        _viewMode
    ) { list, date, mode ->
        val now = Calendar.getInstance()
        list.filter { expense ->
            val expCal = Calendar.getInstance().apply { timeInMillis = expense.date }
            when (mode) {
                CheDoXem.DAY -> {
                    expCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    expCal.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
                }
                CheDoXem.WEEK -> {
                    expCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    expCal.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)
                }
                CheDoXem.MONTH -> {
                    expCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    expCal.get(Calendar.MONTH) == date.get(Calendar.MONTH)
                }
                CheDoXem.YEAR -> {
                    expCal.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                }
                CheDoXem.SEVEN_DAYS -> {
                    val threshold = (now.clone() as Calendar).apply { 
                        add(Calendar.DAY_OF_YEAR, -6)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    expCal.timeInMillis >= threshold.timeInMillis
                }
                CheDoXem.FOURTEEN_DAYS -> {
                    val threshold = (now.clone() as Calendar).apply { 
                        add(Calendar.DAY_OF_YEAR, -13)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    expCal.timeInMillis >= threshold.timeInMillis
                }
            }
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<ThongKeNganSach> = expenses.map { list ->
        val incomeList = list.filter { it.isIncome }
        val expenseList = list.filter { !it.isIncome }
        
        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpense = expenseList.sumOf { it.amount }
        
        val incomeSummaries = incomeList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            
        val expenseSummaries = expenseList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            
        ThongKeNganSach(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            expenseCategorySummaries = expenseSummaries,
            incomeCategorySummaries = incomeSummaries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThongKeNganSach())

    fun moveDate(amount: Int) {
        val newCal = (_selectedDate.value.clone() as Calendar).apply {
            when (_viewMode.value) {
                CheDoXem.DAY -> add(Calendar.DAY_OF_MONTH, amount)
                CheDoXem.WEEK -> add(Calendar.WEEK_OF_YEAR, amount)
                CheDoXem.MONTH -> add(Calendar.MONTH, amount)
                CheDoXem.YEAR -> add(Calendar.YEAR, amount)
                else -> {} // For 7/14 days, we usually look from "now"
            }
        }
        _selectedDate.value = newCal
    }

    fun setViewMode(mode: CheDoXem) {
        _viewMode.value = mode
    }

    fun goToToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    fun setDate(date: Long) {
        _selectedDate.value = Calendar.getInstance().apply { timeInMillis = date }
    }

    fun addTransaction(title: String, amount: Double, category: String, isIncome: Boolean, date: Long = System.currentTimeMillis(), note: String = "") {
        viewModelScope.launch {
            dao.insertExpense(GiaoDich(title = title, amount = amount, category = category, 
                date = date, isIncome = isIncome, note = note))
        }
    }

    fun updateTransaction(expense: GiaoDich) {
        viewModelScope.launch {
            dao.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: GiaoDich) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
        }
    }

    fun addCategory(name: String, icon: String, isIncome: Boolean) {
        viewModelScope.launch {
            dao.insertCategory(DanhMuc(name = name, icon = icon, isIncome = isIncome))
        }
    }

    fun deleteCategory(category: DanhMuc) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    fun setBudget(limit: Double) {
        viewModelScope.launch {
            val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(_selectedDate.value.time)
            dao.insertBudget(NganSach(monthYear, limit))
        }
    }

    fun setCategoryBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(_selectedDate.value.time)
            dao.insertCategoryBudget(NganSachDanhMuc(monthYear = monthYear, categoryName = categoryName, limitAmount = limit))
        }
    }

    fun addSavingsGoal(name: String, target: Double, icon: String = "💰", deadline: Long = 0L) {
        viewModelScope.launch {
            dao.insertSavingsGoal(MucTieuTietKiem(name = name, targetAmount = target, icon = icon, targetDate = deadline))
        }
    }

    fun updateSavingsGoal(goal: MucTieuTietKiem) {
        viewModelScope.launch {
            dao.updateSavingsGoal(goal)
        }
    }

    fun addToSavings(goalId: Int, amount: Double) {
        viewModelScope.launch {
            dao.addToSavingsGoal(goalId, amount)
        }
    }

    fun deleteSavingsGoal(goal: MucTieuTietKiem) {
        viewModelScope.launch {
            dao.deleteSavingsGoal(goal)
        }
    }

    fun addReminder(title: String, amount: Double, dueDate: Long, category: String = "Khác") {
        viewModelScope.launch {
            dao.insertReminder(NhacNho(title = title, amount = amount, dueDate = dueDate, category = category))
        }
    }

    fun updateReminder(reminder: NhacNho) {
        viewModelScope.launch {
            dao.updateReminder(reminder)
        }
    }

    fun toggleReminderPaid(reminder: NhacNho) {
        viewModelScope.launch {
            dao.updateReminder(reminder.copy(isPaid = !reminder.isPaid))
        }
    }

    fun deleteReminder(reminder: NhacNho) {
        viewModelScope.launch {
            dao.deleteReminder(reminder)
        }
    }

    fun deleteCategoryBudget(budget: NganSachDanhMuc) {
        viewModelScope.launch {
            dao.deleteCategoryBudget(budget)
        }
    }

    suspend fun getExpenseById(id: Int): GiaoDich? {
        return dao.getExpenseById(id)
    }

    fun exportToCSV(context: Context) {
        viewModelScope.launch {
            val file = File(context.getExternalFilesDir(null), "BudgetBuddy_Export.csv")
            val content = StringBuilder("ID,Tiêu đề,Số tiền,Danh mục,Ngày,Loại,Ghi chú\n")
            expenses.value.forEach {
                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it.date))
                content.append("${it.id},${it.title},${it.amount},${it.category},$dateStr,${if(it.isIncome) "Thu" else "Chi"},${it.note}\n")
            }
            file.writeText(content.toString())
            Toast.makeText(context, "Đã xuất file: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
