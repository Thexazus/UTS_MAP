package com.example.uts_map

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.uts_map.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.*

class Settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.apply {
            // Profile info
            titleText.text = "My Profile"

            // Stats
            heightValue.text = "${UserPreferences.getHeight(requireContext())}cm"
            weightValue.text = "${UserPreferences.getWeight(requireContext())}kg"
            ageValue.text = "${UserPreferences.getAge(requireContext())}yo"

            // Daily intake goal
            intakeValue.text = "${UserPreferences.getDailyIntakeGoal(requireContext())} ml"

            // Gender
            when (UserPreferences.getGender(requireContext())) {
                "Male" -> genderRadioGroup.check(R.id.radioMale)
                "Female" -> genderRadioGroup.check(R.id.radioFemale)
                else -> genderRadioGroup.check(R.id.radioOther)
            }

            // Sleeping time and Wake up time
            updateTimeDisplay(UserPreferences.getSleepingTime(requireContext()), sleepingTimeLayout)
            updateTimeDisplay(UserPreferences.getWakeUpTime(requireContext()), wakeUpTimeLayout)
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
                UserPreferences.setGender(requireContext(), gender)
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
            context,
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                when (type) {
                    "sleeping" -> {
                        UserPreferences.setSleepingTime(requireContext(), time)
                        updateTimeDisplay(time, binding.sleepingTimeLayout)
                    }
                    "wakeup" -> {
                        UserPreferences.setWakeUpTime(requireContext(), time)
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