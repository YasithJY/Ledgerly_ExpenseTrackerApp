package com.example.expensetrackerapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        DynamicColors.applyToActivitiesIfAvailable(this)
        createNotificationChannel()
        
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BNPL Reminders"
            val descriptionText = "Notifications for BNPL installment reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("bnpl_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
