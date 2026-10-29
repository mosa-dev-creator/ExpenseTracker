package app.mosa.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.key

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

fun formatExpenseDate(timestamp: Long): String {
    if (timestamp == 0L) {
        return "Date inconnue"
    }

    val date = Date(timestamp)
    val formater = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return formater.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HommeScreen(
    modifier: Modifier = Modifier
) {

    val viewModel: ExpenseViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
    }

    var amount by remember { mutableStateOf(value = "") }
    var description by remember { mutableStateOf(value = "") }
    var category by remember { mutableStateOf(value = "Autre") }
    var expenseToEdit by remember { mutableStateOf<Expense?>(value = null) }

    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerState = key(selectedDate) {
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
    }

    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    Column(
        modifier = modifier.fillMaxSize(),
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

                //Titre
                Text(
                    text = "Total des dépenses",
                    fontWeight = FontWeight.Bold
                )

                //Total
                Text(
                    text = totalExpenses.toString()
                )

                if (state.expenses.isEmpty()) {
                    Text(
                        text = "Aucune dépense enregistrée. Ajoutez votre première dépense ! "
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.expenses.size){ index ->

                            val expense = state.expenses[index]

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //AFFICHAGE
                                Column(
                                    modifier = Modifier.clickable {
                                        expenseToEdit = expense
                                        amount = expense.amount.toString()
                                        description = expense.description
                                        selectedDate = expense.date
                                        category = expense.category
                                    }
                                ) {
                                    //Montant
                                    Text(
                                        text = expense.amount.toString(),
                                    )

                                    //Date
                                    Text(
                                        text = formatExpenseDate(expense.date),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    //Description
                                    if (expense.description.isNotBlank()){
                                        Text(
                                            text = expense.description,
                                            fontSize = 12.sp
                                        )
                                    }

                                    //Category
                                    Text(
                                        text = "Catégorie : ${expense.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }


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

                //Champ : montant
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    label = {
                        Text("Montant de la dépense")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                //Champ : description
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text("Description de la dépense")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                //Bouton : date
                Button(
                    onClick = {
                        showDatePicker = true
                    }
                ) {
                    Text("Choisir la date : ${formatExpenseDate(selectedDate)}")
                }

                //Champ : category
                var categoryMenuExpanded by remember { mutableStateOf(false) }

                val categories = listOf(
                    "Alimentation",
                    "Transport",
                    "Santé",
                    "Logement",
                    "Autre"
                )

                Box {
                    Button(
                        onClick = {
                            categoryMenuExpanded = true
                        }
                    ) {
                        Text("Categorie : $category")
                    }

                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = {
                            categoryMenuExpanded = false
                        }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },
                                onClick = {
                                    category = item
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                //fin : champ category

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
                                    Expense(
                                        amount = montantSaisi,
                                        description = description,
                                        date = selectedDate,
                                        category = category
                                    )
                                ){
                                    amount = ""
                                    description = ""
                                    selectedDate = System.currentTimeMillis()
                                    category = "Autre"
                                }
                            } else {
                                // Modifier la dépense sélectionnée
                                viewModel.updateExpense(
                                    selectedExpense.copy(
                                        amount = montantSaisi,
                                        description = description,
                                        date = selectedDate,
                                        category = category
                                    ),
                                    onSuccess = {
                                        amount = ""
                                        description = ""
                                        category = "Autre"
                                        selectedDate = System.currentTimeMillis()
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

    //DatepickerDialog
    if (showDatePicker){
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateChoisie = datePickerState.selectedDateMillis

                        if (dateChoisie != null){
                            selectedDate = dateChoisie
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
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