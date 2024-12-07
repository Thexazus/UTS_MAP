package com.example.uts_map

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        // Set top bar title if exists
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

        // Load user data from Firebase and display welcome message
        loadUserData()

        // Check daily goal achievement
        checkDailyGoalAchievement()

        // Example: Schedule an alarm for 1 hour from now
        setReminderAlarm(1)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        val userEmail = currentUser.email ?: return

        db.collection("users").document(userEmail).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val firstName = document.getString("firstName") ?: "User"
                Toast.makeText(this, "Welcome back, $firstName!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkDailyGoalAchievement() {
        val currentUser = auth.currentUser ?: return
        val userEmail = currentUser.email ?: return

        db.collection("users").document(userEmail).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentAmount = document.getDouble("todayAmount") ?: 0.0
                val targetAmount = document.getDouble("targetAmount") ?: 2000.0
                val lastCheckDate = document.getString("lastCheckDate") ?: ""

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (currentDate != lastCheckDate) {
                    val fragment = if (currentAmount >= targetAmount) {
                        AchieveDayGoalFragment()
                    } else {
                        NotAchieveFragment()
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null)
                        .commit()

                    // Update the last check date
                    db.collection("users").document(userEmail)
                        .update("lastCheckDate", currentDate)
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update daily check.", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Daily goal data not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to schedule an alarm reminder
    private fun setReminderAlarm(hoursFromNow: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, hoursFromNow) // Set alarm for 'hoursFromNow' hours from current time
        val alarmTimeInMillis = calendar.timeInMillis

        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("reminder_message", "Time to drink water!")
        }

        // Add FLAG_IMMUTABLE to PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE // Adding the FLAG_IMMUTABLE flag
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent)

        Toast.makeText(this, "Reminder set for ${calendar.time}", Toast.LENGTH_SHORT).show()
    }
}
