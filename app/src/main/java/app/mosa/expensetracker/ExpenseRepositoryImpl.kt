package app.mosa.expensetracker

import kotlinx.coroutines.delay
import javax.inject.Inject


class ExpenseRepositoryImpl @Inject constructor() : ExpenseRepository {

    private val expenses = mutableListOf(
        Expense(150000),
        Expense(50000),
        Expense(25000)
    )

    override suspend fun getExpenses(): List<Expense> {

        delay(2000)

        return expenses.toList()
    }

    override suspend fun addExpense(expense: Expense) {

        expenses.add(expense)
    }
}