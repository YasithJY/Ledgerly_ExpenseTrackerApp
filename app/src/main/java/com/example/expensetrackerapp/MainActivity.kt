package com.example.expensetrackerapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Setup the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. Initialize the ViewModel
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        // 3. Observe the list of transactions
        transactionViewModel.allTransactions.observe(this) { transactions ->
            // This updates the RecyclerView automatically whenever the database changes
            transactions?.let { adapter.submitList(it) }
        }

        // 4. Setup Balance Views and formatting
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val tvTotalIncome = findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)
        val format = NumberFormat.getCurrencyInstance(Locale.US)

        // 5. Observe Income and Expenses to update the dashboard cards
        transactionViewModel.totalIncome.observe(this) { income ->
            val currentIncome = income ?: 0.0
            tvTotalIncome.text = format.format(currentIncome)
            updateTotalBalance(tvTotalBalance, format)
        }

        transactionViewModel.totalExpenses.observe(this) { expense ->
            val currentExpense = expense ?: 0.0
            tvTotalExpense.text = format.format(currentExpense)
            updateTotalBalance(tvTotalBalance, format)
        }

        // 6. Setup the Floating Action Button to add new items
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddTransaction)
        fabAdd.setOnClickListener {
            val intent = android.content.Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    // Helper function to calculate and update the main balance
    private fun updateTotalBalance(tvTotalBalance: TextView, format: NumberFormat) {
        val income = transactionViewModel.totalIncome.value ?: 0.0
        val expense = transactionViewModel.totalExpenses.value ?: 0.0
        val balance = income - expense
        tvTotalBalance.text = format.format(balance)
    }
}