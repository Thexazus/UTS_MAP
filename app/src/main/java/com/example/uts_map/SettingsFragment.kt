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
        databaseHelper = DatabaseHelper(requireContext())
        setupUI()
        setupListeners()
        return binding.root
    }

    private fun setupUI() = binding.apply {
        titleText.text = "My Profile"

        // Set up user data from DatabaseHelper
        val firstName = databaseHelper.getCurrentUserFirstName()
        val lastName = databaseHelper.getCurrentUserLastName()
        userName.text = "$firstName $lastName"

        val email = databaseHelper.getCurrentUserEmail()
        userEmail.text = email ?: "No email"

        heightValue.text = "${databaseHelper.height() ?: 0} cm"
        weightValue.text = "${databaseHelper.getWeight() ?: 0} kg"
        ageValue.text = "${databaseHelper.getAge() ?: 0} yo"
        intakeValue.text = "${databaseHelper.getDailyWaterGoal()} ml"

        val gender = databaseHelper.getGender() ?: "Other"
        val genderId = when (gender) {
            "Male" -> R.id.radioMale
            "Female" -> R.id.radioFemale
            else -> R.id.radioOther
        }
        genderRadioGroup.check(genderId)

        // Update sleep and wake-up times
        updateTimeDisplay(databaseHelper.getSleepingTime() ?: "00:00", sleepingTimeLayout)
        updateTimeDisplay(databaseHelper.getWakeUpTime() ?: "00:00", wakeUpTimeLayout)
    }

    private fun setupListeners() = binding.apply {
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
            databaseHelper.setGender(gender)
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

    private fun showTimePickerDialog(type: String) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                when (type) {
                    "sleeping" -> {
                        databaseHelper.setSleepingTime(time)
                        updateTimeDisplay(time, binding.sleepingTimeLayout)
                    }
                    "wakeup" -> {
                        databaseHelper.setWakeUpTime(time)
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

        layout.findViewById<TextView>(R.id.sleepingTimeValue)?.text = timeFormat.format(calendar.time)
        layout.findViewById<TextView>(R.id.sleepingTimeAmPm)?.text = amPmFormat.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
