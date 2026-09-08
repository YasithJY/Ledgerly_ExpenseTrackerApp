package com.example.expensetrackerapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.graphics.Color
import android.content.res.ColorStateList
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class AddTransactionActivity : AppCompatActivity(), OcrScannerDialog.OcrResultCallback {

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var etAmount: TextInputEditText
    private var selectedCategory: String = ""
    private lateinit var categoryButtons: List<com.google.android.material.button.MaterialButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        findViewById<ImageButton>(R.id.btnBackAddTx).setOnClickListener { finish() }

        etAmount = findViewById(R.id.etAmount)
        val etCategory = findViewById<TextInputEditText>(R.id.etCategory)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val radioExpense = findViewById<RadioButton>(R.id.radioExpense)
        val radioIncome = findViewById<RadioButton>(R.id.radioIncome)
        val cbSplitPayment = findViewById<CheckBox>(R.id.cbSplitPayment)
        val tilSplitMonths = findViewById<TextInputLayout>(R.id.tilSplitMonths)
        val etSplitMonths = findViewById<TextInputEditText>(R.id.etSplitMonths)
        val btnScanReceipt = findViewById<Button>(R.id.btnScanReceipt)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val etPaymentMethod = findViewById<AutoCompleteTextView>(R.id.etPaymentMethod)

        val paymentMethods = listOf("Cash", "Bank", "Debit Card", "Credit Card", "Online Payment", "Other")
        val pmAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, paymentMethods)
        etPaymentMethod.setAdapter(pmAdapter)

        btnScanReceipt.setOnClickListener {
            OcrScannerDialog().show(supportFragmentManager, "OcrScanner")
        }

        cbSplitPayment.setOnCheckedChangeListener { _, isChecked ->
            tilSplitMonths.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

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
        setupCategoryButtons()
        selectCategory("Food")

        val passedType = intent.getStringExtra("TYPE")
        if (passedType == "INCOME") {
            radioIncome.isChecked = true
            selectCategory("Salary")
        } else if (passedType == "EXPENSE") {
            radioExpense.isChecked = true
        }
        
        val openScanner = intent.getBooleanExtra("OPEN_SCANNER", false)
        if (openScanner) {
            OcrScannerDialog().show(supportFragmentManager, "OcrScanner")
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString()
            val category = etCategory.text.toString()
            val note = etNote.text.toString()
            val isExpense = radioExpense.isChecked
            val isSplit = cbSplitPayment.isChecked
            val paymentMethodText = etPaymentMethod.text.toString()
            val paymentMethod = if (paymentMethodText.isNotEmpty()) paymentMethodText else null

            val amount = amountText.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                Toast.makeText(this, "Please select or enter a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isSplit) {
                val monthsText = etSplitMonths.text.toString()
                val months = monthsText.toIntOrNull()
                if (months == null || months <= 0) {
                    Toast.makeText(this, "Please enter a valid number of months", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val baseInstallment = Math.floor((amount / months) * 100) / 100
                val remainder = amount - (baseInstallment * months)
                val firstInstallment = baseInstallment + remainder

                val transactionsList = ArrayList<Transaction>()
                val tempCalendar = Calendar.getInstance()
                tempCalendar.timeInMillis = selectedDateMillis

                for (i in 0 until months) {
                    if (i > 0) {
                        tempCalendar.add(Calendar.MONTH, 1)
                    }
                    val installAmt = if (i == 0) firstInstallment else baseInstallment
                    val transaction = Transaction(
                        amount = installAmt,
                        category = category,
                        note = if (note.isEmpty()) "BNPL Installment ${i + 1}/$months" else "$note (Installment ${i + 1}/$months)",
                        date = tempCalendar.timeInMillis,
                        isExpense = isExpense,
                        isBnpl = true,
                        installmentNum = i + 1,
                        totalInstallments = months,
                        paymentMethod = paymentMethod,
                        isActivated = (i == 0)
                    )
                    transactionsList.add(transaction)
                }

                transactionViewModel.insertAll(transactionsList)
            } else {
                val transaction = Transaction(
                    amount = amount,
                    category = category,
                    note = note,
                    date = selectedDateMillis,
                    isExpense = isExpense,
                    paymentMethod = paymentMethod
                )
                transactionViewModel.insert(transaction)
            }

            finish()
        }
    }

    override fun onOcrResult(amount: Double, store: String, date: String) {
        etAmount.setText(amount.toString())
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        if (store.isNotEmpty()) {
            etNote.setText(store)
        }
    }

    private fun setupCategoryButtons() {
        categoryButtons = listOf(
            findViewById(R.id.btnCatFood),
            findViewById(R.id.btnCatGrocery),
            findViewById(R.id.btnCatShopping),
            findViewById(R.id.btnCatSalary),
            findViewById(R.id.btnCatUtilities),
            findViewById(R.id.btnCatTransport),
            findViewById(R.id.btnCatEntertainment),
            findViewById(R.id.btnCatOther)
        )

        for (btn in categoryButtons) {
            btn.setOnClickListener {
                selectCategory(btn.text.toString())
            }
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        val etCategory = findViewById<TextInputEditText>(R.id.etCategory)
        val tilCategory = findViewById<TextInputLayout>(R.id.tilCategory)

        etCategory.setText(category)
        if (category.equals("Other", ignoreCase = true)) {
            tilCategory.visibility = View.VISIBLE
        } else {
            tilCategory.visibility = View.GONE
        }

        for (btn in categoryButtons) {
            if (btn.text.toString().equals(category, ignoreCase = true)) {
                btn.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
                btn.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_blue))
                btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.selection_blue))
                btn.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                // Determine if we are in night mode to set text secondary
                val textSecondary = ContextCompat.getColor(this, R.color.text_secondary)
                val borderCol = ContextCompat.getColor(this, R.color.border_color)
                
                btn.setTextColor(textSecondary)
                btn.strokeColor = ColorStateList.valueOf(borderCol)
                btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                btn.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
}