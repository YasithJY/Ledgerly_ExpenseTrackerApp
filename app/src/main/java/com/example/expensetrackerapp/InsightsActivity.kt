package com.example.expensetrackerapp

import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

class InsightsActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private var rangeDays = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val btnBack = findViewById<ImageButton>(R.id.btnBackInsights)
        btnBack.setOnClickListener { finish() }

        val btnRange7  = findViewById<TextView>(R.id.btnRange7)
        val btnRange30 = findViewById<TextView>(R.id.btnRange30)

        fun selectRange(days: Int, selected: TextView, other: TextView) {
            rangeDays = days
            selected.setBackgroundResource(R.drawable.bg_filter_pill_selected)
            selected.setTextColor(Color.WHITE)
            other.setBackgroundResource(R.drawable.bg_filter_pill_unselected)
            other.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            loadData()
        }

        btnRange7.setOnClickListener  { selectRange(7,  btnRange7,  btnRange30) }
        btnRange30.setOnClickListener { selectRange(30, btnRange30, btnRange7)  }

        // PDF button
        findViewById<Button>(R.id.btnDownloadPdf).setOnClickListener { generatePdf() }

        // Initial load
        loadData()
    }

    private fun loadData() {
        val endMillis   = System.currentTimeMillis()
        val startMillis = endMillis - (rangeDays * 24L * 60 * 60 * 1000)

        // Update date interval label
        val tvInterval = findViewById<TextView>(R.id.tvDateInterval)
        tvInterval.text = "${dateFormat.format(Date(startMillis))} – ${dateFormat.format(Date(endMillis))}"

        transactionViewModel.getTransactionsByDateRange(startMillis, endMillis)
        transactionViewModel.dateRangeTransactions.observe(this) { transactions ->
            val list = transactions ?: emptyList()
            renderStats(list)
        }
    }

    private fun renderStats(transactions: List<Transaction>) {
        val symbol = CurrencyUtils.getCurrencySymbol(this)

        val inflow  = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val outflow = transactions.filter { it.isExpense  }.sumOf { it.amount }
        val net     = inflow - outflow

        val tvInflow  = findViewById<TextView>(R.id.tvStatInflow)
        val tvOutflow = findViewById<TextView>(R.id.tvStatOutflow)
        val tvNet     = findViewById<TextView>(R.id.tvStatNet)

        tvInflow.text  = "$symbol${String.format("%,.2f", inflow)}"
        tvOutflow.text = "$symbol${String.format("%,.2f", outflow)}"
        tvNet.text     = "$symbol${String.format("%,.2f", net)}"
        tvNet.setTextColor(if (net >= 0) ContextCompat.getColor(this, R.color.income_green) else ContextCompat.getColor(this, R.color.expense_red))

        // Category breakdown
        renderCategoryBreakdown(transactions, outflow, symbol)

        // Ledger log entries
        renderLedgerLogs(transactions, symbol)
    }

    private fun renderCategoryBreakdown(
        transactions: List<Transaction>, totalOutflow: Double, symbol: String
    ) {
        val container = findViewById<LinearLayout>(R.id.categoryBreakdownContainer)
        container.removeAllViews()

        val expenses = transactions.filter { it.isExpense }
        val byCategory = expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }

        val inflater = LayoutInflater.from(this)

        for ((cat, amount) in byCategory) {
            val pct = if (totalOutflow > 0) (amount / totalOutflow * 100).toInt() else 0

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, dpToPx(12))
            }

            // Category name + amount + pct
            val labelRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val tvCat = TextView(this).apply {
                text = cat; textSize = 13f; setTextColor(ContextCompat.getColor(this@InsightsActivity, R.color.text_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvAmt = TextView(this).apply {
                text = "$symbol${String.format("%,.2f", amount)} ($pct%)"
                textSize = 12f; setTextColor(ContextCompat.getColor(this@InsightsActivity, R.color.text_secondary))
            }
            labelRow.addView(tvCat); labelRow.addView(tvAmt)

            // Progress bar
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100; progress = pct
                progressDrawable.setColorFilter(
                    android.graphics.PorterDuffColorFilter(ContextCompat.getColor(this@InsightsActivity, R.color.primary_blue),
                        android.graphics.PorterDuff.Mode.SRC_IN)
                )
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(8)).apply {
                    topMargin = dpToPx(4)
                }
            }

            row.addView(labelRow); row.addView(progressBar)
            container.addView(row)
        }

        if (byCategory.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No expenses in this period."
                textSize = 13f; setTextColor(ContextCompat.getColor(this@InsightsActivity, R.color.text_secondary)); gravity = android.view.Gravity.CENTER
            }
            container.addView(tv)
        }
    }

    private fun renderLedgerLogs(transactions: List<Transaction>, symbol: String) {
        val container = findViewById<LinearLayout>(R.id.ledgerLogsContainer)
        container.removeAllViews()

        for (tx in transactions) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8))
            }

            val tvNote = TextView(this).apply {
                text = "${tx.note}\n${tx.category} · ${dateFormat.format(Date(tx.date))}"
                textSize = 12f; setTextColor(ContextCompat.getColor(this@InsightsActivity, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val amtStr = if (tx.isExpense) "-$symbol${String.format("%,.2f", tx.amount)}"
                         else              "+$symbol${String.format("%,.2f", tx.amount)}"
            val tvAmt = TextView(this).apply {
                text = amtStr; textSize = 13f; setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(if (tx.isExpense) ContextCompat.getColor(this@InsightsActivity, R.color.expense_red) else ContextCompat.getColor(this@InsightsActivity, R.color.income_green))
            }
            row.addView(tvNote); row.addView(tvAmt)

            // Divider
            val divider = View(this).apply {
                setBackgroundColor(ContextCompat.getColor(this@InsightsActivity, R.color.border_color))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            }
            container.addView(row); container.addView(divider)
        }
    }

    private fun generatePdf() {
        val endMillis   = System.currentTimeMillis()
        val startMillis = endMillis - (rangeDays * 24L * 60 * 60 * 1000)
        val transactions = transactionViewModel.dateRangeTransactions.value ?: emptyList()
        val symbol = CurrencyUtils.getCurrencySymbol(this)

        val inflow  = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val outflow = transactions.filter { it.isExpense  }.sumOf { it.amount }
        val net     = inflow - outflow

        val rows = transactions.joinToString("") { tx ->
            val sign  = if (tx.isExpense) "-" else "+"
            val color = if (tx.isExpense) "#EF4444" else "#16A34A"
            "<tr><td>${tx.note}</td><td>${tx.category}</td><td>${dateFormat.format(Date(tx.date))}</td>" +
            "<td style='color:$color;font-weight:bold;'>$sign$symbol${String.format("%,.2f", tx.amount)}</td></tr>"
        }

        val html = """
            <html><head>
            <style>body{font-family:sans-serif;padding:20px;}
            h1{color:#2563EB;}table{width:100%;border-collapse:collapse;}
            th{background:#F1F5F9;padding:8px;text-align:left;}
            td{padding:8px;border-bottom:1px solid #E5E7EB;}</style>
            </head><body>
            <h1>ExpenseTracker App</h1>
            <h2>Statement of Account</h2>
            <p>${dateFormat.format(Date(startMillis))} – ${dateFormat.format(Date(endMillis))}</p>
            <table><tr>
              <td><b>Inflow</b><br><span style='color:#16A34A'>$symbol${String.format("%,.2f", inflow)}</span></td>
              <td><b>Outflow</b><br><span style='color:#EF4444'>$symbol${String.format("%,.2f", outflow)}</span></td>
              <td><b>Net</b><br><span style='color:${if(net>=0)"#16A34A" else "#EF4444"}'>$symbol${String.format("%,.2f", net)}</span></td>
            </tr></table><br>
            <table><tr><th>Note</th><th>Category</th><th>Date</th><th>Amount</th></tr>$rows</table>
            </body></html>
        """.trimIndent()

        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                val adapter = webView.createPrintDocumentAdapter("ExpenseStatement")
                printManager.print(
                    "ExpenseTracker_Statement",
                    adapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
