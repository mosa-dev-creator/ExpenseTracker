package app.mosa.expensetracker

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}