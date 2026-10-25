package app.mosa.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(
        ExpenseUiState.Loading
    )

    //Lire la liste
    val uiState = _uiState.asStateFlow()

    private var loadExpensesJob: Job? = null

    fun loadExpenses(){

        loadExpensesJob?.cancel()

        loadExpensesJob = viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            repository.getExpenses()
                .catch {
                    _uiState.value = ExpenseUiState.Error(
                        message = "Impossible de charger les dépense"
                    )
                }
                .collect { expenses ->
                    _uiState.value = ExpenseUiState.Success(
                        expenses = expenses
                    )
                }
        }
    }

    //Ajouter une dépense
    fun addExpense(
        expense: Expense,
        onSuccess: () -> Unit
    ){
        viewModelScope.launch {

            try {
                repository.addExpense(expense)
                onSuccess()
            } catch (e: Exception){
                _uiState.value = ExpenseUiState.Error(
                    message = "Impossible d'ajouter la dépense"
                )
            }
        }
    }

    //Supprimer une dépense
    fun deleteExpense(
        expense: Expense
    ){
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(
                    message = "Impossible de supprimer la dépense"
                )
            }
        }
    }
}