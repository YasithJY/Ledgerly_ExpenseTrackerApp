package com.example.expensetrackerapp

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var adapter: TransactionAdapter
    private var allTransactionsList: List<Transaction> = emptyList()
    private var allBnplList: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        adapter = TransactionAdapter(this) { transaction ->
            handleTransactionClick(transaction)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val tvCount = findViewById<TextView>(R.id.tvTransactionCount)

        transactionViewModel.allTransactions.observe(this) { transactions ->
            allTransactionsList = transactions ?: emptyList()
            // Show only the 10 most recent transactions on the dashboard
            val recentList = allTransactionsList.take(10)
            adapter.submitList(recentList)
            tvCount.text = recentList.size.toString()
            
            val empty = recentList.isEmpty()
            tvEmptyState.visibility = if (empty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (empty) View.GONE else View.VISIBLE

            updateDashboardWidgets()
        }

        transactionViewModel.allBnplTransactions.observe(this) { list ->
            allBnplList = list ?: emptyList()
            updateUpcomingBnplWidget()
        }

        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val tvTotalIncome = findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)

        transactionViewModel.totalIncome.observe(this) { income ->
            tvTotalIncome.text = CurrencyUtils.format(this, income ?: 0.0)
            updateBalance(tvTotalBalance)
        }
        transactionViewModel.totalExpenses.observe(this) { expense ->
            tvTotalExpense.text = CurrencyUtils.format(this, expense ?: 0.0)
            updateBalance(tvTotalBalance)
        }

        setupMonthlySummary()
        setupQuickActions()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_ledger -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddTransactionActivity::class.java))
                    true
                }
                R.id.nav_insights -> {
                    startActivity(Intent(this, InsightsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        setupSwipeToDelete(recyclerView)

        transactionViewModel.checkDueBnplInstallments { dueList ->
            if (dueList.isNotEmpty()) {
                val prefs = CurrencyUtils.getPrefs(this)
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val lastCheckedDay = prefs.getString("last_checked_reminder_day", "")
                if (lastCheckedDay != todayStr) {
                    showBnplReminderDialog(dueList)
                    prefs.edit().putString("last_checked_reminder_day", todayStr).apply()
                }
            }
        }
    }

    private fun setupQuickActions() {
        findViewById<LinearLayout>(R.id.btnQuickExpense).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("TYPE", "EXPENSE")
            })
        }
        findViewById<LinearLayout>(R.id.btnQuickIncome).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("TYPE", "INCOME")
            })
        }
        findViewById<LinearLayout>(R.id.btnQuickScan).setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("OPEN_SCANNER", true)
            })
        }
        findViewById<LinearLayout>(R.id.btnQuickBnpl).setOnClickListener {
            startActivity(Intent(this, BnplSplitterActivity::class.java))
        }
    }

    private fun updateUpcomingBnplWidget() {
        val upcomingBnpl = allBnplList.filter { 
            it.isBnpl && !it.isActivated 
        }.minByOrNull { it.date }

        val tvUpcomingBnpl = findViewById<TextView>(R.id.tvUpcomingBnpl)
        if (upcomingBnpl != null) {
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(upcomingBnpl.date))
            tvUpcomingBnpl.text = "${upcomingBnpl.note} - ${CurrencyUtils.format(this, upcomingBnpl.amount)} due on $dateStr"
        } else {
            tvUpcomingBnpl.text = "No upcoming payments"
        }
    }

    private fun updateDashboardWidgets() {

        // Update Top Category (This Month)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis
        val monthlyExpenses = allTransactionsList.filter { it.isExpense && it.date >= startOfMonth }
        
        val tvTopCategory = findViewById<TextView>(R.id.tvTopCategory)
        if (monthlyExpenses.isNotEmpty()) {
            val categoryTotals = monthlyExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            
            val topCat = categoryTotals.maxByOrNull { it.value }
            if (topCat != null) {
                tvTopCategory.text = "${topCat.key}: ${CurrencyUtils.format(this, topCat.value)}"
            } else {
                tvTopCategory.text = "Not enough data"
            }
        } else {
            tvTopCategory.text = "Not enough data"
        }
    }

    private fun setupMonthlySummary() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        val tvMonthlyIncome = findViewById<TextView>(R.id.tvMonthlyIncome)
        val tvMonthlyExpense = findViewById<TextView>(R.id.tvMonthlyExpense)
        val tvSavingsPercent = findViewById<TextView>(R.id.tvSavingsPercent)

        var currentMonthlyIncome = 0.0
        var currentMonthlyExpense = 0.0

        fun updateSavingsPercent() {
            if (currentMonthlyIncome > 0) {
                val savings = currentMonthlyIncome - currentMonthlyExpense
                val percent = (savings / currentMonthlyIncome) * 100
                tvSavingsPercent.text = String.format(Locale.getDefault(), "%.1f%%", percent.coerceAtLeast(0.0))
            } else {
                tvSavingsPercent.text = "0%"
            }
        }

        transactionViewModel.getMonthlyIncome(startOfMonth, endOfMonth).observe(this) { inc ->
            currentMonthlyIncome = inc ?: 0.0
            tvMonthlyIncome.text = CurrencyUtils.format(this, currentMonthlyIncome)
            updateSavingsPercent()
        }

        transactionViewModel.getMonthlyExpenses(startOfMonth, endOfMonth).observe(this) { exp ->
            currentMonthlyExpense = exp ?: 0.0
            tvMonthlyExpense.text = CurrencyUtils.format(this, currentMonthlyExpense)
            updateSavingsPercent()
        }
    }

    override fun onResume() {
        super.onResume()
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val tvTotalIncome = findViewById<TextView>(R.id.tvTotalIncome)
        val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)
        transactionViewModel.totalIncome.value?.let {
            tvTotalIncome.text = CurrencyUtils.format(this, it)
        }
        transactionViewModel.totalExpenses.value?.let {
            tvTotalExpense.text = CurrencyUtils.format(this, it)
        }
        updateBalance(tvTotalBalance)
        adapter.notifyDataSetChanged()
        updateDashboardWidgets()
    }

    private fun updateBalance(tvBalance: TextView) {
        val income = transactionViewModel.totalIncome.value ?: 0.0
        val expense = transactionViewModel.totalExpenses.value ?: 0.0
        tvBalance.text = CurrencyUtils.format(this, income - expense)
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val deleteColor = ContextCompat.getColor(this@MainActivity, R.color.expense_red)
        val deletePaint = Paint().apply { color = deleteColor }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.RIGHT
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_ID.toInt()) {
                    val tx = adapter.currentList[position]
                    transactionViewModel.delete(tx)
                }
            }

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = vh.itemView
                val background = RectF(
                    itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, 16f, 16f, deletePaint)
                val textY = itemView.top + (itemView.height / 2f) + (textPaint.textSize / 3)
                c.drawText("DELETE", itemView.right - 32f, textY, textPaint)
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showBnplReminderDialog(dueList: List<Transaction>) {
        val count = dueList.size
        val message = StringBuilder("You have $count installment${if (count > 1) "s" else ""} due for payment:\n\n")
        for (tx in dueList) {
            val amountStr = CurrencyUtils.format(this, tx.amount)
            message.append("• ${tx.note}: $amountStr\n")
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Installment Reminder")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun handleTransactionClick(transaction: Transaction) {
        if (transaction.isBnpl && !transaction.isActivated) {
            val baseNote = getBaseNote(transaction.note)
            val earlierUnpaid = allBnplList.filter {
                it.isBnpl &&
                it.totalInstallments == transaction.totalInstallments &&
                getBaseNote(it.note) == baseNote &&
                it.installmentNum < transaction.installmentNum &&
                !it.isActivated
            }
            if (earlierUnpaid.isNotEmpty()) {
                val earliest = earlierUnpaid.minByOrNull { it.installmentNum }!!
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Installment Out of Order")
                    .setMessage("This installment has more time remaining. Please pay the earlier installment first (Installment ${earliest.installmentNum}/${earliest.totalInstallments}).")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Pay Installment Early")
                    .setMessage("Would you like to pay this installment early?\n\n${transaction.note}: ${CurrencyUtils.format(this, transaction.amount)}")
                    .setPositiveButton("Pay Early", null)
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.setOnShowListener {
                    val btn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    btn.setTextColor(ContextCompat.getColor(this, R.color.income_green))
                    btn.setOnClickListener {
                        val updated = transaction.copy(date = System.currentTimeMillis(), isActivated = true)
                        transactionViewModel.update(updated)
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
        }
    }

    private fun getBaseNote(note: String): String {
        val idx = note.indexOf(" (Installment ")
        if (idx != -1) return note.substring(0, idx)
        val idx2 = note.indexOf("Installment ")
        if (idx2 != -1) return note.substring(0, idx2).trim()
        return note
    }
}