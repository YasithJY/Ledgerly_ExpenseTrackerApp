package com.example.expensetrackerapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Transaction::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from v1 (no BNPL fields) → v2 (with BNPL fields)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN isBnpl INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN installmentNum INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN totalInstallments INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from v2 → v3 (added paymentMethod field)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN paymentMethod TEXT")
            }
        }

        // Migration from v3 → v4 (added isActivated flag for BNPL scheduled installments)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transaction_table ADD COLUMN isActivated INTEGER NOT NULL DEFAULT 1")
                // Retroactively fix existing BNPLs: if future due date, mark as inactive
                database.execSQL("UPDATE transaction_table SET isActivated = 0 WHERE isBnpl = 1 AND installmentNum > 1 AND date > (strftime('%s', 'now') * 1000)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}