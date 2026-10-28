package app.mosa.expensetracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Int,

    @ColumnInfo(defaultValue = "''")
    val description: String = "",

    @ColumnInfo(defaultValue = "0")
    val date: Long = System.currentTimeMillis(),

    @ColumnInfo(defaultValue = "'Autre'")
    val category: String = "Autre"
)