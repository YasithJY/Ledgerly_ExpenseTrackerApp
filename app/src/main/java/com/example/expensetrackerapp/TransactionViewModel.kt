package com.example.expensetrackerapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    // LiveData gives us real-time updates to display in our UI
    val allTransactions: LiveData<List<Transaction>>
    val allBnplTransactions: LiveData<List<Transaction>>
    val totalExpenses: LiveData<Double?>
    val totalIncome: LiveData<Double?>

    private val _dateRangeTransactions = MutableLiveData<List<Transaction>>()
    val dateRangeTransactions: LiveData<List<Transaction>> = _dateRangeTransactions

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)

        // Convert the Flow from Room into LiveData
        allTransactions = repository.allTransactions.asLiveData()
        allBnplTransactions = repository.allBnplTransactions.asLiveData()
        totalExpenses = repository.totalExpenses.asLiveData()
        totalIncome = repository.totalIncome.asLiveData()

        // Automatically activate any scheduled BNPL installments whose due date has arrived
        viewModelScope.launch(Dispatchers.IO) {
            repository.activateDueInstallments(System.currentTimeMillis())
        }
    }

    fun getMonthlyExpenses(startOfMonth: Long, endOfMonth: Long): LiveData<Double?> {
        return repository.getMonthlyExpenses(startOfMonth, endOfMonth).asLiveData()
    }

    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): LiveData<Double?> {
        return repository.getMonthlyIncome(startOfMonth, endOfMonth).asLiveData()
    }

    // viewModelScope ensures this runs safely in the background
    fun insert(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction)
    }

    fun insertAll(transactions: List<Transaction>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAll(transactions)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }

    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getTransactionsByDateRange(startDate, endDate)
            _dateRangeTransactions.postValue(result)
        }
    }

    fun checkDueBnplInstallments(callback: (List<Transaction>) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.getDueBnplInstallments()
        launch(Dispatchers.Main) {
            callback(result)
        }
    }
}