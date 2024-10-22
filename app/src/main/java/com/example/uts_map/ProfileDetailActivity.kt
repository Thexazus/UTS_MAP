package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TimePicker
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
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString()
            val weight = etWeight.text.toString()
            val height = etHeight.text.toString()
            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Male"
                R.id.rbFemale -> "Female"
                else -> "Other"
            }
            val sleepingTime = "${tpSleepingTime.hour}:${tpSleepingTime.minute}"
            val wakeUpTime = "${tpWakeUpTime.hour}:${tpWakeUpTime.minute}"

            if (userEmail != null) {
                databaseHelper.updateUserProfile(userEmail, firstName, lastName, age, weight, height, gender, sleepingTime, wakeUpTime)
                SessionManager.setProfileCompleted(this, true)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}