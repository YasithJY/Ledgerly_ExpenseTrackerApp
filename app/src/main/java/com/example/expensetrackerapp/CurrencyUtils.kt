package com.example.expensetrackerapp

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility object for currency-aware amount formatting.
 * Reads the selected currency from SharedPreferences and
 * formats amounts with the appropriate symbol.
 *
 * Supported currencies:
 *   - LKR: "Rs. " prefix  (default)
 *   - USD: "$" prefix
 */
object CurrencyUtils {

    private const val PREFS_NAME = "expense_tracker_prefs"
    const val KEY_CURRENCY = "selected_currency"
    const val CURRENCY_LKR = "LKR"
    const val CURRENCY_USD = "USD"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedCurrency(context: Context): String =
        getPrefs(context).getString(KEY_CURRENCY, CURRENCY_LKR) ?: CURRENCY_LKR

    fun getCurrencySymbol(context: Context): String =
        if (getSelectedCurrency(context) == CURRENCY_USD) "$" else "Rs. "

    /**
     * Formats [amount] as a signed currency string.
     * @param isExpense if true, prepends "-"; if false, prepends "+"
     */
    fun format(context: Context, amount: Double, isExpense: Boolean? = null): String {
        val symbol = getCurrencySymbol(context)
        val formatted = String.format("%,.2f", amount)
        return when (isExpense) {
            true  -> "-$symbol$formatted"
            false -> "+$symbol$formatted"
            null  -> "$symbol$formatted"
        }
    }

    fun setSelectedCurrency(context: Context, currency: String) {
        getPrefs(context).edit().putString(KEY_CURRENCY, currency).apply()
    }
}
