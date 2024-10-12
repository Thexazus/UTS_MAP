package com.example.uts_map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WaterViewModel : ViewModel() {
    private val _currentIntake = MutableLiveData(0)
    val currentIntake: LiveData<Int> = _currentIntake

    private val _goal = MutableLiveData(2000)
    val goal: LiveData<Int> = _goal

    private val _waterAmount = MutableLiveData(50)
    val waterAmount: LiveData<Int> = _waterAmount

    private val _intakeHistory = MutableLiveData<List<WaterIntake>>(emptyList())
    val intakeHistory: LiveData<List<WaterIntake>> = _intakeHistory

    fun addWater() {
        val amount = waterAmount.value ?: 50
        _currentIntake.value = (_currentIntake.value ?: 0) + amount
        addIntakeToHistory(amount)
    }

    fun adjustWaterAmount(adjustment: Int) {
        _waterAmount.value = (_waterAmount.value ?: 50) + adjustment
    }

    private fun addIntakeToHistory(amount: Int) {
        val currentTime = System.currentTimeMillis()
        val newIntake = WaterIntake(amount, currentTime)
        _intakeHistory.value = (_intakeHistory.value ?: emptyList()) + newIntake
    }
}

data class WaterIntake(val amount: Int, val timestamp: Long)