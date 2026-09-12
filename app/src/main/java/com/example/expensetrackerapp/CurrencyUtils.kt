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
    const val EXCHANGE_RATE_USD_TO_LKR = 328.42

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSelectedCurrency(context: Context): String =
        getPrefs(context).getString(KEY_CURRENCY, CURRENCY_LKR) ?: CURRENCY_LKR

    fun getCurrencySymbol(context: Context): String =
        if (getSelectedCurrency(context) == CURRENCY_USD) "$" else "Rs. "

    /**
     * Pure function for conversion testing without context.
     */
    fun convertFromBasePure(amountInLkr: Double, isUsd: Boolean): Double {
        return if (isUsd) {
            amountInLkr / EXCHANGE_RATE_USD_TO_LKR
        } else {
            amountInLkr
        }
    }

    /**
     * Converts an amount from base LKR to the currently selected currency numerical value.
     */
    fun convertFromBase(context: Context, amountInLkr: Double): Double {
        return convertFromBasePure(amountInLkr, getSelectedCurrency(context) == CURRENCY_USD)
    }

    /**
     * Pure function for conversion testing without context.
     */
    fun convertToBasePure(displayAmount: Double, isUsd: Boolean): Double {
        return if (isUsd) {
            displayAmount * EXCHANGE_RATE_USD_TO_LKR
        } else {
            displayAmount
        }
    }

    /**
     * Converts a user-entered amount in the currently selected currency back to base LKR for saving.
     */
    fun convertToBase(context: Context, displayAmount: Double): Double {
        return convertToBasePure(displayAmount, getSelectedCurrency(context) == CURRENCY_USD)
    }

    /**
     * Formats [amountInLkr] (which is always in base LKR) as a signed currency string.
     * @param isExpense if true, prepends "-"; if false, prepends "+"
     */
    fun format(context: Context, amountInLkr: Double, isExpense: Boolean? = null): String {
        val symbol = getCurrencySymbol(context)
        val convertedAmount = convertFromBase(context, amountInLkr)
        val formatted = String.format("%,.2f", convertedAmount)
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
