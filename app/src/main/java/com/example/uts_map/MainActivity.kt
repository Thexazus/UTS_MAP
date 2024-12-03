package com.example.uts_map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a welcome message with user email
        val userEmail = SessionManager.getUserEmail(this)
        userEmail?.let {
            Toast.makeText(this, "HALOOOOOOO, $userEmail", Toast.LENGTH_SHORT).show()
        }

        // Check if user is logged in
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Check if the Topbar exists and set its title
        val topBar = findViewById<Topbar?>(R.id.topbar)
        topBar?.setTitle("Dynamic Page Title")

        // Apply window insets for padding adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup navigation controller and bottom navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)

        // Check daily goal achievement
        checkDailyGoalAchievement()
    }

    private fun checkDailyGoalAchievement() {
        val prefs = getSharedPreferences("WaterTracker", Context.MODE_PRIVATE)
        val lastCheckDate = prefs.getString("lastCheckDate", "")
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Check if it's a new day
        if (currentDate != lastCheckDate) {
            val currentAmount = prefs.getFloat("todayAmount", 0f)
            val targetAmount = 2000f

            val fragment = if (currentAmount >= targetAmount) {
                // If the target is achieved, show AchieveDayGoalFragment
                AchieveDayGoalFragment()
            } else {
                // If the target is not achieved, show NotAchieveFragment
                NotAchieveFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment) // Use nav_host_fragment as the container
                .addToBackStack(null)
                .commit()

            // Update last check date
            prefs.edit().putString("lastCheckDate", currentDate).apply()
        }
    }
}
