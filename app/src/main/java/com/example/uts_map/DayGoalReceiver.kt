package com.example.uts_map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DailyGoalReceiver : BroadcastReceiver() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onReceive(context: Context?, intent: Intent?) {
        val activity = context as? FragmentActivity ?: return

        val currentUser = auth.currentUser
        val userEmail = currentUser?.email

        if (currentUser == null || userEmail == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userEmail).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentAmount = document.getDouble("todayAmount") ?: 0.0
                val targetAmount = document.getDouble("targetAmount") ?: 2000.0
                val fragment = if (currentAmount >= targetAmount) {
                    AchieveDayGoalFragment()
                } else {
                    NotAchieveFragment()
                }

                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit()

                // Reset `todayAmount` for the new day
                db.collection("users").document(userEmail).update("todayAmount", 0.0)
            } else {
                Toast.makeText(context, "Daily goal data not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
