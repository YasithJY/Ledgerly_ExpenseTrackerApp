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

class SettingsActivity : AppCompatActivity() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

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
        switchReminders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminders_enabled", isChecked).apply()
        }

        val switchBiometric = findViewById<SwitchCompat>(R.id.switchBiometric)
        switchBiometric.isChecked = prefs.getBoolean("biometric_enabled", false)
        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("biometric_enabled", isChecked).apply()
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
}
