package com.example.uts_map

data class Reminder(
    var id: String = "", // Firestore document ID
    val time: String = "",
    val daysOfWeek: List<Boolean> = List(7) { false },
    var isEnabled: Boolean = false,
    var userId: String = "" // UID pengguna
)
