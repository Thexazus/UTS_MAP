package com.example.uts_map.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.uts_map.WaterIntake
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class WaterViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _currentIntake = MutableLiveData(0)
    val currentIntake: LiveData<Int> = _currentIntake

    private val _waterAmount = MutableLiveData(250) // Default water intake amount
    val waterAmount: LiveData<Int> = _waterAmount

    private val _intakeHistory = MutableLiveData<List<WaterIntake>>(emptyList())
    val intakeHistory: LiveData<List<WaterIntake>> = _intakeHistory

    private val _goal = MutableLiveData(2000) // Default daily water intake goal
    val goal: LiveData<Int> = _goal

    // Function to adjust the water amount (for the dialog)
    fun adjustWaterAmount(change: Int) {
        val newAmount = (_waterAmount.value ?: 250) + change
        if (newAmount in 50..1000) { // Limit range between 50ml and 1000ml
            _waterAmount.value = newAmount
        }
    }

    // Add water intake (from Home)
    fun addWater(amount: Int) {
        val currentAmount = _currentIntake.value ?: 0
        _currentIntake.value = currentAmount + amount

        val newIntake = WaterIntake(
            id = System.currentTimeMillis(),
            amount = amount,
            // Convert Date to String before saving to Firestore
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        // Save to Firebase Firestore
        saveWaterIntakeToFirestore(newIntake)

        // Update intake history locally
        _intakeHistory.value = (_intakeHistory.value ?: emptyList()) + newIntake
    }

    // Remove water intake (from Home)
    fun removeWater(amount: Int) {
        // You can implement logic here to remove the water intake from Firestore if needed
        // For now, it is assumed we're just removing the last entry and updating the UI
        _intakeHistory.value = (_intakeHistory.value ?: emptyList()).dropLast(1)
        removeWaterIntakeFromFirestore(amount)
    }

    // Load water intake history from Firestore
    fun loadWaterIntakeDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val intakeList = snapshot.documents.map { document ->
                    WaterIntake(
                        id = document.getLong("id") ?: 0L,
                        amount = document.getLong("amount")?.toInt() ?: 0,
                        timestamp = document.getString("timestamp") ?: ""
                    )
                }
                _intakeHistory.value = intakeList
            }
            .addOnFailureListener { e ->
                Log.e("WaterViewModel", "Error loading data from Firestore", e)
            }
    }

    // Set daily goal for water intake
    fun setGoal(newGoal: Int) {
        if (newGoal > 0) {
            _goal.value = newGoal
        }
    }

    // Save water intake data to Firestore
    private fun saveWaterIntakeToFirestore(waterIntake: WaterIntake) {
        val userId = auth.currentUser?.uid ?: return
        val waterIntakeData = hashMapOf(
            "id" to waterIntake.id,
            "amount" to waterIntake.amount,
            "timestamp" to waterIntake.timestamp
        )

        firestore.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .add(waterIntakeData)
            .addOnSuccessListener {
                Log.d("WaterViewModel", "Water intake added to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("WaterViewModel", "Error saving water intake to Firestore", e)
            }
    }

    // Remove water intake from Firestore (optional)
    private fun removeWaterIntakeFromFirestore(amount: Int) {
        // For demonstration purposes, we'll simply drop the last document
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("waterIntakes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    val docRef = snapshot.documents[0].reference
                    docRef.delete()
                        .addOnSuccessListener {
                            Log.d("WaterViewModel", "Last water intake removed from Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("WaterViewModel", "Error removing water intake from Firestore", e)
                        }
                }
            }
    }

    // Set current intake value manually (useful for initializing with today's intake)
    fun setCurrentIntake(intakeToday: Int) {
        _currentIntake.value = intakeToday
    }

    init {
        loadWaterIntakeDataFromFirestore() // Load initial data from Firestore
    }
}
