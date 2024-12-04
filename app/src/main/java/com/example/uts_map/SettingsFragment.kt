package com.example.uts_map

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uts_map.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        setupUI()
        setupListeners()
        return binding.root
    }

    private fun setupUI() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        // Pastikan binding tidak null sebelum digunakan
        _binding?.let { binding ->
            // Get user data from Firebase Firestore
            db.collection("users").document(currentUser.email ?: "").get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "User"
                    binding.userName.text = "$firstName $lastName"

                    val email = document.getString("email") ?: currentUser.email
                    binding.userEmail.text = email

                    val height = document.getDouble("height") ?: 0.0
                    binding.heightValue.text = "${height.toInt()} cm"

                    val weight = document.getDouble("weight") ?: 0.0
                    binding.weightValue.text = "${weight.toInt()} kg"

                    val age = document.getLong("age") ?: 0
                    binding.ageValue.text = "$age yo"

                    val intake = document.getDouble("targetAmount") ?: 0.0
                    binding.intakeValue.text = "${intake.toInt()} ml"

                    val gender = document.getString("gender") ?: "Other"
                    val genderId = when (gender) {
                        "Male" -> R.id.radioMale
                        "Female" -> R.id.radioFemale
                        else -> R.id.radioOther
                    }
                    binding.genderRadioGroup.check(genderId)

                    val sleepingTime = document.getString("sleepingTime") ?: "00:00"
                    updateTimeDisplay(sleepingTime, binding.sleepingTimeLayout)

                    val wakeUpTime = document.getString("wakeUpTime") ?: "00:00"
                    updateTimeDisplay(wakeUpTime, binding.wakeUpTimeLayout)
                } else {
                    Toast.makeText(requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
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
            updateFieldInFirestore("gender", gender)
        }

        sleepingTimeLayout.setOnClickListener {
            showTimePickerDialog("sleeping")
        }

        wakeUpTimeLayout.setOnClickListener {
            showTimePickerDialog("wakeup")
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            navigateToLogin()
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
                        updateFieldInFirestore("sleepingTime", time)
                        updateTimeDisplay(time, binding.sleepingTimeLayout)
                    }
                    "wakeup" -> {
                        updateFieldInFirestore("wakeUpTime", time)
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
        try {
            // Validate input time format
            if (!time.matches(Regex("\\d{1,2}:\\d{2}"))) {
                throw IllegalArgumentException("Invalid time format: $time")
            }

            val (hour, minute) = time.split(":").map { it.toInt() }
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

            val timeValueTextView = layout.findViewById<TextView>(R.id.sleepingTimeValue)
            val timeAmPmTextView = layout.findViewById<TextView>(R.id.sleepingTimeAmPm)

            timeValueTextView?.text = timeFormat.format(calendar.time)
            timeAmPmTextView?.text = amPmFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e("updateTimeDisplay", "Error updating time display", e)
        }
    }

    private fun updateFieldInFirestore(field: String, value: Any) {
        val currentUser = auth.currentUser ?: run {
            navigateToLogin()
            return
        }

        val userEmail = currentUser.email
        if (userEmail.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Invalid user email", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userEmail).update(field, value)
            .addOnSuccessListener {
                Log.d("SettingsFragment", "$field updated successfully")
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update $field: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
