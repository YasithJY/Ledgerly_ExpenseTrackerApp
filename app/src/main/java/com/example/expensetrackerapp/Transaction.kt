package com.example.expensetrackerapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long, // We store dates as Long (timestamps) for easy sorting
    val isExpense: Boolean // true if it's an Expense, false if it's Income
)