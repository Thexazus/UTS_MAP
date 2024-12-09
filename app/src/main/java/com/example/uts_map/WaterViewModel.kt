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

    private val _waterAmount = MutableLiveData(250)
    val waterAmount: LiveData<Int> = _waterAmount

    private val _intakeHistory = MutableLiveData<List<WaterIntake>>(emptyList())
    val intakeHistory: LiveData<List<WaterIntake>> = _intakeHistory

    private val _goal = MutableLiveData(2000)
    val goal: LiveData<Int> = _goal

    fun adjustWaterAmount(change: Int) {
        val newAmount = (_waterAmount.value ?: 250) + change
        if (newAmount in 50..1000) {
            _waterAmount.value = newAmount
        }
    }

    fun addWater(amount: Int) {
        val userId = auth.currentUser?.uid ?: return
        val currentAmount = _currentIntake.value ?: 0
        _currentIntake.value = currentAmount + amount

        val newIntake = WaterIntake(
            amount = amount,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            userId = userId
        )

        saveWaterIntakeToFirestore(newIntake)
    }

    fun removeWater(waterIntake: WaterIntake) {
        removeWaterIntakeFromFirestore(waterIntake)
    }

    fun loadWaterIntakeDataFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("water_intakes")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val intakeList = snapshot.documents.map { document ->
                    WaterIntake(
                        id = document.id, // Gunakan ID dokumen Firestore
                        amount = document.getLong("amount")?.toInt() ?: 0,
                        timestamp = document.getString("timestamp") ?: "",
                        userId = document.getString("userId")
                    )
                }
                _intakeHistory.value = intakeList

                // Hitung total intake hari ini
                val todayIntake = intakeList
                    .filter {
                        isToday(it.timestamp)
                    }
                    .sumOf { it.amount }

                _currentIntake.value = todayIntake
            }
            .addOnFailureListener { e ->
                Log.e("WaterViewModel", "Error loading data from Firestore", e)
            }
    }

    // Fungsi helper untuk mengecek apakah timestamp adalah hari ini
    private fun isToday(timestamp: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = sdf.parse(timestamp)
            val today = Calendar.getInstance()
            val timestampCal = Calendar.getInstance().apply { time = date!! }

            today.get(Calendar.YEAR) == timestampCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == timestampCal.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    fun setGoal(newGoal: Int) {
        if (newGoal > 0) {
            _goal.value = newGoal

            // Simpan goal ke Firestore untuk pengguna yang sedang login
            val userId = auth.currentUser?.uid ?: return
            firestore.collection("user_goals")
                .document(userId)
                .set(mapOf("water_goal" to newGoal))
        }
    }

    private fun saveWaterIntakeToFirestore(waterIntake: WaterIntake) {
        firestore.collection("water_intakes")
            .add(mapOf(
                "amount" to waterIntake.amount,
                "timestamp" to waterIntake.timestamp,
                "userId" to waterIntake.userId
            ))
            .addOnSuccessListener { documentReference ->
                // Update lokal data dengan ID dari Firestore
                val updatedHistory = (_intakeHistory.value ?: emptyList()) +
                        waterIntake.copy(id = documentReference.id)
                _intakeHistory.value = updatedHistory

                Log.d("WaterViewModel", "Water intake added to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("WaterViewModel", "Error saving water intake to Firestore", e)
            }
    }

    private fun removeWaterIntakeFromFirestore(waterIntake: WaterIntake) {
        firestore.collection("water_intakes")
            .document(waterIntake.id)
            .delete()
            .addOnSuccessListener {
                // Update lokal data setelah berhasil dihapus
                val updatedHistory = (_intakeHistory.value ?: emptyList())
                    .filter { it.id != waterIntake.id }
                _intakeHistory.value = updatedHistory

                // Kurangi intake saat ini
                val currentAmount = _currentIntake.value ?: 0
                _currentIntake.value = (currentAmount - waterIntake.amount).coerceAtLeast(0)

                Log.d("WaterViewModel", "Water intake removed from Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("WaterViewModel", "Error removing water intake from Firestore", e)
            }
    }

    init {
        loadWaterIntakeDataFromFirestore()
    }
}