package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreen1 : AppCompatActivity() {

  companion object {
    private const val SPLASH_DURATION = 3000L // 3 seconds
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash_screen1)

    Handler(Looper.getMainLooper())
        .postDelayed(
            {
              startActivity(Intent(this, OnboardingActivity::class.java))
              finish()
            },
            SPLASH_DURATION)
  }
}
