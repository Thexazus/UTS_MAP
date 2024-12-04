package com.example.uts_map

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileDetailActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etSleepingTime = findViewById<EditText>(R.id.etSleepingTime)
        val etWakeUpTime = findViewById<EditText>(R.id.etWakeUpTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val userEmail = auth.currentUser?.email ?: return
        loadData(userEmail, etFirstName, etLastName, etAge, etWeight, etHeight, etSleepingTime, etWakeUpTime)

        // Time Pickers
        etSleepingTime.setOnClickListener { showTimePicker(etSleepingTime) }
        etWakeUpTime.setOnClickListener { showTimePicker(etWakeUpTime) }

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val age = etAge.text.toString().toIntOrNull()
            val weight = etWeight.text.toString().toDoubleOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()
            val sleepingTime = etSleepingTime.text.toString()
            val wakeUpTime = etWakeUpTime.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || age == null || weight == null || height == null || sleepingTime.isEmpty() || wakeUpTime.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveData(userEmail, firstName, lastName, age, weight, height, sleepingTime, wakeUpTime)
        }
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }.time)
            editText.setText(formattedTime)
        }, hour, minute, false).show()
    }

    private fun loadData(
        email: String,
        etFirstName: EditText,
        etLastName: EditText,
        etAge: EditText,
        etWeight: EditText,
        etHeight: EditText,
        etSleepingTime: EditText,
        etWakeUpTime: EditText
    ) {
        db.collection("users").document(email).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                etFirstName.setText(document.getString("firstName"))
                etLastName.setText(document.getString("lastName"))
                etAge.setText(document.getLong("age")?.toString())
                etWeight.setText(document.getDouble("weight")?.toString())
                etHeight.setText(document.getDouble("height")?.toString())
                etSleepingTime.setText(document.getString("sleepingTime"))
                etWakeUpTime.setText(document.getString("wakeUpTime"))
            }
        }
    }

    private fun saveData(
        email: String,
        firstName: String,
        lastName: String,
        age: Int,
        weight: Double,
        height: Double,
        sleepingTime: String,
        wakeUpTime: String
    ) {
        val userMap = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "age" to age,
            "weight" to weight,
            "height" to height,
            "sleepingTime" to sleepingTime,
            "wakeUpTime" to wakeUpTime
        )

        db.collection("users").document(email).set(userMap).addOnSuccessListener {
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
            navigateToMainActivity() // Navigate to MainActivity after saving
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save profile.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
