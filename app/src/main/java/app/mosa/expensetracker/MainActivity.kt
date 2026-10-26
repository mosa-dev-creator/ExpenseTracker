package app.mosa.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.mosa.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HommeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HommeScreen(
    modifier: Modifier = Modifier
) {

    val viewModel: ExpenseViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    var amount by remember { mutableStateOf("") }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "ExpenseTracker",
            fontSize = 24.sp
        )

        Text(
            text = "Suivis de vos dépenses"
        )

        when(state){
            is ExpenseUiState.Loading -> {
                Text(
                    text = "Chargement..."
                )
            }

            is ExpenseUiState.Success -> {

                val totalExpenses = state.expenses.sumOf { it.amount }

                Text(
                    text = "Total des dépenses",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = totalExpenses.toString()
                )

                if (state.expenses.isEmpty()) {
                    Text(
                        text = "Aucune dépense enregistrée. Ajoutez votre première dépense ! "
                    )
                } else {
                    LazyColumn {
                        items(state.expenses.size){ index ->

                            val expense = state.expenses[index]

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //Dépense
                                Text(
                                    text = expense.amount.toString(),
                                    modifier = Modifier.clickable {
                                        expenseToEdit = expense
                                        amount = expense.amount.toString()
                                    }
                                )

                                //Icone Supprimer
                                IconButton(
                                    onClick = {
                                        viewModel.deleteExpense(expense)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer cette dépense",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                val montantSaisi = amount.toIntOrNull()

                val messageErreur = when {

                    amount.isBlank() -> null
                    montantSaisi == null -> "Saisissez un nombre entier valide"
                    montantSaisi <= 0 -> "Le montant doit être supérieur à zéro"
                    else -> null
                }

                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                if (messageErreur != null){
                    Text(
                        text = messageErreur
                    )
                }

                Button(
                    onClick = {
                        if (montantSaisi != null && montantSaisi > 0) {

                            val selectedExpense = expenseToEdit

                            if (selectedExpense == null) {
                                // Ajouter une nouvelle dépense
                                viewModel.addExpense(
                                    Expense(amount = montantSaisi)
                                ){
                                    amount = ""
                                }
                            } else {
                                // Modifier la dépense sélectionnée
                                viewModel.updateExpense(
                                    selectedExpense.copy(
                                        amount = montantSaisi
                                    ),
                                    onSuccess = {
                                        amount = ""
                                        expenseToEdit = null
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (expenseToEdit == null) {
                            "Ajouter une dépense"
                        } else {
                            "Enregistrer la modification"
                        })
                }
            }

            is ExpenseUiState.Error -> {
                Text(
                    text = state.message
                )

                Button(
                    onClick = {
                        viewModel.loadExpenses()
                    }
                ) {
                    Text(text = "Réessayer")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExpenseTrackerTheme {
        HommeScreen()
    }
}