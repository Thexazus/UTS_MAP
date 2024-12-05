package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreen1 : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 3000L // 3 seconds
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen1)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // Jika user belum login, arahkan ke OnboardingActivity
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            } else {
                // User sudah login, cek apakah data profil lengkap
                val userEmail = currentUser.email ?: ""
                db.collection("users").document(userEmail).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists() && document.data?.isNotEmpty() == true) {
                            // Data profil lengkap, ke MainActivity
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // Data profil belum lengkap, ke ProfileDetailActivity
                            startActivity(Intent(this, ProfileDetailActivity::class.java))
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        // Jika gagal mengambil data, arahkan ulang ke LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
            }
        }, SPLASH_DURATION)
    }
}
