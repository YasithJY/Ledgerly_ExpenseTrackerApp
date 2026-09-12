package com.example.expensetrackerapp

import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    // Room executes all queries returning Flow on a background thread automatically
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()

    fun getMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): Flow<Double?> {
        return transactionDao.getMonthlyExpenses(startOfMonth, endOfMonth)
    }

    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): Flow<Double?> {
        return transactionDao.getMonthlyIncome(startOfMonth, endOfMonth)
    }

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun insertAll(transactions: List<Transaction>) {
        transactionDao.insertAll(transactions)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteAll() {
        transactionDao.deleteAll()
    }

    suspend fun getTransactionsByDateRange(startDate: Long, endDate: Long): List<Transaction> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    val allBnplTransactions: Flow<List<Transaction>> = transactionDao.getAllBnplTransactions()

    suspend fun getDueBnplInstallments(): List<Transaction> {
        return transactionDao.getDueBnplInstallments()
    }

    suspend fun activateDueInstallments(currentTime: Long) {
        transactionDao.activateDueInstallments(currentTime)
    }
}