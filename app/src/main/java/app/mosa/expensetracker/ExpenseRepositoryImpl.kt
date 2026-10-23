package app.mosa.expensetracker

import javax.inject.Inject


class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun getExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
}