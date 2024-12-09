package com.example.uts_map

data class WaterIntake(
    val id: String = "", // Gunakan ID dokumen Firestore
    val amount: Int,
    val timestamp: String,
    val userId: String? = null // Tambahkan userId untuk autentikasi
)