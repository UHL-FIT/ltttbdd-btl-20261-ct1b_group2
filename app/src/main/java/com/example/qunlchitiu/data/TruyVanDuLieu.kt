package com.example.qunlchitiu.data

import androidx.room.*
import com.example.qunlchitiu.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TruyVanDuLieu {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<GiaoDich>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: GiaoDich)

    @Update
    suspend fun updateExpense(expense: GiaoDich)

    @Delete
    suspend fun deleteExpense(expense: GiaoDich)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): GiaoDich?

    // Category methods
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<DanhMuc>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<DanhMuc>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: DanhMuc)

    @Update
    suspend fun updateCategory(category: DanhMuc)

    @Delete
    suspend fun deleteCategory(category: DanhMuc)

    // Budget methods
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetForMonth(monthYear: String): Flow<NganSach?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: NganSach)

    // Category Budget methods
    @Query("SELECT * FROM category_budgets WHERE monthYear = :monthYear")
    fun getAllCategoryBudgets(monthYear: String): Flow<List<NganSachDanhMuc>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(categoryBudget: NganSachDanhMuc)

    @Delete
    suspend fun deleteCategoryBudget(categoryBudget: NganSachDanhMuc)

    // Savings Goal methods
    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<MucTieuTietKiem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: MucTieuTietKiem)

    @Update
    suspend fun updateSavingsGoal(savingsGoal: MucTieuTietKiem)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: MucTieuTietKiem)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun addToSavingsGoal(goalId: Int, amount: Double)

    // Reminder methods
    @Query("SELECT * FROM reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<NhacNho>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: NhacNho)

    @Update
    suspend fun updateReminder(reminder: NhacNho)

    @Delete
    suspend fun deleteReminder(reminder: NhacNho)
}
