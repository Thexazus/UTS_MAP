package com.example.uts_map

import java.util.Date

data class WaterIntake(
    val id: Long, // Bisa menggunakan timestamp sebagai ID
    val amount: Int,
    val timestamp: Date
)