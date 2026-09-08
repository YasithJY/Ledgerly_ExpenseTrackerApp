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

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // Gets all active transactions ordered by the newest first
    @Query("SELECT * FROM transaction_table WHERE isActivated = 1 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Gets ALL BNPL transactions (active and scheduled) for the BNPL view
    @Query("SELECT * FROM transaction_table WHERE isBnpl = 1 ORDER BY date DESC")
    fun getAllBnplTransactions(): Flow<List<Transaction>>

    // Calculates the total expenses (excluding future unpaid BNPL installments)
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 1 AND isActivated = 1 AND (isBnpl = 0 OR installmentNum = 1 OR date <= (strftime('%s', 'now') * 1000))")
    fun getTotalExpenses(): Flow<Double?>

    // Gets due BNPL installments for reminder notifications
    @Query("SELECT * FROM transaction_table WHERE isBnpl = 1 AND installmentNum > 1 AND date <= (strftime('%s', 'now') * 1000)")
    suspend fun getDueBnplInstallments(): List<Transaction>

    // Calculates the total income
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 0 AND isActivated = 1")
    fun getTotalIncome(): Flow<Double?>

    // Gets transactions within a date range (for Statement screen)
    @Query("SELECT * FROM transaction_table WHERE date BETWEEN :startDate AND :endDate AND isActivated = 1 ORDER BY date DESC")
    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction>

    // Calculates the total expenses for a specific month (excluding future unpaid BNPL installments)
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 1 AND isActivated = 1 AND date BETWEEN :startOfMonth AND :endOfMonth AND (isBnpl = 0 OR installmentNum = 1 OR date <= (strftime('%s', 'now') * 1000))")
    fun getMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Calculates the total income for a specific month
    @Query("SELECT SUM(amount) FROM transaction_table WHERE isExpense = 0 AND isActivated = 1 AND date BETWEEN :startOfMonth AND :endOfMonth")
    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    // Deletes all transactions (for Reset DB)
    @Query("DELETE FROM transaction_table")
    suspend fun deleteAll()

    // Count of rows (to detect first-run / empty state for seeding)
    @Query("SELECT COUNT(*) FROM transaction_table")
    suspend fun getCount(): Int

    // Activates scheduled BNPL installments whose due date has arrived
    @Query("UPDATE transaction_table SET isActivated = 1 WHERE isBnpl = 1 AND isActivated = 0 AND date <= :currentTime")
    suspend fun activateDueInstallments(currentTime: Long)
}