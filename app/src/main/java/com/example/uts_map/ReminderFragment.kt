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
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.reminderRecyclerView)
        addReminderButton = view.findViewById(R.id.addReminderButton)
        backButton = view.findViewById(R.id.backButton)
    }

    private fun setupRecyclerView() {
        reminderAdapter = ReminderAdapter(reminderList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            // Kembali ke fragment sebelumnya
            requireActivity().supportFragmentManager.popBackStack()
        }

        addReminderButton.setOnClickListener {
            showReminderDialog()
        }
    }

    private fun showReminderDialog() {
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            // Membuat array boolean untuk 7 hari (Senin-Minggu)
            val daysOfWeek = BooleanArray(7) { false }
            // Membuat reminder baru dengan isActive default true
            val newReminder = Reminder(timeString, daysOfWeek, true)
            reminderList.add(newReminder)
            reminderAdapter.notifyItemInserted(reminderList.size - 1)

            Toast.makeText(
                requireContext(),
                "Reminder set for $timeString",
                Toast.LENGTH_SHORT
            ).show()
        }
        dialogFragment.show(parentFragmentManager, "reminder_dialog")
    }

    private fun editReminder(position: Int) {
        val reminder = reminderList[position]
        val dialogFragment = ReminderDialogFragment.newInstance()
        dialogFragment.setOnTimeSetListener { hour, minute ->
            val timeString = String.format("%02d:%02d", hour, minute)
            reminderList[position] = Reminder(timeString, reminder.daysOfWeek, reminder.isActive)
            reminderAdapter.notifyItemChanged(position)
        }
        dialogFragment.show(parentFragmentManager, "reminder_dialog")
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReminderFragment()
    }
}