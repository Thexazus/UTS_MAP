package com.example.uts_map

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Properti untuk mendapatkan UID pengguna yang sedang login
    private val userId: String?
        get() = auth.currentUser?.uid // Gunakan nullable String

    fun getReminders(onSuccess: (List<Reminder>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = userId
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        firestore.collection("reminders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val reminders = result.map { document ->
                    document.toObject(Reminder::class.java).apply { id = document.id }
                }
                onSuccess(reminders)
            }
            .addOnFailureListener(onFailure)
    }

    fun addReminder(reminder: Reminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = userId
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        val reminderWithUserId = reminder.copy(userId = userId)

        firestore.collection("reminders")
            .add(reminderWithUserId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun updateReminder(reminderId: String, reminder: Reminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = userId
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        firestore.collection("reminders").document(reminderId)
            .set(reminder)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun deleteReminder(reminderId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = userId
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        firestore.collection("reminders").document(reminderId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}
