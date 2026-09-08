package com.example.expensetrackerapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Theme from SharedPreferences
        val prefs = CurrencyUtils.getPrefs(this)
        val savedTheme = prefs.getString("app_theme", "system") ?: "system"
        val nightMode = when (savedTheme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
