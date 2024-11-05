package com.example.uts_map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var greetingTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Find and set the greeting TextView
        greetingTextView = view.findViewById(R.id.textViewGreeting)
        val firstName = dbHelper.getCurrentUserFirstName()
        greetingTextView.text = "Hi, $firstName!"

        // Setup click listener for the notification icon
        view.findViewById<ImageView>(R.id.imageViewBell).setOnClickListener {
            // Navigate to ReminderFragment
            val reminderFragment = ReminderFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, reminderFragment) // Ensure this container ID is correct
                .addToBackStack(null)  // Add to back stack to enable back navigation
                .commit()
        }

        // Initialize other UI components
        setupUI()
    }

    private fun setupUI() {
        // Setup other UI components here, if needed
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
