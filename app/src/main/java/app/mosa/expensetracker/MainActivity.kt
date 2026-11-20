package app.mosa.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.mosa.expensetracker.ui.theme.ExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

//Formaté la date
fun formatExpenseDate(timestamp: Long): String {
    if (timestamp == 0L) {
        return "Date inconnue"
    }

    val date = Date(timestamp)
    val formater = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return formater.format(date)
}

fun formatExpenseAmount(amount: Int): String {
    val formatter = NumberFormat.getIntegerInstance(Locale.FRANCE)

    return "${formatter.format(amount)} Ar"
}

//Correspondance : nom de catégorie -> symbole.
fun getCategoryIcon(category: String): String {
    return when (category) {
        "Alimentation" -> "🍽️"
        "Transport" -> "🚗"
        "Santé" -> "🩺"
        "Logement" -> "🏠"
        else -> "📦"
    }
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

    // Fonction pour sélectionner une dépense à modifier
    fun selectExpenseForEdit(expense: Expense) {
        expenseToEdit = expense
        amount = expense.amount.toString()
        description = expense.description
        selectedDate = expense.date
        category = expense.category
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerState = key(selectedDate) {
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
    }

    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "ExpenseTracker",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Suivis de vos dépenses",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (state) {
            is ExpenseUiState.Loading -> {
                Text(
                    text = "Chargement..."
                )
            }

            is ExpenseUiState.Success -> {

                val totalExpenses = state.expenses.sumOf { it.amount }

                //Résumé des dépenses
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    //Titre
                    Text(
                        text = "Total des dépenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    //Total
                    Text(
                        text = formatExpenseAmount(totalExpenses),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (state.expenses.isEmpty()) {
                    Text(
                        text = "Aucune dépense enregistrée. Ajoutez votre première dépense ! "
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.expenses.size) { index ->

                            val expense = state.expenses[index]

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (expenseToEdit?.id == expense.id) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (expenseToEdit?.id == expense.id) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    //AFFICHAGE
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectExpenseForEdit(expense)
                                            },
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        //Montant
                                        Text(
                                            text = formatExpenseAmount(expense.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        //Date
                                        Text(
                                            text = formatExpenseDate(expense.date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        //Description
                                        if (expense.description.isNotBlank()) {
                                            Text(
                                                text = expense.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        //Category
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            shape = RoundedCornerShape(50.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                //Icone
                                                Text(
                                                    text = getCategoryIcon(expense.category),
                                                    fontSize = 18.sp
                                                )

                                                //Nom categorie
                                                Text(
                                                    text = expense.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                    }

                                    //Icone Modifier
                                    FilledTonalIconButton(
                                        onClick = {
                                            selectExpenseForEdit(expense)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Modifier une dépense",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    //Icone Supprimer
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.deleteExpense(expense)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Supprimer cette dépense",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
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

                //Formulaire
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    // Organisation horizontale des boutons : DATE + CATEGORY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Bouton : date
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
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

                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            FilledTonalButton(
                                modifier = Modifier.fillMaxWidth(),
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
                    }

                    //Message d'erreur
                    if (messageErreur != null) {
                        Text(
                            text = messageErreur,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = 24.dp,
                            vertical = 16.dp
                        ),
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
                                    ) {
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (expenseToEdit == null) {
                                "Ajouter une dépense"
                            } else {
                                "Enregistrer la modification"
                            }
                        )
                    }
                }
            }

            is ExpenseUiState.Error -> {
                Text(
                    text = state.message
                )

                OutlinedButton(
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
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateChoisie = datePickerState.selectedDateMillis

                        if (dateChoisie != null) {
                            selectedDate = dateChoisie
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(
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