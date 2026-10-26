package app.mosa.expensetracker

import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    fun getExpenses(): Flow<List<Expense>>

    suspend fun addExpense(expense: Expense)

    suspend fun updateExpense(expense: Expense)

    suspend fun deleteExpense(expense: Expense)

}