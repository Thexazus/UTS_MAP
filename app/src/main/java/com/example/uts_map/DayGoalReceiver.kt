package com.example.uts_map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DayGoalReceiver : BroadcastReceiver() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onReceive(context: Context?, intent: Intent?) {
        val activity = context as? FragmentActivity ?: return

        if (intent?.action == "com.example.uts_map.DAILY_GOAL_ACHIEVED") {
            showAchievedGoalFragment(activity)
            return // Exit after handling this specific action
        }

        checkAndHandleDailyGoal(activity)
    }

    private fun checkAndHandleDailyGoal(activity: FragmentActivity) {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email

        if (currentUser == null || userEmail == null) {
            Toast.makeText(activity, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        fetchGoalDataAndShowFragment(activity, userEmail)
    }

    private fun fetchGoalDataAndShowFragment(activity: FragmentActivity, userEmail: String) {
        db.collection("users").document(userEmail).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    handleGoalData(activity, document, userEmail)
                } else {
                    Toast.makeText(activity, "Daily goal data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun handleGoalData(activity: FragmentActivity, document: DocumentSnapshot, userEmail: String) {
        val todayProgress = document.getDouble("todayAmount") ?: 0.0
        val dailyGoal = document.getDouble("targetAmount") ?: 2000.0
        Log.d("DailyGoalReceiver", "Today's Progress: $todayProgress, Daily Goal: $dailyGoal")

        val progressPercentage = (todayProgress / dailyGoal) * 100

        val fragment = if (progressPercentage >= 100) {
            AchieveDayGoalFragment()
        } else {
            NotAchieveFragment()
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment) // Replace with your fragment container ID
            .addToBackStack(null)
            .commit()

        resetTodayAmount(userEmail)
    }

    private fun resetTodayAmount(userEmail: String) {
        db.collection("users").document(userEmail).update("todayAmount", 0.0)
    }

    private fun showAchievedGoalFragment(activity: FragmentActivity) {
        val fragment = AchieveDayGoalFragment()
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment) // Replace with your fragment container ID
            .addToBackStack(null)
            .commit()
    }
}