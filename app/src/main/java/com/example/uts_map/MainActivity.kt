package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController

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

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupWithNavController(bottomNavigationView, navController)

        // Contoh implementasi tombol logout
//        val btnLogout = findViewById<Button>(R.id.btnLogout)
//        btnLogout.setOnClickListener {
//            SessionManager.logout(this)
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }
    }
}
