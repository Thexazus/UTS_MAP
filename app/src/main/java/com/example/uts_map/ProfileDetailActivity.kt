package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileDetailActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        databaseHelper = DatabaseHelper(this)

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val tpSleepingTime = findViewById<TimePicker>(R.id.tpSleepingTime)
        val tpWakeUpTime = findViewById<TimePicker>(R.id.tpWakeUpTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val userEmail = SessionManager.getUserEmail(this)

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val ageText = etAge.text.toString().trim()
            val weightText = etWeight.text.toString().trim()
            val heightText = etHeight.text.toString().trim()
            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                else -> null
            }
            val sleepingTime = "${tpSleepingTime.hour}:${tpSleepingTime.minute}"
            val wakeUpTime = "${tpWakeUpTime.hour}:${tpWakeUpTime.minute}"

            // Validasi input
            if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty() ||
                weightText.isEmpty() || heightText.isEmpty() || gender == null) {

                Toast.makeText(this, "Semua bidang harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                val age = ageText.toIntOrNull()
                val weight = weightText.toIntOrNull()
                val height = heightText.toIntOrNull()

                // Validasi apakah input angka valid (misalnya umur, berat, dan tinggi)
                if (age == null || weight == null || height == null) {
                    Toast.makeText(this, "Umur, berat, dan tinggi harus berupa angka yang valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Jika semua validasi lolos, simpan data
                if (userEmail != null) {
                    databaseHelper.updateUserProfile(userEmail, firstName, lastName, age, weight, height, gender, sleepingTime, wakeUpTime)
                    SessionManager.setProfileCompleted(this, true)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
