package com.example.uts_map

data class WaterIntake(
    val id: Long, // Bisa menggunakan timestamp sebagai ID
    val amount: Int,
    val timestamp: String
)