package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReminderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addReminderButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private val reminderRepository = ReminderRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        fetchReminders()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.reminderRecyclerView)
        addReminderButton = view.findViewById(R.id.addReminderButton)
        backButton = view.findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(reminderList,
            onUpdate = { reminder -> updateReminderInFirestore(reminder) },
            onDelete = { reminder -> deleteReminderFromFirestore(reminder) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        addReminderButton.setOnClickListener {
            showReminderDialog()
        }
    }

    private fun showReminderDialog() {
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute, daysOfWeek ->
            val timeString = String.format("%02d:%02d", hour, minute)
            val newReminder = Reminder("", timeString, daysOfWeek, true)

            addReminderToFirestore(newReminder)
        }
        dialogFragment.show(parentFragmentManager, "reminder_dialog")
    }

    private fun fetchReminders() {
        reminderRepository.getReminders(
            onSuccess = { reminders ->
                reminderList.clear()
                reminderList.addAll(reminders)
                reminderAdapter.notifyDataSetChanged()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to fetch reminders: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addReminderToFirestore(reminder: Reminder) {
        reminderRepository.addReminder(
            reminder,
            onSuccess = {
                Toast.makeText(requireContext(), "Reminder added successfully!", Toast.LENGTH_SHORT).show()
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to add reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateReminderInFirestore(reminder: Reminder) {
        reminderRepository.updateReminder(
            reminder.id,
            reminder,
            onSuccess = {
                Toast.makeText(requireContext(), "Reminder updated successfully!", Toast.LENGTH_SHORT).show()
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to update reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteReminderFromFirestore(reminder: Reminder) {
        reminderRepository.deleteReminder(
            reminder.id,
            onSuccess = {
                Toast.makeText(requireContext(), "Reminder deleted successfully!", Toast.LENGTH_SHORT).show()
                fetchReminders()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to delete reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    companion object {
        fun newInstance() = ReminderFragment()
    }
}
