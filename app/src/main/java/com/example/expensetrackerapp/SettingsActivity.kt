package com.example.expensetrackerapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // ─── Back button ──────────────────────────────────────────────────
        findViewById<ImageButton>(R.id.btnBackSettings).setOnClickListener { finish() }

        // ─── Currency selection ────────────────────────────────────────────
        val checkLkr   = findViewById<TextView>(R.id.checkLkr)
        val checkUsd   = findViewById<TextView>(R.id.checkUsd)
        val rowLkr     = findViewById<View>(R.id.rowCurrencyLkr)
        val rowUsd     = findViewById<View>(R.id.rowCurrencyUsd)

        // Reflect current saved currency
        val currentCurrency = CurrencyUtils.getSelectedCurrency(this)
        if (currentCurrency == CurrencyUtils.CURRENCY_USD) {
            checkLkr.visibility = View.GONE
            checkUsd.visibility = View.VISIBLE
        } else {
            checkLkr.visibility = View.VISIBLE
            checkUsd.visibility = View.GONE
        }

        rowLkr.setOnClickListener {
            CurrencyUtils.setSelectedCurrency(this, CurrencyUtils.CURRENCY_LKR)
            checkLkr.visibility = View.VISIBLE
            checkUsd.visibility = View.GONE
        }
        rowUsd.setOnClickListener {
            CurrencyUtils.setSelectedCurrency(this, CurrencyUtils.CURRENCY_USD)
            checkLkr.visibility = View.GONE
            checkUsd.visibility = View.VISIBLE
        }

        // ─── Toggles (save to SharedPreferences) ──────────────────────────
        val prefs = CurrencyUtils.getPrefs(this)

        val switchReminders = findViewById<SwitchCompat>(R.id.switchReminders)
        switchReminders.isChecked = prefs.getBoolean("reminders_enabled", false)

        val requestPermissionLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                scheduleReminderWork()
            } else {
                Toast.makeText(this, "Permission denied, reminders will not work in the background", Toast.LENGTH_LONG).show()
                switchReminders.isChecked = false
                prefs.edit().putBoolean("reminders_enabled", false).apply()
            }
        }

        switchReminders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminders_enabled", isChecked).apply()
            if (isChecked) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        scheduleReminderWork()
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    scheduleReminderWork()
                }
            } else {
                androidx.work.WorkManager.getInstance(this).cancelUniqueWork("bnpl_reminder_work")
            }
        }




        // ─── Theme selection ───────────────────────────────────────────────
        val spinnerTheme = findViewById<android.widget.Spinner>(R.id.spinnerTheme)
        val savedTheme = prefs.getString("app_theme", "system") ?: "system"
        val initialIndex = when (savedTheme) {
            "light" -> 1
            "dark"  -> 2
            else    -> 0
        }
        spinnerTheme.setSelection(initialIndex)
        spinnerTheme.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val themeVal = when (position) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                if (prefs.getString("app_theme", "system") != themeVal) {
                    prefs.edit().putString("app_theme", themeVal).apply()
                    val nightMode = when (themeVal) {
                        "light" -> AppCompatDelegate.MODE_NIGHT_NO
                        "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
                        else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // ─── How to Use ───────────────────────────────────────────────────
        findViewById<Button>(R.id.btnHowToUse).setOnClickListener {
            val instructions = """
                Welcome to ExpenseTracker!

                • Adding Transactions: Tap '+' on the dashboard to log your Income or Expenses. You can categorize them and select payment methods.
                
                • OCR Scanner: Don't want to type? Tap the scanner icon when adding an expense to snap a picture of your receipt. It automatically detects the total amount.
                
                • Dynamic Currency: Switch between LKR and USD in Settings. The app seamlessly converts your dashboard and dynamically calculates scanned receipts based on your current setting (1 USD = 328.42 LKR).
                
                • BNPL Splitter: Found a Buy-Now-Pay-Later deal? The BNPL Splitter divides the total cost into equal monthly installments and reminds you when they're due!
                
                • Insights & Statements: Head to the Insights tab to see your category breakdown, track your net savings, and download a PDF Statement of Account.
            """.trimIndent()

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("How to Use ExpenseTracker")
                .setMessage(instructions)
                .setPositiveButton("Got it!", null)
                .show()
        }

        // ─── Reset DB ─────────────────────────────────────────────────────
        val cardResetConfirm = findViewById<View>(R.id.cardResetConfirm)
        val tvResetConfirmMsg = findViewById<TextView>(R.id.tvResetConfirmMsg)
        val btnResetDatabase = findViewById<Button>(R.id.btnResetDatabase)
        val btnCancelReset   = findViewById<Button>(R.id.btnCancelReset)
        val btnConfirmReset  = findViewById<Button>(R.id.btnConfirmReset)

        btnResetDatabase.setOnClickListener {
            val transactions = transactionViewModel.allTransactions.value ?: emptyList()
            val hasActiveBnpl = transactions.any { it.isBnpl }
            if (hasActiveBnpl) {
                tvResetConfirmMsg.setText(R.string.reset_confirm_bnpl_warning)
            } else {
                tvResetConfirmMsg.setText(R.string.reset_confirm_msg)
            }
            cardResetConfirm.visibility = View.VISIBLE
        }
        btnCancelReset.setOnClickListener {
            cardResetConfirm.visibility = View.GONE
        }
        btnConfirmReset.setOnClickListener {
            transactionViewModel.deleteAll()
            cardResetConfirm.visibility = View.GONE
            Toast.makeText(this, "Database cleared successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleReminderWork() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.expensetrackerapp.worker.BnplReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "bnpl_reminder_work",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Toast.makeText(this, "Background Reminders enabled!", Toast.LENGTH_SHORT).show()
    }
}
