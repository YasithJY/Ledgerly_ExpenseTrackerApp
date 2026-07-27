package com.example.expensetrackerapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val etCategory = findViewById<TextInputEditText>(R.id.etCategory)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val radioExpense = findViewById<RadioButton>(R.id.radioExpense)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val calendar = Calendar.getInstance()
        var selectedDateMillis = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))

        etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            val category = etCategory.text.toString()
            val note = etNote.text.toString()
            val isExpense = radioExpense.isChecked

            if (amountText.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please enter an amount and category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull() ?: 0.0

            // Create the new transaction object
            val transaction = Transaction(
                amount = amount,
                category = category,
                note = note,
                date = selectedDateMillis,
                isExpense = isExpense
            )

            // Save it to the database
            transactionViewModel.insert(transaction)

            // Close this screen and go back to the dashboard
            finish()
        }
    }
}