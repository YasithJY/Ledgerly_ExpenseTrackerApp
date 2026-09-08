package com.example.expensetrackerapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val TOTAL_DURATION_MS = 2400L
    private val TICK_INTERVAL_MS = 24L // 100 ticks total (0→100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val progressBar = findViewById<ProgressBar>(R.id.splashProgressBar)
        val tvStatus = findViewById<TextView>(R.id.tvSplashStatus)

        // Pulse animation for status text
        val pulseAnim = AlphaAnimation(0.3f, 1.0f).apply {
            duration = 600
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        tvStatus.startAnimation(pulseAnim)

        // Animate progress bar 0 → 100 over TOTAL_DURATION_MS
        val handler = Handler(Looper.getMainLooper())
        var currentProgress = 0

        val runnable = object : Runnable {
            override fun run() {
                if (currentProgress <= 100) {
                    progressBar.progress = currentProgress
                    currentProgress++
                    handler.postDelayed(this, TICK_INTERVAL_MS)
                } else {
                    // Progress complete — navigate to Dashboard
                    tvStatus.clearAnimation()
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        handler.post(runnable)
    }
}
