package com.example.uts_map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.uts_map.WaterIntake
import java.util.*

class WaterViewModel : ViewModel() {

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

    fun addWater() {
        val amount = _waterAmount.value ?: 250
        _currentIntake.value = (_currentIntake.value ?: 0) + amount

        val newIntake = WaterIntake(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            timestamp = Date()
        )
        _intakeHistory.value = (_intakeHistory.value ?: emptyList()) + newIntake
    }

    fun setGoal(newGoal: Int) {
        if (newGoal > 0) {
            _goal.value = newGoal
        }
    }

    fun setIntakeHistory(history: List<WaterIntake>) {
        TODO("Not yet implemented")
    }

    fun setCurrentIntake(intakeToday: Int) {

    }
}