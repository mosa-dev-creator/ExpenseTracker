package app.mosa.expensetracker

sealed class ExpenseUiState {

    data object Loading : ExpenseUiState()

    data class Success(
        val expenses: List<Expense>
    ) : ExpenseUiState()

    data class Error(
        val message: String
    ) : ExpenseUiState()
}