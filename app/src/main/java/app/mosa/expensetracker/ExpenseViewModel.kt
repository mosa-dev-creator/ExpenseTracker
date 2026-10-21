package app.mosa.expensetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    //Modifier la liste
    fun addExpense(
        expense: Expense,
        onSuccess: () -> Unit
    ){
        viewModelScope.launch {

            try {
                repository.addExpense(expense)

                val expenses = repository.getExpenses()

                _uiState.value = ExpenseUiState.Success(
                    expenses = expenses
                )

                onSuccess()

            } catch (e: Exception){
                _uiState.value = ExpenseUiState.Error(
                    message = "Impossible d'ajouter la dépense"
                )
            }
        }
    }

    fun loadExpenses(){
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading

            try {
                val expenses = repository.getExpenses()

                _uiState.value = ExpenseUiState.Success(
                    expenses = expenses
                )
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(
                    message = "Impossible de charger les dépenses"
                )
            }
        }
    }
}