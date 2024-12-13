package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val currentUser  = auth.currentUser
        if (currentUser  == null) {
            navigateToLogin()
            return
        }

        binding.apply {
            db.collection("users").document(currentUser .email ?: "").get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Load user data
                    userName.text = "${document.getString("firstName") ?: "Unknown"} ${document.getString("lastName") ?: "User "}"
                    userEmail.text = document.getString("email") ?: currentUser .email
                    heightValue.text = "${document.getDouble("height")?.toInt() ?: 0} cm"
                    weightValue.text = "${document.getDouble("weight")?.toInt() ?: 0} kg"
                    ageValue.text = "${document.getLong("age") ?: 0} yo"
                    intakeValue.text = "${document.getDouble("targetAmount")?.toInt() ?: 0} ml" // Display target amount

                    // Display gender
                    val gender = document.getString("gender") ?: "Other"
                    displayGender(gender)

                    // Display profile picture based on gender
                    updateProfilePicture(gender)

                    // Display sleeping and wake-up times
                    updateTimeDisplay(document.getString("sleepingTime") ?: "00:00", sleepingTimeDisplay)
                    updateTimeDisplay(document.getString("wakeUpTime") ?: "00:00", wakeUpTimeDisplay)
                } else {
                    Toast.makeText(requireContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayGender(gender: String) {
        binding.apply {
            val genderImageView = genderImage
            val genderTextView = genderDisplayText

            when (gender) {
                "Male" -> {
                    genderImageView.setImageResource(R.drawable.male_icon)
                    genderTextView.text = "Male"
                }
                "Female" -> {
                    genderImageView.setImageResource(R.drawable.female_icon)
                    genderTextView.text = "Female"
                }
                else -> {
                    genderImageView.setImageResource(R.drawable.other_icon)
                    genderTextView.text = "Other"
                }
            }
        }
    }

    private fun updateProfilePicture(gender: String) {
        binding.apply {
            val profilePictureImageView = profileCard.findViewById<ImageView>(R.id.profilePicture)

            when (gender) {
                "Male" -> {
                    profilePictureImageView.setImageResource(R.drawable.profile_placeholder)
                }
                "Female" -> {
                    profilePictureImageView.setImageResource(R.drawable.female_profile)
                }
                else -> {
                    profilePictureImageView.setImageResource(R.drawable.male_profile)
                }
            }
        }
    }

    private fun setupListeners() = binding.apply {
        editButton.setOnClickListener {
            val intent = Intent(requireContext(), ProfileDetailActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }
    }

    private fun updateTimeDisplay(time: String, textView: TextView) {
        try {
            if (!time.matches(Regex("\\d{1,2}:\\d{2}"))) {
                throw IllegalArgumentException("Invalid time format: $time")
            }

            val (hour, minute) = time.split(":").map { it.toInt() }
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            textView.text = timeFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e("updateTimeDisplay", "Error updating time display", e)
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

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
