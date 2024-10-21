package com.example.uts_map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.uts_map.WaterIntake
import com.example.uts_map.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class WaterViewModel : ViewModel() {

    private lateinit var databaseHelper: DatabaseHelper

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
            // Convert Date to String before saving to the database
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )

        // Update intake history and save to the database
        _intakeHistory.value = (_intakeHistory.value ?: emptyList()) + newIntake
        databaseHelper.insertWaterIntake(newIntake)
    }

    // Remove water intake (from Home)
    fun removeWater(amount: Int) {
        databaseHelper.removeLastWaterIntake(amount)
        loadWaterIntakeDataFromDatabase() // Refresh history after removing water intake
    }

    // Load water intake history from the database
    fun loadWaterIntakeDataFromDatabase() {
        _intakeHistory.value = databaseHelper.getWaterIntakeData()
    }

    // Set daily goal for water intake
    fun setGoal(newGoal: Int) {
        if (newGoal > 0) {
            _goal.value = newGoal
        }
    }

    // Set the DatabaseHelper instance
    fun init(databaseHelper: DatabaseHelper) {
        this.databaseHelper = databaseHelper
        loadWaterIntakeDataFromDatabase() // Load initial data from the database
    }

    // Set water intake history manually (useful for setting from external sources like a database)
    fun setIntakeHistory(history: List<WaterIntake>) {
        _intakeHistory.value = history
    }

    // Set current intake value manually (useful for initializing with today's intake)
    fun setCurrentIntake(intakeToday: Int) {
        _currentIntake.value = intakeToday
    }
}
