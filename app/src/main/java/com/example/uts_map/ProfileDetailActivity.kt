package com.example.uts_map

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        val sleepingTimeLayout = findViewById<ViewGroup>(R.id.sleepingTimeLayout)
        val wakeUpTimeLayout = findViewById<ViewGroup>(R.id.wakeUpTimeLayout)

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
                R.id.rbOther -> "Other"
                else -> null
            }

            val sleepingTime = sleepingTimeLayout.findViewById<TextView>(R.id.sleepingTimeValue).text.toString() +
                    " " + sleepingTimeLayout.findViewById<TextView>(R.id.sleepingTimeAmPm).text.toString()
            val wakeUpTime = wakeUpTimeLayout.findViewById<TextView>(R.id.wakeUpTimeValue).text.toString() +
                    " " + wakeUpTimeLayout.findViewById<TextView>(R.id.wakeUpTimeAmPm).text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || ageText.isEmpty() ||
                weightText.isEmpty() || heightText.isEmpty() || gender == null) {

                Toast.makeText(this, "Semua bidang harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                val age = ageText.toIntOrNull()
                val weight = weightText.toIntOrNull()
                val height = heightText.toIntOrNull()

                if (age == null || weight == null || height == null) {
                    Toast.makeText(this, "Umur, berat, dan tinggi harus berupa angka yang valid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (userEmail != null) {
                    databaseHelper.updateUserProfile(userEmail, firstName, lastName, age, weight, height, gender, sleepingTime, wakeUpTime)
                    SessionManager.setProfileCompleted(this, true)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        sleepingTimeLayout.setOnClickListener {
            showTimePickerDialog { selectedTime ->
                updateTimeDisplay(selectedTime, sleepingTimeLayout)
            }
        }

        wakeUpTimeLayout.setOnClickListener {
            showTimePickerDialog { selectedTime ->
                updateTimeDisplay(selectedTime, wakeUpTimeLayout)
            }
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(time)
        }, hour, minute, false)
        timePickerDialog.show()
    }

    private fun updateTimeDisplay(time: String, layout: ViewGroup) {
        val (hour, minute) = time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

        val timeValueView = layout.findViewById<TextView>(R.id.sleepingTimeValue) ?: layout.findViewById(R.id.wakeUpTimeValue)
        val amPmValueView = layout.findViewById<TextView>(R.id.sleepingTimeAmPm) ?: layout.findViewById(R.id.wakeUpTimeAmPm)

        timeValueView?.text = timeFormat.format(calendar.time)
        amPmValueView?.text = amPmFormat.format(calendar.time)
    }
}
