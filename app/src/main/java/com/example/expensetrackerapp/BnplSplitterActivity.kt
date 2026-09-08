package com.example.expensetrackerapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class BnplSplitterActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bnpl_splitter)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val btnBack = findViewById<ImageButton>(R.id.btnBackBnpl)
        val etTitle = findViewById<TextInputEditText>(R.id.etBnplTitle)
        val etAmount = findViewById<TextInputEditText>(R.id.etBnplAmount)
        val etMonths = findViewById<TextInputEditText>(R.id.etBnplMonths)
        val btnCalculate = findViewById<Button>(R.id.btnCalculateBnpl)

        btnBack.setOnClickListener { finish() }

        btnCalculate.setOnClickListener {
            val title = etTitle.text.toString().ifEmpty { "BNPL Purchase" }
            val amountText = etAmount.text.toString()
            val monthsText = etMonths.text.toString()

            val amount = amountText.toDoubleOrNull()
            val months = monthsText.toIntOrNull()

            if (amount == null || amount <= 0 || months == null || months <= 0) {
                Toast.makeText(this, "Please enter valid total amount and installment months", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val splitAmount = amount / months
            val transactionsList = ArrayList<Transaction>()
            val calendar = Calendar.getInstance()

            for (i in 0 until months) {
                if (i > 0) {
                    calendar.add(Calendar.MONTH, 1)
                }
                val transaction = Transaction(
                    amount = splitAmount,
                    category = "BNPL Splitter",
                    note = "$title (Installment ${i + 1}/$months)",
                    date = calendar.timeInMillis,
                    isExpense = true,
                    isBnpl = true,
                    installmentNum = i + 1,
                    totalInstallments = months,
                    isActivated = (calendar.timeInMillis <= System.currentTimeMillis())
                )
                transactionsList.add(transaction)
            }

            transactionViewModel.insertAll(transactionsList)
            Toast.makeText(this, "Scheduled $months BNPL installments successfully!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
