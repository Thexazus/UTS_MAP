package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userEmail = SessionManager.getUserEmail(this)
        if (userEmail != null) {
            Toast.makeText(this, "HALOOOOOOO, $userEmail", Toast.LENGTH_SHORT).show()
        }


        // Cek apakah pengguna sudah login
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val topBar = findViewById<Topbar>(R.id.topbar)
        topBar.setTitle("Dynamic Page Title")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
                findViewById<BottomNavigationView>(R.id.bottomNavigation).setupWithNavController(navController)
            }
        }

        // Contoh implementasi tombol logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}