package app.mosa.expensetracker

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Expense::class],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase: RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN description TEXT NOT NULL DEFAULT ''"
                )

                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN date INTEGER NOT NULL DEFAULT 0"
                )

                db.execSQL(
                    "ALTER TABLE expenses ADD COLUMN category TEXT NOT NULL DEFAULT 'Autre'"
                )
            }
        }
    }

}