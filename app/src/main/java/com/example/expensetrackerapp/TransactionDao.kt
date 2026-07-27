package com.example.expensetrackerapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // Gets all transactions ordered by the newest first
    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Calculates the total expenses
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 1")
    fun getTotalExpenses(): Flow<Double?>

    // Calculates the total income
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 0")
    fun getTotalIncome(): Flow<Double?>
}