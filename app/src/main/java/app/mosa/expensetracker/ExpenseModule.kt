package app.mosa.expensetracker

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExpenseModule {

    @Binds
    abstract fun bindExpenseRepository(
        implementation: ExpenseRepositoryImpl
    ): ExpenseRepository

}