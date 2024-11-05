package com.example.uts_map

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
