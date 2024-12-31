package com.example.uts_map

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class SaveStepsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("StepTrackingPrefs", Context.MODE_PRIVATE)

    override fun doWork(): Result {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val stepCount = getCurrentStepCount()

            currentUser?.let { user ->
                val stepData = mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "stepCount" to stepCount
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.email ?: user.uid)
                    .collection("step_tracking")
                    .add(stepData)
                    .addOnSuccessListener {
                        Log.d("SaveStepsWorker", "Step count saved successfully: $stepCount")
                        // Reset step count after saving
                        resetStepCount()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SaveStepsWorker", "Failed to save step count", e)
                    }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SaveStepsWorker", "Error in doWork()", e)
            Result.failure()
        }
    }

    private fun getCurrentStepCount(): Int {
        return sharedPreferences.getInt(STEP_COUNT_KEY, 0)
    }

    private fun resetStepCount() {
        sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).apply()
    }

    companion object {
        private const val STEP_COUNT_KEY = "daily_step_count"

        // Method to increment steps (to be called from StepTrackingService)
        fun incrementStepCount(context: Context) {
            val sharedPreferences = context.getSharedPreferences("StepTrackingPrefs", Context.MODE_PRIVATE)
            val currentSteps = sharedPreferences.getInt(STEP_COUNT_KEY, 0)
            sharedPreferences.edit().putInt(STEP_COUNT_KEY, currentSteps + 1).apply()
        }
    }
}