package app.mosa.expensetracker

interface ExpenseRepository {

    suspend fun getExpenses(): List<Expense>

    suspend fun addExpense(expense: Expense)

}