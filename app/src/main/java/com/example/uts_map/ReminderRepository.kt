package com.example.uts_map

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ReminderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val reminderCollection = db.collection("reminders")

    fun addReminder(reminder: Reminder, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        reminderCollection.add(reminder)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                onSuccess()
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getReminders(onSuccess: (List<Reminder>) -> Unit, onFailure: (Exception) -> Unit) {
        reminderCollection.get()
            .addOnSuccessListener { snapshot ->
                val reminders = snapshot.documents.mapNotNull { it.toObject<Reminder>() }
                onSuccess(reminders)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun updateReminder(
        id: String,
        updatedReminder: Reminder,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        reminderCollection.document(id).set(updatedReminder)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun deleteReminder(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        reminderCollection.document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}
