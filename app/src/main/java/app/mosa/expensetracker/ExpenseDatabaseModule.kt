package app.mosa.expensetracker

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpenseDatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(
        @ApplicationContext context: Context
    ): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        )
            .addMigrations(ExpenseDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideExpenseDao(
        database: ExpenseDatabase
    ): ExpenseDao {
        return database.expenseDao()
    }
}