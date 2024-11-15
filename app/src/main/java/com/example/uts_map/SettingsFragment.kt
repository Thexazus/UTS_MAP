package com.example.uts_map

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.uts_map.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        databaseHelper = DatabaseHelper(requireContext()) // Initialize DatabaseHelper
        setupUI()
        setupListeners()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            titleText.text = "My Profile"

            // Use DatabaseHelper to retrieve user data
            userName.text = "${databaseHelper.getCurrentUserFirstName()} ${databaseHelper.getCurrentUserLastName()}"
            userEmail.text = databaseHelper.getCurrentUserEmail() ?: "No email"
            heightValue.text = "${databaseHelper.getHeight()} cm" // Access height through DatabaseHelper
            weightValue.text = "${databaseHelper.getWeight()} kg" // Access weight through DatabaseHelper
            ageValue.text = "${databaseHelper.getAge()} yo" // Access age through DatabaseHelper
            intakeValue.text = "${databaseHelper.getDailyWaterGoal()} ml"

            // Gender selection based on shared preferences
            when (databaseHelper.getGender()) { // Access gender through DatabaseHelper
                "Male" -> genderRadioGroup.check(R.id.radioMale)
                "Female" -> genderRadioGroup.check(R.id.radioFemale)
                else -> genderRadioGroup.check(R.id.radioOther)
            }

            // Retrieve and display sleeping and wake-up times
            val sleepingTime = databaseHelper.getSleepingTime()
            val wakeUpTime = databaseHelper.getWakeUpTime()
            updateTimeDisplay(sleepingTime, sleepingTimeLayout)
            updateTimeDisplay(wakeUpTime, wakeUpTimeLayout)
        }
    }

    private fun setupListeners() {
        binding.apply {
            editButton.setOnClickListener {
                val intent = Intent(requireContext(), ProfileDetailActivity::class.java)
                startActivity(intent)
            }

            genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val gender = when (checkedId) {
                    R.id.radioMale -> "Male"
                    R.id.radioFemale -> "Female"
                    else -> "Other"
                }
                databaseHelper.setGender(gender) // Set gender through DatabaseHelper
            }

            sleepingTimeLayout.setOnClickListener {
                showTimePickerDialog("sleeping")
            }

            wakeUpTimeLayout.setOnClickListener {
                showTimePickerDialog("wakeup")
            }

            logoutButton.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun showTimePickerDialog(type: String) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                when (type) {
                    "sleeping" -> {
                        databaseHelper.setSleepingTime(time) // Set sleeping time through DatabaseHelper
                        updateTimeDisplay(time, binding.sleepingTimeLayout)
                    }
                    "wakeup" -> {
                        databaseHelper.setWakeUpTime(time) // Set wake-up time through DatabaseHelper
                        updateTimeDisplay(time, binding.wakeUpTimeLayout)
                    }
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateTimeDisplay(time: String, layout: ViewGroup) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
