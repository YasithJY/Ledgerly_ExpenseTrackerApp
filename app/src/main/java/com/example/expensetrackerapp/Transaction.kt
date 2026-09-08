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
    val date: Long, // stored as timestamp millis for easy sorting
    val isExpense: Boolean, // true = Expense, false = Income
    val isBnpl: Boolean = false, // true if part of a BNPL installment series
    val installmentNum: Int = 0, // e.g. 1, 2, 3 ...
    val totalInstallments: Int = 0, // e.g. 3 (total in series)
    val paymentMethod: String? = null, // e.g. "Cash", "Bank", "Credit Card"
    val isActivated: Boolean = true // true for standard/active, false for future scheduled BNPL
)