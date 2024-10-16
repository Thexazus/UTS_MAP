package com.example.uts_map

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen1 : AppCompatActivity() {
    companion object {
        private const val SPLASH_DURATION = 3000L // 3 seconds
    }

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen1)

        databaseHelper = DatabaseHelper(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (SessionManager.isLoggedIn(this)) {
                val userEmail = SessionManager.getUserEmail(this)
                if (userEmail != null) {
                    // Jika sudah login dan profil lengkap, langsung ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Jika sudah login tapi profil belum lengkap, ke ProfileDetailActivity
                    startActivity(Intent(this, ProfileDetailActivity::class.java))
                }
            } else {
                // Jika belum login, ke OnboardingActivity
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
        }, SPLASH_DURATION)
    }
}