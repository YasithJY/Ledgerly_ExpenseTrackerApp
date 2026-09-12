package com.example.expensetrackerapp

import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import androidx.core.content.ContextCompat
import android.widget.AdapterView
import java.util.Calendar

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsActivity : AppCompatActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private var allTransactions: List<Transaction> = emptyList()
    private var allBnplTransactions: List<Transaction> = emptyList()
    private var currentFilter = "ALL"
    private var searchQuery = ""
    private var currentSort = 0 // 0: Newest, 1: Oldest, 2: Highest, 3: Lowest

    // Advanced filters
    private var minAmount: Double? = null
    private var maxAmount: Double? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var filterPaymentMethod: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)



        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLedger)
        adapter = TransactionAdapter(this) { transaction ->
            handleTransactionClick(transaction)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tvCount = findViewById<TextView>(R.id.tvLedgerCount)
        val emptyState = findViewById<LinearLayout>(R.id.emptySearchState)

        transactionViewModel.allTransactions.observe(this) { list ->
            allTransactions = list ?: emptyList()
            if (currentFilter != "BNPL") {
                applyFilterAndSearch(recyclerView, emptyState, tvCount)
            }
        }

        transactionViewModel.allBnplTransactions.observe(this) { list ->
            allBnplTransactions = list ?: emptyList()
            if (currentFilter == "BNPL") {
                applyFilterAndSearch(recyclerView, emptyState, tvCount)
            }
        }

        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilterAndSearch(recyclerView, emptyState, tvCount)
            }
        })

        setupFilters(recyclerView, emptyState, tvCount)
        setupSortingAndAdvancedFilters(recyclerView, emptyState, tvCount)

        findViewById<ImageButton>(R.id.btnBackLedger).setOnClickListener { finish() }
        setupSwipeToDelete(recyclerView)
    }

    private fun setupSortingAndAdvancedFilters(recyclerView: RecyclerView, emptyState: View, tvCount: TextView) {
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)
        val sortOptions = listOf("Newest First", "Oldest First", "Highest Amount", "Lowest Amount")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = spinnerAdapter

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = position
                applyFilterAndSearch(recyclerView, emptyState, tvCount)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnAdvancedFilters).setOnClickListener {
            // Placeholder for full advanced filter dialog
            // We would show a custom dialog here. For now we will just show a toast indicating it's coming in Phase 1 polish
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Advanced Filters")
                .setMessage("Advanced filtering by Date Range, Amount Range, and Payment Method will be enabled in the final polish.")
                .setPositiveButton("Close", null)
                .show()
        }
    }

    private fun applyFilterAndSearch(
        recyclerView: RecyclerView,
        emptyState: View,
        tvCount: TextView
    ) {
        val sourceList = if (currentFilter == "BNPL") allBnplTransactions else allTransactions

        var filtered = sourceList.filter { tx ->
            val matchesFilter = when (currentFilter) {
                "INCOME"  -> !tx.isExpense
                "EXPENSE" -> tx.isExpense
                "BNPL"    -> true
                else      -> true
            }
            val matchesSearch = if (searchQuery.isEmpty()) true else
                tx.note.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                (tx.paymentMethod != null && tx.paymentMethod.contains(searchQuery, ignoreCase = true))

            val matchesMin = minAmount == null || tx.amount >= minAmount!!
            val matchesMax = maxAmount == null || tx.amount <= maxAmount!!
            val matchesStart = startDate == null || tx.date >= startDate!!
            val matchesEnd = endDate == null || tx.date <= endDate!!
            val matchesPm = filterPaymentMethod == null || tx.paymentMethod == filterPaymentMethod

            matchesFilter && matchesSearch && matchesMin && matchesMax && matchesStart && matchesEnd && matchesPm
        }

        filtered = when (currentSort) {
            0 -> filtered.sortedByDescending { it.date }
            1 -> filtered.sortedBy { it.date }
            2 -> filtered.sortedByDescending { it.amount }
            3 -> filtered.sortedBy { it.amount }
            else -> filtered
        }

        adapter.submitList(filtered)
        tvCount.text = "LEDGER RECORDS (${filtered.size} items)"
        val isEmpty = filtered.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun setupFilters(recyclerView: RecyclerView, emptyState: View, tvCount: TextView) {
        val filterAll     = findViewById<TextView>(R.id.ledgerFilterAll)
        val filterIncome  = findViewById<TextView>(R.id.ledgerFilterIncome)
        val filterExpense = findViewById<TextView>(R.id.ledgerFilterExpense)
        val filterBnpl    = findViewById<TextView>(R.id.ledgerFilterBnpl)
        val pills = listOf(filterAll, filterIncome, filterExpense, filterBnpl)

        fun selectPill(selected: TextView) {
            pills.forEach { pill ->
                if (pill == selected) {
                    pill.setBackgroundResource(R.drawable.bg_filter_pill_selected)
                    pill.setTextColor(Color.WHITE)
                } else {
                    pill.setBackgroundResource(R.drawable.bg_filter_pill_unselected)
                    pill.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
            }
        }

        filterAll.setOnClickListener { currentFilter = "ALL"; selectPill(filterAll); applyFilterAndSearch(recyclerView, emptyState, tvCount) }
        filterIncome.setOnClickListener { currentFilter = "INCOME"; selectPill(filterIncome); applyFilterAndSearch(recyclerView, emptyState, tvCount) }
        filterExpense.setOnClickListener { currentFilter = "EXPENSE"; selectPill(filterExpense); applyFilterAndSearch(recyclerView, emptyState, tvCount) }
        filterBnpl.setOnClickListener { currentFilter = "BNPL"; selectPill(filterBnpl); applyFilterAndSearch(recyclerView, emptyState, tvCount) }
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val deletePaint = Paint().apply { color = ContextCompat.getColor(this@TransactionsActivity, R.color.expense_red) }
        val textPaint = Paint().apply {
            color = Color.WHITE; textSize = 40f; textAlign = Paint.Align.RIGHT
        }
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    viewHolder.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.REJECT)
                } else {
                    viewHolder.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                }
                val tx = adapter.currentList[viewHolder.bindingAdapterPosition]
                transactionViewModel.delete(tx)
            }
            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, active: Boolean) {
                val v = vh.itemView
                c.drawRoundRect(RectF(v.right + dX, v.top.toFloat(), v.right.toFloat(),
                    v.bottom.toFloat()), 16f, 16f, deletePaint)
                c.drawText("DELETE", v.right - 32f,
                    v.top + v.height / 2f + textPaint.textSize / 3, textPaint)
                super.onChildDraw(c, rv, vh, dX, dY, actionState, active)
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun handleTransactionClick(transaction: Transaction) {
        if (transaction.isBnpl && !transaction.isActivated) {
            val baseNote = getBaseNote(transaction.note)
            val earlierUnpaid = allBnplTransactions.filter {
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
