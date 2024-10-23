package com.example.uts_map

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.uts_map.databinding.ActivitySettingBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.apply {
            // Profile info
            titleText.text = "My Profile"

            // Stats
            heightValue.text = "${UserPreferences.getHeight(this@SettingActivity)}cm"
            weightValue.text = "${UserPreferences.getWeight(this@SettingActivity)}kg"
            ageValue.text = "${UserPreferences.getAge(this@SettingActivity)}yo"

            // Daily intake goal
            intakeValue.text = "${UserPreferences.getDailyIntakeGoal(this@SettingActivity)} ml"

            // Gender
            when (UserPreferences.getGender(this@SettingActivity)) {
                "Male" -> genderRadioGroup.check(R.id.radioMale)
                "Female" -> genderRadioGroup.check(R.id.radioFemale)
                else -> genderRadioGroup.check(R.id.radioOther)
            }

            // Sleeping time and Wake up time
            updateTimeDisplay(UserPreferences.getSleepingTime(this@SettingActivity), sleepingTimeLayout)
            updateTimeDisplay(UserPreferences.getWakeUpTime(this@SettingActivity), wakeUpTimeLayout)
        }
    }

    private fun setupListeners() {
        binding.apply {
            editButton.setOnClickListener {
                // Implement edit functionality
            }

            genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val gender = when (checkedId) {
                    R.id.radioMale -> "Male"
                    R.id.radioFemale -> "Female"
                    else -> "Other"
                }
                UserPreferences.setGender(this@SettingActivity, gender)
            }

            sleepingTimeLayout.setOnClickListener {
                showTimePickerDialog("sleeping")
            }

            wakeUpTimeLayout.setOnClickListener {
                showTimePickerDialog("wakeup")
            }
        }
    }

    private fun showTimePickerDialog(type: String) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                when (type) {
                    "sleeping" -> {
                        UserPreferences.setSleepingTime(this, time)
                        updateTimeDisplay(time, binding.sleepingTimeLayout)
                    }
                    "wakeup" -> {
                        UserPreferences.setWakeUpTime(this, time)
                        updateTimeDisplay(time, binding.wakeUpTimeLayout)
                    }
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateTimeDisplay(time: String, layout: android.view.ViewGroup) {
        val (hour, minute) = time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

        val timeValueView = when (layout.id) {
            R.id.sleepingTimeLayout -> layout.findViewById<TextView>(R.id.sleepingTimeValue)
            R.id.wakeUpTimeLayout -> layout.findViewById<TextView>(R.id.wakeUpTimeValue)
            else -> null
        }
        val amPmValueView = when (layout.id) {
            R.id.sleepingTimeLayout -> layout.findViewById<TextView>(R.id.sleepingTimeAmPm)
            R.id.wakeUpTimeLayout -> layout.findViewById<TextView>(R.id.wakeUpTimeAmPm)
            else -> null
        }

        timeValueView?.text = timeFormat.format(calendar.time)
        amPmValueView?.text = amPmFormat.format(calendar.time)
    }
}