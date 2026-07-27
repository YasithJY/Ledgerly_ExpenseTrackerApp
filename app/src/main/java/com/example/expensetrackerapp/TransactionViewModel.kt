package com.example.expensetrackerapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    // LiveData gives us real-time updates to display in our UI
    val allTransactions: LiveData<List<Transaction>>
    val totalExpenses: LiveData<Double?>
    val totalIncome: LiveData<Double?>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)

        // Convert the Flow from Room into LiveData
        allTransactions = repository.allTransactions.asLiveData()
        totalExpenses = repository.totalExpenses.asLiveData()
        totalIncome = repository.totalIncome.asLiveData()
    }

    // viewModelScope ensures this runs safely in the background
    fun insert(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(transaction)
    }
}