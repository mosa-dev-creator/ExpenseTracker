package app.mosa.expensetracker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Insert
    suspend fun insertExpense(expense: Expense)

}